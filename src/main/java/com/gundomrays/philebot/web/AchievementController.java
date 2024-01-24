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
    @RequestMapping(value = "/xbox/{game}/{achievement}/{description}/{pts}/{rarity}", method = RequestMethod.GET)
    public String achievementPage(
            @PathVariable("game") String game,
            @PathVariable("achievement") String achievement,
            @PathVariable("description") String description,
            @PathVariable("pts") Integer score,
            @PathVariable("rarity") Integer rarity,
            @RequestParam("imgUrl") String imgUrl,
            Model model
    ) {
        final String achievementInfo = String.format("%s - %s (%d pts., %d%%)", achievement, description, score, rarity);

        model.addAttribute("game", URLDecoder.decode(game, StandardCharsets.UTF_8));
        model.addAttribute("achievement", URLDecoder.decode(achievementInfo, StandardCharsets.UTF_8));
        model.addAttribute("imgUrl", imgUrl);

        return "achievement";
    }

    @RequestMapping("/")
    public String home() {
        return "index";
    }

}