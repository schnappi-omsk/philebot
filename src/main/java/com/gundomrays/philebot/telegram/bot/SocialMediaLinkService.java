package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.telegram.exception.TelegramException;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

@Service
public class SocialMediaLinkService {

    private final Map<String, String> urlMap = Map.of(
            "x.com", "fxtwitter.com",
            "twitter.com", "fxtwitter.com",
            "instagram.com", "kkclip.com",
            "tiktok.com", "kktiktok.com"
    );

    public String hasLink(String messageText) {
        if (StringUtils.isNotEmpty(messageText)) {
            String[] parts = messageText.split(" ");
            for (final String part : parts) {
                if (isValidUrl(part)) {
                    final String link = mediaLink(part);
                    if (link != null) {
                        return link;
                    }
                }
            }
        }
        return null;
    }

    private String mediaLink(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String key = host;
            if (StringUtils.isNotEmpty(host) && host.startsWith("www.")) {
                key = host.replaceFirst("www.", "");
            }
            String substitute = urlMap.get(key);
            if (StringUtils.isNotEmpty(substitute)) {
                return url.replaceFirst(host, substitute);
            }
        } catch (URISyntaxException e) {
            throw new TelegramException(e.getMessage(), e);
        }
        return null;
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
