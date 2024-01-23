package com.gundomrays.philebot.command;

import org.springframework.stereotype.Service;

@Service(SystemCommandTypes.XBOX_ACHIEVEMENTS)
public class PhilAchievementsCommand implements PhilCommand {
    @Override
    public String execute(String caller, String parameter) {
        return null;
    }
}
