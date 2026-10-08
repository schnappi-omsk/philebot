package com.gundomrays.philebot.telegram.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SocialMediaLinkServiceTest {

    private EmbedVideoChecker mockEmbedVideoChecker;

    private SocialMediaLinkService socialMediaLinkService;

    @BeforeEach
    public void setUp() {
        mockEmbedVideoChecker = Mockito.mock(EmbedVideoChecker.class);
        Mockito.when(mockEmbedVideoChecker.hasVideo(Mockito.anyString())).thenReturn(true);

        socialMediaLinkService = new SocialMediaLinkService(mockEmbedVideoChecker);
    }

    @Test
    public void testLinkSeparatedBySpaceIsReplaced() {
        assertEquals("https://fxtwitter.com/user/status/1",
                socialMediaLinkService.hasLink("look https://x.com/user/status/1"));
    }

    @Test
    public void testLinkOnNewLineIsReplaced() {
        assertEquals("https://fxtwitter.com/user/status/1",
                socialMediaLinkService.hasLink("look\nhttps://x.com/user/status/1\nnice"));
    }

    @Test
    public void testLinkWrappedInPunctuationIsReplaced() {
        assertEquals("https://fxtwitter.com/user/status/1",
                socialMediaLinkService.hasLink("(https://x.com/user/status/1),"));
    }

    @Test
    public void testUpperCaseHostIsReplaced() {
        assertEquals("https://fxtwitter.com/user/status/1",
                socialMediaLinkService.hasLink("https://X.com/user/status/1"));
    }

    @Test
    public void testSubdomainIsReplaced() {
        assertEquals("https://tnktok.com/ZMabc/",
                socialMediaLinkService.hasLink("https://vm.tiktok.com/ZMabc/"));
        assertEquals("https://fxtwitter.com/user/status/1",
                socialMediaLinkService.hasLink("https://mobile.twitter.com/user/status/1"));
        assertEquals("https://instagramfix.com/reel/abc/",
                socialMediaLinkService.hasLink("https://www.instagram.com/reel/abc/"));
    }

    @Test
    public void testNextServiceIsUsedWhenFirstHasNoVideo() {
        Mockito.when(mockEmbedVideoChecker.hasVideo("https://tnktok.com/@user/video/1")).thenReturn(false);

        assertEquals("https://tiktokfix.com/@user/video/1",
                socialMediaLinkService.hasLink("https://www.tiktok.com/@user/video/1"));
    }

    @Test
    public void testFirstServiceIsUsedWhenNoneHasVideo() {
        Mockito.when(mockEmbedVideoChecker.hasVideo(Mockito.anyString())).thenReturn(false);

        assertEquals("https://tnktok.com/@user/video/1",
                socialMediaLinkService.hasLink("https://www.tiktok.com/@user/video/1"));
        Mockito.verify(mockEmbedVideoChecker, Mockito.times(3)).hasVideo(Mockito.anyString());
    }

    @Test
    public void testSingleServiceIsNotChecked() {
        assertEquals("https://fxtwitter.com/user/status/1",
                socialMediaLinkService.hasLink("https://twitter.com/user/status/1"));
        Mockito.verifyNoInteractions(mockEmbedVideoChecker);
    }

    @Test
    public void testOtherLinksAreNotReplaced() {
        assertNull(socialMediaLinkService.hasLink("https://example.com/x.com"));
        assertNull(socialMediaLinkService.hasLink("https://fxtwitter.com/user/status/1"));
        assertNull(socialMediaLinkService.hasLink("https://notx.com/user/status/1"));
    }

    @Test
    public void testMessageWithoutLinks() {
        assertNull(socialMediaLinkService.hasLink("just text"));
        assertNull(socialMediaLinkService.hasLink(""));
        assertNull(socialMediaLinkService.hasLink(null));
    }
}
