package com.gundomrays.philebot.command;

import org.springframework.stereotype.Service;

@Service("/find")
public class PhilFindGameCommand implements PhilCommand {

    @Override
    public String execute(CommandRequest request) {

        return "To be implement, soon we can find game " + request.getArgument();
    }

}

