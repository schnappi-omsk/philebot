package com.gundomrays.philebot.psn.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsnUserTrophy {

    private Integer trophyId;

    private boolean trophyHidden;

    private boolean earned;

    private Instant earnedDateTime;

    private String trophyType;

    // 0 - ultra rare, 1 - very rare, 2 - rare, 3 - common
    private Integer trophyRare;

    private String trophyEarnedRate;

}
