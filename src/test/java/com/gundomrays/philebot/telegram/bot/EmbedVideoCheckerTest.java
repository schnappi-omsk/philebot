package com.gundomrays.philebot.telegram.bot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmbedVideoCheckerTest {

    @Test
    public void testOpenGraphVideoIsDetected() {
        assertTrue(EmbedVideoChecker.containsVideoMeta(
                "<head><meta property=\"og:video\" content=\"https://cdn.example.com/1.mp4\"/></head>"));
    }

    @Test
    public void testVideoWithContentBeforePropertyIsDetected() {
        assertTrue(EmbedVideoChecker.containsVideoMeta(
                "<meta content=\"https://cdn.example.com/1.mp4\" property=\"og:video:secure_url\">"));
    }

    @Test
    public void testTwitterPlayerStreamIsDetected() {
        assertTrue(EmbedVideoChecker.containsVideoMeta(
                "<meta name=\"twitter:player:stream\" content=\"https://cdn.example.com/1.mp4\">"));
    }

    @Test
    public void testVideoTypeWithoutVideoUrlIsNotDetected() {
        assertFalse(EmbedVideoChecker.containsVideoMeta(
                "<meta property=\"og:video:type\" content=\"video/mp4\"/>"));
    }

    @Test
    public void testErrorPageIsNotDetected() {
        assertFalse(EmbedVideoChecker.containsVideoMeta(
                "<meta property=\"og:title\" content=\"Instagramfix\"/>"
                        + "<meta property=\"og:description\" content=\"Sorry, this post couldn't be loaded.\"/>"));
        assertFalse(EmbedVideoChecker.containsVideoMeta(null));
    }
}
