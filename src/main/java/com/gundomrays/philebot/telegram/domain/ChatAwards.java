package com.gundomrays.philebot.telegram.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ChatAwards {
    @Id
    private String tgId;
    private LocalDate awardDate;
}
