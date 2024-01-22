package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.xapi.XboxUserScreenshotService;
import org.springframework.stereotype.Service;

@Service("/screenshots")
public class PhilScreenshotsCommand implements PhilCommand {

    private final XboxUserScreenshotService xboxUserScreenshotService;

    public PhilScreenshotsCommand(XboxUserScreenshotService xboxUserScreenshotService) {
        this.xboxUserScreenshotService = xboxUserScreenshotService;
    }

    @Override
    public String execute(String caller, String argument) {
        return xboxUserScreenshotService.playerScreenshots(caller);
    }
}
