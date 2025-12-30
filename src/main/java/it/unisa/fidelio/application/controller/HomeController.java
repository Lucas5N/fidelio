package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("popularFilms", homeService.getPopularCards(4));
        model.addAttribute("newReleases", homeService.getNewReleaseCards(4));
        return "home";
    }
}


