package com.gundomrays.philebot.psn.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// A newly earned trophy, ready to be announced in the chat
@Getter
@Setter
@AllArgsConstructor
public class PsnTrophy {
    private PsnProfile profile;
    private String titleName;
    private String trophyName;
    private String trophyDetail;
    private String trophyIconUrl;
    private PsnTrophyType type;
    private String earnedRate;
    private Instant earnedAt;
}
