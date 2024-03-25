package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class TitleHubTitleList {

    private String xuid;

    private List<TitleHubTitle> titles = new LinkedList<>();

}
