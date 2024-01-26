package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@Entity
public class Activity {

    @Id
    private Integer activityId;

    private String xuid;

    @OneToMany(mappedBy = "activityId")
    private Set<ActivityItem> activityItems = new TreeSet<>();

}