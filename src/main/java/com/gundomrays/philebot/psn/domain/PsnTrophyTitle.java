package com.gundomrays.philebot.psn.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsnTrophyTitle {

    // "trophy" for PS3, PS4 and PS Vita, "trophy2" for PS5 and PC
    private String npServiceName;

    private String npCommunicationId;

    private String trophySetVersion;

    private String trophyTitleName;

    private String trophyTitleIconUrl;

    private String trophyTitlePlatform;

    private boolean hasTrophyGroups;

    private PsnTrophyCounts definedTrophies;

    private PsnTrophyCounts earnedTrophies;

    private Integer progress;

    private Instant lastUpdatedDateTime;

}
