package com.gundomrays.philebot.command;

import org.springframework.stereotype.Service;

@Service(SystemCommandTypes.XBOX_ACHIEVEMENTS)
public class PhilAchievementsCommand implements PhilCommand {

    @Override
    public String execute(CommandRequest request) {
        return null;
    }
}
