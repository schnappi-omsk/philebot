package com.gundomrays.philebot.telegram.bot.clown.domain;

import lombok.Getter;

@Getter
public enum Wokeness {
    NO_WOKE(1, "Норм игра, можно не рваться."),
    SLIGHTLY_WOKE(0, "Есть чутка повесточки, но не критично"),
    WOKE(-1, "Повесточное говнище! 0/10");

    private final int wokeness;
    private final String comment;

    Wokeness(int wokeness, String comment) {
        this.wokeness = wokeness;
        this.comment = comment;
    }

    public static Wokeness isWoke(int value) {
        for (Wokeness element : Wokeness.values()) {
            if (element.wokeness == value) {
                return element;
            }
        }
        return null;
    }

}
