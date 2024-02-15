package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Column;
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
    @Column(name = "xuid")
    private String id;

    private String gamertag;

    @NonNull
    private String tgUsername;

    @NonNull
    private Long tgId;

    private boolean ping;

    private boolean active;

    private LocalDateTime lastAchievement = LocalDateTime.now();

}
