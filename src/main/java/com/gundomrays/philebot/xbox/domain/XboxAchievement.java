package com.gundomrays.philebot.xbox.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class XboxAchievement {
    private Profile profile;
    private TitleHubAchievement achievement;
}
