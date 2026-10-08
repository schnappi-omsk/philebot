package com.gundomrays.philebot.psn.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PsnTrophyType {

    BRONZE("bronze", "Bronze", "🥉"),
    SILVER("silver", "Silver", "🥈"),
    GOLD("gold", "Gold", "🥇"),
    PLATINUM("platinum", "Platinum", "🏆");

    private final String value;

    private final String displayName;

    private final String emoji;

    PsnTrophyType(String value, String displayName, String emoji) {
        this.value = value;
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public static PsnTrophyType fromValue(final String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

}
