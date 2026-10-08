package com.gundomrays.philebot.psn.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@IdClass(PsnEarnedTrophyId.class)
public class PsnEarnedTrophy {

    @Id
    private String accountId;

    @Id
    private String npCommunicationId;

    @Id
    private Integer trophyId;

    private String trophyType;

    private Instant earnedAt;

}
