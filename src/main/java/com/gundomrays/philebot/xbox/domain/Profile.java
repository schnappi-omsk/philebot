package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Profile {

    @NonNull
    @Id
    private String xuid;

    private String gamertag;

    @NonNull
    private String tgUsername;

    @NonNull
    private Long tgId;

    private LocalDateTime lastAchievement = LocalDateTime.MIN;

}
