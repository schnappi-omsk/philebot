package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class XApiClient {

    Logger log = LoggerFactory.getLogger(XApiClient.class);

    private final WebClient webClient;

    public XApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Profile userByGamertag(final String gamertag) {
        Profile xboxProfile = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{gamertag}/profile-for-gamertag").build(gamertag))
                .retrieve()
                .bodyToMono(Profile.class)
                .block();

        if (xboxProfile != null) {
            log.info("Profile was found for gamertag {}, id = {}", gamertag, xboxProfile.getXuid());
            xboxProfile.setGamertag(gamertag);
            return xboxProfile;
        }

        throw new RuntimeException("Nothing found for gamertag: " + gamertag);
    }

    public TitleHistory titleHistory(final String xuid) {
        TitleHistory titleHistory = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{xuid}/title-history").build(xuid))
                .retrieve()
                .bodyToMono(TitleHistory.class)
                .block();
        if (titleHistory != null) {
            log.info("Title history found for XUID={}, titles count={}", xuid, titleHistory.getTitles().size());
            return titleHistory;
        }

        throw new RuntimeException("No title history found for xuid: " + xuid);
    }

    public Activity userActivity(final String xuid) {
        final Activity activity = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{xuid}/activity").build(xuid))
                .retrieve()
                .bodyToMono(Activity.class)
                .block();
        if (activity != null) {
            log.info("Found activity for xuid={}, items count: {}", xuid, activity.getActivityItems().size());
            return activity;
        }

        throw new RuntimeException("No activity found for xuid: " + xuid);
    }

}
