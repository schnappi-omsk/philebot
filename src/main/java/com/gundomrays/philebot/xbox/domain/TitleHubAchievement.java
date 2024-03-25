package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class TitleHubAchievement implements Comparable<TitleHubAchievement> {

    private String name;
    private String description;
    private boolean isSecret;

    private List<Title> titleAssociations = new LinkedList<>();
    private Progression progression;
    private List<Rewards> rewards = new LinkedList<>();
    private Rarity rarity;
    private List<MediaAsset> mediaAssets = new LinkedList<>();

    @Override
    public int compareTo(TitleHubAchievement o) {
        return progression.getTimeUnlocked().compareTo(o.progression.getTimeUnlocked());
    }
}
