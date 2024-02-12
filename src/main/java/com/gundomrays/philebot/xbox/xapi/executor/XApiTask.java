package com.gundomrays.philebot.xbox.xapi.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class XApiTask {

    private Integer permits;
    private Runnable task;

}
