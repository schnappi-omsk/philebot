package com.gundomrays.philebot.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/")
public class AchievementController {
    @RequestMapping(value = "/xbox/{achievement}/{description}/{pts}/{rarity}", method = RequestMethod.GET)
    public String achievementPage(
            @PathVariable("achievement") String achievement,
            @PathVariable("description") String description,
            @PathVariable("pts") Integer score,
            @PathVariable("rarity") Integer rarity,
            @RequestParam("imgUrl") String imgUrl,
            Model model
    ) {
        final String achievementStats = String.format("%d pts., %d%%", score, rarity);

        model.addAttribute("achievement", URLDecoder.decode(achievement, StandardCharsets.UTF_8));
        model.addAttribute("achievementInfo", URLDecoder.decode(description, StandardCharsets.UTF_8));
        model.addAttribute("achievementStats", achievementStats);
        model.addAttribute("imgUrl", URLDecoder.decode(imgUrl, StandardCharsets.UTF_8));

        return "achievement";
    }

    @RequestMapping("/")
    public String home() {
        return "index";
    }

}
