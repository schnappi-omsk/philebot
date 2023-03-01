package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@RedisHash("XboxTitleHistory")
public class TitleHistory {

    @Id
    private String xuid;
    private List<Title> titles = new LinkedList<>();

}
