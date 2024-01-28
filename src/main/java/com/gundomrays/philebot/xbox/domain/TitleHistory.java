package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
public class TitleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer titleHistoryId;

    private String xuid;

    @ManyToOne
    @JoinColumn(name = "title_id")
    private Title title;

    private Integer currentGamescore;

    private Integer totalGamescore;

    private LocalDateTime lastUpdated;

    @Transient
    private List<Title> titles = new LinkedList<>();

}
