package com.gundomrays.philebot.psn.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PsnEarnedTrophyId implements Serializable {

    private String accountId;

    private String npCommunicationId;

    private Integer trophyId;

}
