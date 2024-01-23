package com.gundomrays.philebot.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandRequest {

    private String caller;
    private Long chatId;
    private String command;
    private String argument;

}
