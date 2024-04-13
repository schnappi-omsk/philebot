package com.gundomrays.philebot.telegram.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AwardMsg {
    @Id
    private Integer id;
    private String message;
}
