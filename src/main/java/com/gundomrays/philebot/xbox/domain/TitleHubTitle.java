package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class TitleHubTitle {

    private String titleId;

    private String name;

    private Achievement achievement;

    private List<String> devices = new LinkedList<>();

}
