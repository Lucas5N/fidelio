package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.FilmService;
import it.unisa.fidelio.application.RecensioneService;
import it.unisa.fidelio.application.TmdbClient;
import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.FilmCardDto;
import it.unisa.fidelio.presentation.MovieDetailsView;
import it.unisa.fidelio.storage.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/film")
public class GestioneFilmController {

    private final FilmService filmService;
    private final RecensioneService recensioneService;
    private final UtenteService utenteService;
    private final TmdbClient tmdbClient;

    public GestioneFilmController(FilmService filmService, RecensioneService recensioneService, UtenteService utenteService, TmdbClient tmdbClient) {
        this.filmService = filmService;
        this.recensioneService = recensioneService;
        this.utenteService = utenteService;
        this.tmdbClient = tmdbClient;
    }

    // API Ricerca Film
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<?> ricercaFilm(@RequestParam(required = false) String query,
                                         @RequestParam(defaultValue = "1") int page) {
        if (query == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!query.matches("[a-zA-Z0-9 ]+")) {
            return ResponseEntity.badRequest().body("Errato: Nome film errato");
        }

        List<FilmCardDto> risultati = filmService.ricercaFilm(query, page);
        return ResponseEntity.ok(risultati);
    }

    // API Ricerca Filtrata
    @GetMapping("/api/search/filter")
    @ResponseBody
    public ResponseEntity<?> ricercaFiltrata(@RequestParam(required = false) String genere,
                                             @RequestParam(required = false) String anno) {
        if (genere == null && anno == null) {
            return ResponseEntity.badRequest().build();
        }

        String finalGenere = genere != null ? genere : "";
        String finalAnno = anno != null ? anno : "";

        if (!finalGenere.isEmpty() && !finalGenere.matches("[a-zA-Z ]+")) {
            return ResponseEntity.badRequest().build();
        }

        if (!finalAnno.isEmpty() && !finalAnno.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body("Errato: Anno errato");
        }

        List<FilmCardDto> risultati = filmService.ricercaFiltrata("", finalGenere, finalAnno);

        return ResponseEntity.ok(risultati);
    }

    // API Creazione Lista
    @PostMapping("/api/lista")
    @ResponseBody
    public ResponseEntity<?> creaLista(@RequestParam(required = false) String titolo,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (titolo == null) {
            return ResponseEntity.badRequest().build();
        }

        if (titolo.length() < 3 || titolo.length() > 30) {
            return ResponseEntity.badRequest().body("Errato: Titolo troppo corto o troppo lungo");
        }

        Utente utente = utenteService.findByEmail(userDetails.getUsername());
        filmService.creaLista(titolo, utente.getId());

        return ResponseEntity.ok("Lista creata con successo");
    }

    // Visualizzazione Dettagli
    @GetMapping("/{filmId}")
    public String visualizzaDettagli(@PathVariable Long filmId,
                                     Model model,
                                     Principal principal) {

        // 1. Recupero dati Film
        MovieDetailsView movie = filmService.getMovieDetailsView(filmId);

        if (movie == null) {
            return "redirect:/";
        }

        // Popolamento Model BASE
        model.addAttribute("movie", movie);
        model.addAttribute("popularReviews", recensioneService.getTutteLeRecensioni(filmId));

        // 2. Gestione Loggato vs Guest
        if (principal != null) {
            Utente utente = utenteService.findByEmail(principal.getName());

            model.addAttribute("utenteLoggato", utente);
            model.addAttribute("isAdmin", utente.getAmministratore());
            model.addAttribute("likeGiaFatti", recensioneService.getLikeGiaFatti(utente.getId()));
            model.addAttribute("dislikeGiaFatti", recensioneService.getDislikeGiaFatti(utente.getId()));
            model.addAttribute("segnalazioniGiaFatte", recensioneService.getSegnalazioniGiaFatte(utente.getId()));
            model.addAttribute("tutteLeSegnalazioni", Collections.emptySet());

        } else {
            model.addAttribute("utenteLoggato", null);
            model.addAttribute("isAdmin", false);
            model.addAttribute("likeGiaFatti", Collections.emptySet());
            model.addAttribute("dislikeGiaFatti", Collections.emptySet());
            model.addAttribute("segnalazioniGiaFatte", Collections.emptySet());
            model.addAttribute("tutteLeSegnalazioni", Collections.emptySet());
        }

        return "film/details";
    }
}