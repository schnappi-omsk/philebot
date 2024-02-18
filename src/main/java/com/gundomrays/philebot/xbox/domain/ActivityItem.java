package com.gundomrays.philebot.xbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
public class ActivityItem implements Comparable<ActivityItem> {

    @Id
    private String userXuid;

    @Id
    private Integer activityId;

    private String contentTitle;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ActivityItem that)) {
            return false;
        }

        return Objects.equals(titleId, that.titleId) &&
                Objects.equals(achievementName, that.achievementName) &&
                Objects.equals(achievementDescription, that.achievementDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titleId, achievementName, achievementDescription);
    }
}
