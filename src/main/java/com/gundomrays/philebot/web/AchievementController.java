package com.gundomrays.philebot.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class AchievementController {
    @RequestMapping(value = "/xbox/{username}/{game}/{achievement}/{description}/{pts}/{rarity}", method = RequestMethod.GET)
    public String achievementPage(
            @PathVariable("username") String username,
            @PathVariable("game") String game,
            @PathVariable("achievement") String achievement,
            @PathVariable("description") String description,
            @PathVariable("pts") Integer score,
            @PathVariable("rarity") Integer rarity,
            @RequestParam("imgUrl") String imgUrl,
            Model model
    ) {
        final String achievementInfo = String.format("%s -%s (%d pts., %d%%)", achievement, description, score, rarity);

        model.addAttribute("username", username);
        model.addAttribute("game", game);
        model.addAttribute("achievement", achievementInfo);
        model.addAttribute("imgUrl", imgUrl);

        return "achievement";
    }

    @RequestMapping("/")
    public String home() {
        return "index";
    }

}
