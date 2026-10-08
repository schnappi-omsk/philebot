package com.gundomrays.philebot.web;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AchievementControllerTest {

    private final AchievementController controller = new AchievementController();

    @Test
    public void testTrophyPage() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.trophyPage("Hidden <Hero>", "Find the secret & win.", "Gold", "3.7",
                "https://image.example.com/2.png", "seed", model);

        assertEquals("achievement", view);
        assertEquals("Hidden <Hero>", model.get("achievement"));
        assertEquals("Find the secret & win.", model.get("achievementInfo"));
        assertEquals("Gold, 3.7%", model.get("achievementStats"));
        assertEquals("https://image.example.com/2.png", model.get("imgUrl"));
    }

    @Test
    public void testTrophyPageWithoutRarity() {
        ExtendedModelMap model = new ExtendedModelMap();

        controller.trophyPage("Trophy", "", "Bronze", "", "", "", model);

        assertEquals("Bronze", model.get("achievementStats"));
    }
}
