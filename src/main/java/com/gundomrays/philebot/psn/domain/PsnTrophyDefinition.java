package com.gundomrays.philebot.psn.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsnTrophyDefinition {

    private Integer trophyId;

    private boolean trophyHidden;

    private String trophyType;

    private String trophyName;

    private String trophyDetail;

    private String trophyIconUrl;

    private String trophyGroupId;

}
