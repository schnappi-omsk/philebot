package com.gundomrays.philebot.web;

import com.gundomrays.philebot.web.xbox.XboxTitleUIService;
import com.gundomrays.philebot.xbox.domain.Title;
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

    private final XboxTitleUIService xboxTitleUIService;

    public AchievementController(XboxTitleUIService xboxTitleUIService) {
        this.xboxTitleUIService = xboxTitleUIService;
    }

    @RequestMapping(value = "/xbox/{achievement}/{description}/{pts}/{rarity}", method = RequestMethod.GET)
    public String achievementPage(
            @PathVariable("achievement") String achievement,
            @PathVariable("description") String description,
            @PathVariable("pts") Integer score,
            @PathVariable("rarity") Integer rarity,
            @RequestParam("imgUrl") String imgUrl,
            @RequestParam("seed") String seed,
            Model model
    ) {
        final String achievementStats = String.format("%d pts., %d%%", score, rarity);

        model.addAttribute("achievement", URLDecoder.decode(achievement, StandardCharsets.UTF_8));
        model.addAttribute("achievementInfo", URLDecoder.decode(description, StandardCharsets.UTF_8));
        model.addAttribute("achievementStats", achievementStats);
        model.addAttribute("imgUrl", URLDecoder.decode(imgUrl, StandardCharsets.UTF_8));
        model.addAttribute("seed", seed);

        return "achievement";
    }

    @RequestMapping(value = "/xbox/game/{titleId}", method = RequestMethod.GET)
    public String gameStats(@PathVariable("titleId") String titleId, Model model) {
        final Title title = xboxTitleUIService.title(titleId);

        model.addAttribute("alphabet", xboxTitleUIService.alphabet());
        model.addAttribute("title", title);
        model.addAttribute("statistics", xboxTitleUIService.stats(title));

        return "game_stats";
    }

    @RequestMapping("/")
    public String home() {
        return "index";
    }

}
