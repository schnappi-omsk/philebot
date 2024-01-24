package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Getter
@Setter
@RedisHash("XboxProfile")
public class Profile {

    @NonNull
    @Indexed
    private String id;

    @Indexed
    private String gamertag;

    @NonNull
    @Indexed
    private String tgUsername;

    @NonNull
    @Indexed
    private Long tgId;

    private LocalDateTime lastAchievement = LocalDateTime.MIN;

}
