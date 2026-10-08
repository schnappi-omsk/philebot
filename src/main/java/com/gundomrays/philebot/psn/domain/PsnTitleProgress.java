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
@IdClass(PsnTitleProgressId.class)
public class PsnTitleProgress {

    @Id
    private String accountId;

    @Id
    private String npCommunicationId;

    private String npServiceName;

    private String titleName;

    private String titleIconUrl;

    private String platform;

    private int earnedBronze;

    private int earnedSilver;

    private int earnedGold;

    private int earnedPlatinum;

    private Integer progress;

    private Instant lastUpdated;

    // True when psn_earned_trophy holds every earned trophy of this title
    private boolean trophiesTracked;

}
