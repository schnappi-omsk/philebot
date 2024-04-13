package com.gundomrays.philebot.telegram.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AwardNominee {
    @Id
    private String tgId;
    private String tgName;
}
