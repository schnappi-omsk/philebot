package com.gundomrays.philebot.command;

import org.springframework.stereotype.Service;

@Service("/penis")
public class PCommand implements PhilCommand {

    @Override
    public CommandResponse execute(CommandRequest request) {
        CommandResponse response = new CommandResponse();
        response.setMessage("Например, в рот");
        return response;
    }

}
