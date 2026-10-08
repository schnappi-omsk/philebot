package com.gundomrays.philebot.psn;

import com.gundomrays.philebot.data.PsnTrophyDataService;
import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.psn.api.PsnApiClient;
import com.gundomrays.philebot.psn.api.exception.PsnAccessDeniedException;
import com.gundomrays.philebot.psn.api.exception.PsnApiException;
import com.gundomrays.philebot.psn.auth.PsnAuthService;
import com.gundomrays.philebot.psn.auth.PsnAuthenticationException;
import com.gundomrays.philebot.psn.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PsnTrophyActivityService {

    private static final Logger log = LoggerFactory.getLogger(PsnTrophyActivityService.class);

    private static final int TOO_MANY_REQUESTS = 429;

    private static final int MAX_CACHED_TITLES = 200;

    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneOffset.UTC);

    private final PsnApiClient psnApiClient;

    private final PsnAuthService psnAuthService;

    private final PsnTrophyDataService psnTrophyDataService;

    private final PsnTrophyQueue psnTrophyQueue;

    private final MessageQueue messageQueue;

    private final Clock clock;

    private final int titlesLimit;

    private final int limitPerUser;

    private final PsnTrophyType minTrophyType;

    private final Duration refreshWarning;

    private final String tokenExpiringMessage;

    private final String authFailedMessage;

    // Trophy names and icons per title and trophy set version
    private final Map<String, Map<Integer, PsnTrophyDefinition>> definitions = new ConcurrentHashMap<>();

    private volatile boolean paused;

    private boolean expiryWarned;

    @Autowired
    public PsnTrophyActivityService(PsnApiClient psnApiClient,
                                    PsnAuthService psnAuthService,
                                    PsnTrophyDataService psnTrophyDataService,
                                    PsnTrophyQueue psnTrophyQueue,
                                    MessageQueue messageQueue,
                                    @Value("${psn.titlesLimit:50}") int titlesLimit,
                                    @Value("${psn.limitPerUser:5}") int limitPerUser,
                                    @Value("${psn.minTrophyType:bronze}") String minTrophyType,
                                    @Value("${psn.refreshWarningDays:7}") int refreshWarningDays,
                                    @Value("${messages.psn.tokenExpiring}") String tokenExpiringMessage,
                                    @Value("${messages.psn.authFailed}") String authFailedMessage) {
        this(psnApiClient, psnAuthService, psnTrophyDataService, psnTrophyQueue, messageQueue, Clock.systemUTC(),
                titlesLimit, limitPerUser, minTrophyType, refreshWarningDays, tokenExpiringMessage, authFailedMessage);
    }

    PsnTrophyActivityService(PsnApiClient psnApiClient, PsnAuthService psnAuthService,
                             PsnTrophyDataService psnTrophyDataService, PsnTrophyQueue psnTrophyQueue,
                             MessageQueue messageQueue, Clock clock, int titlesLimit, int limitPerUser,
                             String minTrophyType, int refreshWarningDays,
                             String tokenExpiringMessage, String authFailedMessage) {
        this.psnApiClient = psnApiClient;
        this.psnAuthService = psnAuthService;
        this.psnTrophyDataService = psnTrophyDataService;
        this.psnTrophyQueue = psnTrophyQueue;
        this.messageQueue = messageQueue;
        this.clock = clock;
        this.titlesLimit = titlesLimit;
        this.limitPerUser = limitPerUser;
        this.minTrophyType = Optional.ofNullable(PsnTrophyType.fromValue(minTrophyType)).orElse(PsnTrophyType.BRONZE);
        this.refreshWarning = Duration.ofDays(refreshWarningDays);
        this.tokenExpiringMessage = tokenExpiringMessage;
        this.authFailedMessage = authFailedMessage;
    }

    @Scheduled(initialDelay = 1L, fixedDelay = 5L, timeUnit = TimeUnit.MINUTES)
    public void allPlayersLatestTrophies() {
        if (paused) {
            log.debug("PSN polling is paused until the bot is restarted with a new NPSSO");
            return;
        }

        final List<PsnProfile> players = psnTrophyDataService.profiles();
        if (players.isEmpty()) {
            return;
        }
        warnIfRefreshTokenExpires();

        for (PsnProfile player : players) {
            if (!player.isActive()) {
                log.info("Skipping PSN player {} - not active", player.getOnlineId());
                continue;
            }
            try {
                processPlayerTrophies(player);
            } catch (PsnAuthenticationException e) {
                pause(e);
                return;
            } catch (PsnAccessDeniedException e) {
                log.warn("Trophies of {} are not visible to the bot account: {}", player.getOnlineId(), e.getMessage());
            } catch (PsnApiException e) {
                if (e.getStatus() == TOO_MANY_REQUESTS) {
                    log.warn("PSN rate limit reached, stopping this run");
                    return;
                }
                log.error("PSN request failed for {}: {}", player.getOnlineId(), e.getMessage());
            } catch (RuntimeException e) {
                log.error("Error processing PSN trophies of " + player.getOnlineId(), e);
            }
        }
    }

    private void processPlayerTrophies(final PsnProfile player) {
        final List<PsnTrophyTitle> titles =
                psnApiClient.trophyTitles(player.getAccountId(), titlesLimit, 0).getTrophyTitles();
        final Map<String, PsnTitleProgress> stored = psnTrophyDataService.titleProgress(player.getAccountId());

        for (PsnTrophyTitle title : titles) {
            final PsnTitleProgress progress = stored.get(title.getNpCommunicationId());
            if (progress == null && earnedTotal(title) == 0) {
                // A new game without trophies yet, nothing to fetch
                psnTrophyDataService.saveTitleProgress(player, title, null, List.of());
            } else if (countsChanged(progress, title)) {
                log.info("Trophies changed for PSN player {} and title={}", player.getOnlineId(), title.getTrophyTitleName());
                processTitle(player, title, progress);
            }
        }
    }

    private void processTitle(final PsnProfile player, final PsnTrophyTitle title, final PsnTitleProgress progress) {
        final PsnUserTrophies userTrophies = psnApiClient.earnedTrophies(
                player.getAccountId(), title.getNpCommunicationId(), title.getNpServiceName());
        final List<PsnUserTrophy> earned = userTrophies.getTrophies().stream()
                .filter(trophy -> trophy.isEarned() && trophy.getTrophyId() != null)
                .toList();

        final List<PsnUserTrophy> announced = announced(newTrophies(player, title, progress, earned));
        if (!announced.isEmpty()) {
            final Map<Integer, PsnTrophyDefinition> titleDefinitions =
                    definitions(title, userTrophies.getTrophySetVersion());
            announced.forEach(trophy -> psnTrophyQueue.placeTrophy(
                    toTrophy(player, title, trophy, titleDefinitions.get(trophy.getTrophyId()))));
        }

        psnTrophyDataService.saveTitleProgress(player, title, progress, earned);
    }

    List<PsnUserTrophy> newTrophies(final PsnProfile player, final PsnTrophyTitle title,
                                    final PsnTitleProgress progress, final List<PsnUserTrophy> earned) {
        if (progress != null && progress.isTrophiesTracked()) {
            final Set<Integer> known =
                    psnTrophyDataService.earnedTrophyIds(player.getAccountId(), title.getNpCommunicationId());
            return earned.stream().filter(trophy -> !known.contains(trophy.getTrophyId())).toList();
        }

        // Single trophies are not known yet: everything earned after the last stored update is new
        final Instant since = progress != null && progress.getLastUpdated() != null
                ? progress.getLastUpdated()
                : player.getRegisteredAt();
        return earned.stream()
                .filter(trophy -> trophy.getEarnedDateTime() != null && trophy.getEarnedDateTime().isAfter(since))
                .toList();
    }

    // Newest trophies of the allowed types up to the limit, platinum always included, oldest first
    List<PsnUserTrophy> announced(final List<PsnUserTrophy> newTrophies) {
        final List<PsnUserTrophy> allowed = newTrophies.stream()
                .filter(trophy -> type(trophy) == PsnTrophyType.PLATINUM || type(trophy).compareTo(minTrophyType) >= 0)
                .sorted(Comparator.comparing(PsnUserTrophy::getEarnedDateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toCollection(ArrayList::new));

        final List<PsnUserTrophy> selected = new ArrayList<>(allowed.subList(0, Math.min(limitPerUser, allowed.size())));
        allowed.stream()
                .filter(trophy -> !selected.isEmpty())
                .filter(trophy -> type(trophy) == PsnTrophyType.PLATINUM && !selected.contains(trophy))
                .findFirst()
                .ifPresent(platinum -> selected.set(selected.size() - 1, platinum));

        if (newTrophies.size() > selected.size()) {
            log.info("{} new PSN trophies, {} will be announced", newTrophies.size(), selected.size());
        }
        selected.sort(Comparator.comparing(PsnUserTrophy::getEarnedDateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return selected;
    }

    private Map<Integer, PsnTrophyDefinition> definitions(final PsnTrophyTitle title, final String trophySetVersion) {
        final String key = title.getNpCommunicationId() + ":" + trophySetVersion;
        final Map<Integer, PsnTrophyDefinition> cached = definitions.get(key);
        if (cached != null) {
            return cached;
        }
        if (definitions.size() >= MAX_CACHED_TITLES) {
            definitions.clear();
        }
        final Map<Integer, PsnTrophyDefinition> loaded = psnApiClient
                .titleTrophies(title.getNpCommunicationId(), title.getNpServiceName())
                .getTrophies()
                .stream()
                .filter(definition -> definition.getTrophyId() != null)
                .collect(Collectors.toMap(PsnTrophyDefinition::getTrophyId, Function.identity(), (first, second) -> first));
        definitions.put(key, loaded);
        return loaded;
    }

    private PsnTrophy toTrophy(final PsnProfile player, final PsnTrophyTitle title,
                               final PsnUserTrophy trophy, final PsnTrophyDefinition definition) {
        final String name = definition != null && definition.getTrophyName() != null
                ? definition.getTrophyName()
                : "Trophy #" + trophy.getTrophyId();
        final String detail = definition != null && definition.getTrophyDetail() != null ? definition.getTrophyDetail() : "";
        final String icon = definition != null && definition.getTrophyIconUrl() != null
                ? definition.getTrophyIconUrl()
                : title.getTrophyTitleIconUrl();
        return new PsnTrophy(player, title.getTrophyTitleName(), name, detail, icon, type(trophy),
                trophy.getTrophyEarnedRate(), trophy.getEarnedDateTime());
    }

    private void warnIfRefreshTokenExpires() {
        final Instant expiresAt = psnAuthService.refreshTokenExpiresAt();
        if (expiresAt == null) {
            return;
        }
        final boolean expiresSoon = expiresAt.isBefore(clock.instant().plus(refreshWarning));
        if (expiresSoon && !expiryWarned) {
            log.warn("PSN refresh token expires at {}, a new NPSSO is needed", expiresAt);
            messageQueue.messageToSend(String.format(tokenExpiringMessage, EXPIRY_FORMAT.format(expiresAt)));
        }
        expiryWarned = expiresSoon;
    }

    private void pause(final PsnAuthenticationException e) {
        paused = true;
        log.error("PSN authentication failed, PSN polling is paused: {}", e.getMessage());
        messageQueue.messageToSend(authFailedMessage);
    }

    private static PsnTrophyType type(final PsnUserTrophy trophy) {
        return Optional.ofNullable(PsnTrophyType.fromValue(trophy.getTrophyType())).orElse(PsnTrophyType.BRONZE);
    }

    private static boolean countsChanged(final PsnTitleProgress stored, final PsnTrophyTitle title) {
        final PsnTrophyCounts earned = title.getEarnedTrophies();
        if (earned == null) {
            return false;
        }
        return stored == null
                || stored.getEarnedBronze() != earned.getBronze()
                || stored.getEarnedSilver() != earned.getSilver()
                || stored.getEarnedGold() != earned.getGold()
                || stored.getEarnedPlatinum() != earned.getPlatinum();
    }

    private static int earnedTotal(final PsnTrophyTitle title) {
        final PsnTrophyCounts earned = title.getEarnedTrophies();
        return earned == null ? 0 : earned.getBronze() + earned.getSilver() + earned.getGold() + earned.getPlatinum();
    }

}
