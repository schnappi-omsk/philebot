package com.gundomrays.philebot.psn.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsnProfileLookup {

    private Profile profile;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {

        private String accountId;

        private String onlineId;

        private String currentOnlineId;

    }

}
