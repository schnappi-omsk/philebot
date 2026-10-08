package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.telegram.exception.TelegramException;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SocialMediaLinkService {

    private static final Logger log = LoggerFactory.getLogger(SocialMediaLinkService.class);

    private static final String LEADING_PUNCTUATION = "([{<\"'«";

    private static final String TRAILING_PUNCTUATION = ".,!?;:)]}>\"'»";

    // Services are checked in order, the first one returning a video wins
    private final Map<String, List<String>> urlMap = Map.of(
            "x.com", List.of("fxtwitter.com"),
            "twitter.com", List.of("fxtwitter.com"),
            "instagram.com", List.of("instagramfix.com", "kkclip.com"),
            "tiktok.com", List.of("tnktok.com", "tiktokfix.com", "kktiktok.com")
    );

    private final EmbedVideoChecker embedVideoChecker;

    public SocialMediaLinkService(EmbedVideoChecker embedVideoChecker) {
        this.embedVideoChecker = embedVideoChecker;
    }

    public String hasLink(String messageText) {
        if (StringUtils.isNotEmpty(messageText)) {
            String[] parts = messageText.split("\\s+");
            for (final String part : parts) {
                final String url = trimPunctuation(part);
                if (isValidUrl(url)) {
                    final String link = mediaLink(url);
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
            List<String> substitutes = substitutes(host);
            if (!substitutes.isEmpty()) {
                return firstWithVideo(url, host, substitutes);
            }
        } catch (URISyntaxException e) {
            throw new TelegramException(e.getMessage(), e);
        }
        return null;
    }

    private List<String> substitutes(final String host) {
        if (StringUtils.isEmpty(host)) {
            return List.of();
        }
        final String normalizedHost = host.toLowerCase(Locale.ROOT);
        return urlMap.entrySet()
                .stream()
                .filter(entry -> normalizedHost.equals(entry.getKey()) || normalizedHost.endsWith("." + entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(List.of());
    }

    private String firstWithVideo(final String url, final String host, final List<String> substitutes) {
        if (substitutes.size() > 1) {
            for (final String substitute : substitutes) {
                final String candidate = replaceHost(url, host, substitute);
                if (embedVideoChecker.hasVideo(candidate)) {
                    log.info("Video for {} found at {}", url, substitute);
                    return candidate;
                }
            }
            log.info("No video for {} found, falling back to {}", url, substitutes.getFirst());
        }
        return replaceHost(url, host, substitutes.getFirst());
    }

    private String replaceHost(final String url, final String host, final String substitute) {
        return url.replaceFirst(Pattern.quote(host), Matcher.quoteReplacement(substitute));
    }

    private String trimPunctuation(final String value) {
        int start = 0;
        int end = value.length();
        while (start < end && LEADING_PUNCTUATION.indexOf(value.charAt(start)) >= 0) {
            start++;
        }
        while (end > start && TRAILING_PUNCTUATION.indexOf(value.charAt(end - 1)) >= 0) {
            end--;
        }
        return value.substring(start, end);
    }

    private boolean isValidUrl(String url) {
        try {
            new URI(url).toURL();
            return true;
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

}
