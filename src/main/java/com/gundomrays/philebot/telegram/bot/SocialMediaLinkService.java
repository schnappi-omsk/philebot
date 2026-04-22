package com.gundomrays.philebot.telegram.bot;

import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class SocialMediaLinkService {

    private final Map<String, String> urlMap = Map.of(
            "x.com", "fxtwitter.com",
            "instagram.com", "kkclip.com",
            "tiktok.com", "kktiktok.com"
    );

    public String mediaLink(String url) {
        for (String key : urlMap.keySet()) {
            if (url.contains(key)) {
                return url.replace(key, urlMap.get(key));
            }
        }
        return url;
    }

    public boolean isSocialMediaLink(String url) {
        if (StringUtils.isNotEmpty(url) && isValidUrl(url)) {
            for (String key : urlMap.keySet()) {
                if (url.contains(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

}
