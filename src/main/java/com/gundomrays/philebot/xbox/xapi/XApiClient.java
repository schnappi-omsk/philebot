package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import com.gundomrays.philebot.xbox.xapi.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
        TitleHistory titleHistory = xapiRequest("/{xuid}/title-history", xuid, TitleHistory.class).block();

        if (titleHistory != null) {
            log.info("Title history found for XUID={}, titles count={}", xuid, titleHistory.getTitles().size());
            return titleHistory;
        }

        throw new RuntimeException("No title history found for xuid: " + xuid);
    }

    public Activity userActivity(final String xuid) {

        final Activity activity = xapiRequest("/{xuid}/activity", xuid, Activity.class).block();

        if (activity != null) {
            log.info("Found activity for xuid={}, items count: {}", xuid, activity.getActivityItems().size());
            return activity;
        }

        throw new RuntimeException("No activity found for xuid: " + xuid);
    }

    private <T> Mono<T> xapiRequest(final String uri, final String argument, Class<T> clazz) {
        return xapiRequest(uri, argument, clazz, false)
                .onErrorResume(
                        UnauthorizedException.class,
                        e -> xapiRequest(uri, argument, clazz, true)
                );
    }

    private <T> Mono<T> xapiRequest(final String uri, final String argument, Class<T> clazz, boolean freshLogin) {
        return  webClient.get()
                .uri(uriBuilder -> {
                    if (freshLogin) {
                        log.warn("xAPI returned 401: Unauthorized, trying to refresh login...");
                        return uriBuilder.path(uri).queryParam("fresh-login", "").build(argument);
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

}
