package com.gundomrays.philebot.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class EmbedVideoChecker {

    private static final Logger log = LoggerFactory.getLogger(EmbedVideoChecker.class);

    private static final String TELEGRAM_USER_AGENT = "TelegramBot (like TwitterBot)";

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final int MAX_PAGE_SIZE = 512 * 1024;

    private static final String VIDEO_META_NAME = "(?:property|name)=\"(?:og:video(?::url|:secure_url)?|twitter:player:stream)\"";

    private static final String VIDEO_META_CONTENT = "content=\"https?://[^\"]+\"";

    private static final Pattern VIDEO_META = Pattern.compile(
            "<meta[^>]+" + VIDEO_META_NAME + "[^>]*" + VIDEO_META_CONTENT
                    + "|<meta[^>]+" + VIDEO_META_CONTENT + "[^>]*" + VIDEO_META_NAME,
            Pattern.CASE_INSENSITIVE
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(TIMEOUT)
            .build();

    public boolean hasVideo(final String url) {
        try {
            final HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("User-Agent", TELEGRAM_USER_AGENT)
                    .GET()
                    .build();
            final HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() >= 400) {
                    log.info("No video at {}, status: {}", url, response.statusCode());
                    return false;
                }
                final String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
                if (contentType.startsWith("video/")) {
                    return true;
                }
                if (!contentType.contains("html")) {
                    log.info("No video at {}, content type: {}", url, contentType);
                    return false;
                }
                return containsVideoMeta(new String(body.readNBytes(MAX_PAGE_SIZE), StandardCharsets.UTF_8));
            }
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Cannot check video at {}: {}", url, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    static boolean containsVideoMeta(final String page) {
        return page != null && VIDEO_META.matcher(page).find();
    }

}
