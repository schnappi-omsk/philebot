package com.gundomrays.philebot.psn.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsnTrophyDefinitions {

    private String trophySetVersion;

    private List<PsnTrophyDefinition> trophies = new LinkedList<>();

    private Integer totalItemCount;

}
