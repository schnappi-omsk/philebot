package com.gundomrays.philebot.telegram.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Settings {

    @Id
    private String id;

    private String description;

    private String value;

}
