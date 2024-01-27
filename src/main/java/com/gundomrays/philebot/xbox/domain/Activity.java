package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@Entity
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer activityId;

    private String xuid;

    @OneToMany(mappedBy = "activityId")
    private Set<ActivityItem> activityItems = new TreeSet<>();

}