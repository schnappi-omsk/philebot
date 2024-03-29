package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.*;
import com.gundomrays.philebot.xbox.xapi.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class XApiClient {

    Logger log = LoggerFactory.getLogger(XApiClient.class);

    private final WebClient webClient;

    public XApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Profile userByGamertag(final String gamertag) {
        Profile xboxProfile = xapiRequest("/{gamertag}/profile-for-gamertag", gamertag, Profile.class).block();

        if (xboxProfile != null) {
            log.info("Profile was found for gamertag {}, id = {}", gamertag, xboxProfile.getId());
            xboxProfile.setGamertag(gamertag);
            return xboxProfile;
        }

        throw new RuntimeException("Nothing found for gamertag: " + gamertag);
    }

    public TitleHistory titleHistory(final String xuid) {
        final Long startTime = System.currentTimeMillis();
        TitleHistory titleHistory = xapiRequest("/{xuid}/title-history", xuid, TitleHistory.class).block();

        final Long endTime = System.currentTimeMillis();
        log.info("TitleHistory XAPI request for xuid={} took {} ms.", xuid, endTime - startTime);

        if (titleHistory != null) {
            log.info("Title history found for XUID={}, titles count={}", xuid, titleHistory.getTitles().size());
            return titleHistory;
        }

        throw new RuntimeException("No title history found for xuid: " + xuid);
    }

    public TitleHubTitleList titleHubTitleList(final String xuid) {
        final Long startTime = System.currentTimeMillis();

        final  TitleHubTitleList titleHubTitleList =
                xapiRequest("/{xuid}/titlehub-achievement-list", xuid, TitleHubTitleList.class).block();

        final Long endTime = System.currentTimeMillis();
        log.info("TitleHub Title List XAPI request for xuid={} took {} ms.", xuid, endTime - startTime);

        if (titleHubTitleList == null) {
            throw new RuntimeException("No TitleHub achievements found for xuid: " + xuid);
        }

        log.info("TitleHub Achievements response for xuid={}, titles count={}", xuid, titleHubTitleList.getTitles().size());
        return titleHubTitleList;
    }

    public TitleHubAchievements titleHubAchievements(final String xuid, final String title) {
        final Map<String, String> argument = Map.of("xuid", xuid, "titleId", title);

        final Long startTime = System.currentTimeMillis();

        final TitleHubAchievements achievements
                = xapiRequest("/{xuid}/achievements/{titleId}", argument, TitleHubAchievements.class).block();

        final Long endTime = System.currentTimeMillis();
        log.info("TitleHub Achievements XAPI request for xuid={} and title={} took {} ms.", xuid, title, endTime - startTime);

        return achievements;
    }

    private <T> Mono<T> xapiRequest(final String uri, final Map<String, String> argument, Class<T> clazz) {
        return xapiRequest(uri, argument, clazz, false)
                .onErrorResume(
                        UnauthorizedException.class,
                        e -> xapiRequest(uri, argument, clazz, true)
                );
    }

    private <T> Mono<T> xapiRequest(final String uri, final String argument, Class<T> clazz) {
        final Map<String, String> arg = mapFromArgument(uri, argument);
        return xapiRequest(uri, arg, clazz, false)
                .onErrorResume(
                        UnauthorizedException.class,
                        e -> xapiRequest(uri, arg, clazz, true)
                );
    }

    private <T> Mono<T> xapiRequest(final String uri, final Map<String, String> argument, Class<T> clazz, boolean freshLogin) {
        return  webClient.get()
                .uri(uriBuilder -> {
                    if (freshLogin) {
                        log.warn("xAPI returned 401: Unauthorized, trying to refresh login...");
                        return uriBuilder.path(uri).queryParam("fresh-login").build(argument);
                    } else {
                        return uriBuilder.path(uri).build(argument);
                    }
                })
                .retrieve()
                .onStatus(
                        HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedException("401"))
                )
                .bodyToMono(clazz);
    }

    private Map<String, String> mapFromArgument(final String uri, final String argument) {
        final Pattern pattern = Pattern.compile("\\{(.*?)}");
        final Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Collections.singletonMap(matcher.group(1), argument);
        } else {
            return Collections.emptyMap();
        }
    }

}
