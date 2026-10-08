package com.gundomrays.philebot.psn.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
public class PsnProfile {

    @Id
    private String accountId;

    private String onlineId;

    private Long tgId;

    private String tgUsername;

    private boolean ping;

    private boolean active = true;

    private Instant registeredAt = Instant.now();

}
