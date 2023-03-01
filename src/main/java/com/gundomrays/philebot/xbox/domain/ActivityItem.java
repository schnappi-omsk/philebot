package com.gundomrays.philebot.xbox.domain;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Getter
@Setter
@RedisHash("XboxActivityItem")
public class ActivityItem implements Comparable<ActivityItem> {

    @Id
    @Indexed
    private String userXuid;

    @Indexed
    private String contentTitle;

    @Indexed
    private String titleId;

    private String achievementName;
    private String achievementDescription;
    private String achievementIcon;

    private Integer gamerscore;
    private Integer rarityPercentage;

    @NonNull
    private LocalDateTime date;

    @Override
    public int compareTo(ActivityItem o) {
        return date.compareTo(o.date);
    }
}
