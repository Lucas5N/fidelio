package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.RecensioneService;
import it.unisa.fidelio.application.TmdbClient;
import it.unisa.fidelio.storage.Film;
import it.unisa.fidelio.storage.FilmRepository;
import it.unisa.fidelio.storage.Recensione;
import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/film/{filmId}/recensioni")
public class GestioneRecensioneController {

    private final RecensioneService recensioneService;
    private final FilmRepository filmRepository;
    private final UtenteRepository utenteRepository;
    private final TmdbClient tmdbClient;

    public GestioneRecensioneController(RecensioneService recensioneService,
                                        FilmRepository filmRepository,
                                        UtenteRepository utenteRepository,
                                        TmdbClient tmdbClient) {
        this.recensioneService = recensioneService;
        this.filmRepository = filmRepository;
        this.utenteRepository = utenteRepository;
        this.tmdbClient = tmdbClient;
    }

    // Lista recensioni (usa DTO)
    @GetMapping
    public String elencoRecensioni(@PathVariable Long filmId, Model model) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Film non trovato"));

        model.addAttribute("film", film);
        model.addAttribute("recensioni", recensioneService.getRecensioniDtoPerFilm(film));
        model.addAttribute("movieDetails", tmdbClient.getMovieDetails(filmId));
        model.addAttribute("tmdbId", filmId);  // per link TMDB
        return "recensioni/lista";
    }

    // Form nuova recensione
    @GetMapping("/nuova")
    public String mostraFormNuovaRecensione(@PathVariable Long filmId, Model model) {
        Film film = filmRepository.findById(filmId).orElseThrow();

        model.addAttribute("film", film);
        model.addAttribute("movieDetails", tmdbClient.getMovieDetails(filmId));
        model.addAttribute("recensioneForm", new Recensione());
        model.addAttribute("tmdbId", filmId);
        return "recensioni/nuova";
    }

    // Salva recensione
    @PostMapping("/nuova")
    public String salvaRecensione(@PathVariable Long filmId,
                                  @Valid @ModelAttribute("recensioneForm") Recensione recensioneForm,
                                  BindingResult result,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        if (result.hasErrors()) {
            Film film = filmRepository.findById(filmId).orElseThrow();
            model.addAttribute("film", film);
            model.addAttribute("movieDetails", tmdbClient.getMovieDetails(filmId));
            model.addAttribute("tmdbId", filmId);
            return "recensioni/nuova";
        }

        Utente autore = utenteRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Film film = filmRepository.findById(filmId).orElseThrow();

        recensioneService.scriviRecensione(
                autore,
                film,
                recensioneForm.getTesto(),
                recensioneForm.getVoto(),
                recensioneForm.getSpoilerAlert() != null && recensioneForm.getSpoilerAlert()
        );

        return "redirect:/film/" + filmId + "/recensioni";
    }

    // Like
    @PostMapping("/{recensioneId}/like")
    public String like(@PathVariable Long filmId, @PathVariable Integer recensioneId) {
        recensioneService.aggiungiLike(recensioneId);
        return "redirect:/film/" + filmId + "/recensioni";
    }

    // Dislike
    @PostMapping("/{recensioneId}/dislike")
    public String dislike(@PathVariable Long filmId, @PathVariable Integer recensioneId) {
        recensioneService.aggiungiDislike(recensioneId);
        return "redirect:/film/" + filmId + "/recensioni";
    }
}