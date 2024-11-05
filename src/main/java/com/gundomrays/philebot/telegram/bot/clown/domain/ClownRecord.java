package com.gundomrays.philebot.telegram.bot.clown.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClownRecord {

    private String appId;
    private String name;
    private String banner;
    private Wokeness woke;
    private String description;

}
