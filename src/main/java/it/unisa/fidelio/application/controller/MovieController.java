package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.FilmService;
import it.unisa.fidelio.application.RecensioneService;
import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.MovieDetailsView;
import it.unisa.fidelio.storage.Utente;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final FilmService filmService;
    private final RecensioneService recensioneService;
    private final UtenteService utenteService;

    public MovieController(FilmService filmService,
                           RecensioneService recensioneService,
                           UtenteService utenteService) {
        this.filmService = filmService;
        this.recensioneService = recensioneService;
        this.utenteService = utenteService;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") Long id, Model model,
                         @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        MovieDetailsView movie = filmService.getMovieDetailsView(id);
        model.addAttribute("movie", movie);
        model.addAttribute("popularReviews", recensioneService.getTutteLeRecensioni(id));

        Utente utenteLoggato = null;
        Set<Integer> likeGiaFatti = Set.of();
        Set<Integer> dislikeGiaFatti = Set.of();
        Set<Integer> segnalazioniGiaFatte = Set.of();

        if (userDetails != null) {
            // Recupera utente tramite UtenteService
            utenteLoggato = utenteService.findByEmail(userDetails.getUsername());

            if (utenteLoggato != null) {
                int utenteId = utenteLoggato.getId();

                // Recupera i dati tramite RecensioneService
                likeGiaFatti = recensioneService.getLikeGiaFatti(utenteId);
                dislikeGiaFatti = recensioneService.getDislikeGiaFatti(utenteId);
                segnalazioniGiaFatte = recensioneService.getSegnalazioniGiaFatte(utenteId);
            }
        }

        model.addAttribute("likeGiaFatti", likeGiaFatti);
        model.addAttribute("dislikeGiaFatti", dislikeGiaFatti);
        model.addAttribute("segnalazioniGiaFatte", segnalazioniGiaFatte);
        model.addAttribute("utenteLoggato", utenteLoggato);

        return "details";
    }
}