package com.gundomrays.philebot.command;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service("/roll")
public class RollCommand implements PhilCommand {
    @Override
    public CommandResponse execute(CommandRequest request) {
        final Random random = new Random();
        return PhilCommandUtils.textResponse(String.valueOf(random.nextInt(100)));
    }
}
