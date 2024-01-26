package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;


import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
public class TitleHistory {

    @Id
    private Integer titleHistoryId;
    private String xuid;

    @OneToMany(mappedBy = "titleId")
    private List<Title> titles = new LinkedList<>();

}
