package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
public class Activity {

    private Set<ActivityItem> activityItems = new TreeSet<>();

}
