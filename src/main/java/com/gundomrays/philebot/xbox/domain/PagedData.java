package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagedData {

    @Setter
    private String continuationToken;

    private String[] values;

}
