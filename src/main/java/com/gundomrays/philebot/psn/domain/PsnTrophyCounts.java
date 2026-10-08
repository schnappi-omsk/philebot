package com.gundomrays.philebot.psn.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsnTrophyCounts {

    private int bronze;

    private int silver;

    private int gold;

    private int platinum;

}
