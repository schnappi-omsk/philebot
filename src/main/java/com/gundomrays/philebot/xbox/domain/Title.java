package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
public class Title {

    @Id
    private String titleId;

    private String name;

    private String titleImg;

    @Transient
    private Achievement achievement;

    @Transient
    private List<Image> images = new LinkedList<>();

}
