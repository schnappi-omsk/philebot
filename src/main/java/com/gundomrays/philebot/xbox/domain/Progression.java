package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Progression {
    private LocalDateTime timeUnlocked;
}
