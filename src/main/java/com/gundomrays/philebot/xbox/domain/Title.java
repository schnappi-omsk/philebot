package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@RedisHash("XboxTitle")
public class Title {

    @Indexed
    private String titleId;

    @Indexed
    private String name;

    private TitleProgress achievement;

}
