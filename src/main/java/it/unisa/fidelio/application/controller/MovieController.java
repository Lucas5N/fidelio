package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.FilmService;
import it.unisa.fidelio.application.RecensioneService;
import it.unisa.fidelio.presentation.MovieDetailsView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final FilmService filmService;
    private final RecensioneService recensioneService;

    public MovieController(FilmService filmService, RecensioneService recensioneService) {
        this.filmService = filmService;
        this.recensioneService = recensioneService;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        MovieDetailsView movie = filmService.getMovieDetailsView(id);
        model.addAttribute("movie", movie);

        model.addAttribute("popularReviews", recensioneService.getPopularReviewsFromTmdb(id, 6));

        return "details"; // templates/movies/details.html
    }
}