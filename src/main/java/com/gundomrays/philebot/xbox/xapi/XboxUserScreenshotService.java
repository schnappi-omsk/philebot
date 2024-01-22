package com.gundomrays.philebot.xbox.xapi;


import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.xbox.domain.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class XboxUserScreenshotService {

    private static final Logger log = LoggerFactory.getLogger(XboxUserScreenshotService.class);

    private final XboxProfileRepository xboxProfileRepository;

    private final XApiClient xApiClient;

    public XboxUserScreenshotService(XboxProfileRepository xboxProfileRepository, XApiClient xApiClient) {
        this.xboxProfileRepository = xboxProfileRepository;
        this.xApiClient = xApiClient;
    }

    public String playerScreenshots(final String tgUsername) {
        log.info("Retrieving screenshots for tgUsername {}", tgUsername);

        final Profile profile = xboxProfileRepository.findByTgUsername(tgUsername)
                .orElseThrow(() -> new RuntimeException("User is not registered: " + tgUsername));

        return xApiClient.usersScreenshots(profile.getId());
    }

}
