package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Title {

    @Id
    private String titleId;

    private String name;

}
