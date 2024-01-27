package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    // LocalDateTime.MIN is not supported by PostgreSQL
    private LocalDateTime lastAchievement = LocalDateTime.of(
            LocalDate.of(1970, 1, 1), LocalTime.of(0, 0)
    );

}
