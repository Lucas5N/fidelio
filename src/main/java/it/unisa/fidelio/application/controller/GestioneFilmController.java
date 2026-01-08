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
// Mapping di classe esteso per includere /filmlist e risolvere il 404 sulla lista.
@RequestMapping({"/film", "/search", "/filmlist"})
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

    // ==========================================
    // NUOVO MAPPING: LISTA FILM POPOLARI (Mappato su /filmlist)
    // ==========================================
    /**
     * Cattura il percorso base "/filmlist" (ereditato dal RequestMapping di classe).
     */
    @GetMapping
    public String filmListPage(Model model) {
        // Questa @GetMapping senza path specifico ora cattura /filmlist
        List<FilmCardDto> films = filmService.getFilmPopolari(1);

        model.addAttribute("filmsList", films);

        // RESTITUISCE LA VISTA CORRETTA: templates/film/filmlist.html
        return "film/filmlist";
    }

    // ==========================================
    // 0. RICERCA DA NAVBAR/RICERCA FILTRATA (Mappato su /search)
    // ==========================================
    /**
     * Mappato esplicitamente su /search per non entrare in conflitto con /filmlist.
     */
    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String query,
                         @RequestParam(value = "genere", required = false) String genere,
                         @RequestParam(value = "anno", required = false) String anno,
                         Model model) {

        String cleanedQuery = (query != null) ? query.trim() : "";
        String cleanedGenere = (genere != null && !genere.isEmpty()) ? genere : null;
        String cleanedAnno = (anno != null && !anno.isEmpty()) ? anno : null;

        List<?> results = Collections.emptyList();

        // 1. CASO: RICERCA FILTRATA ATTIVA (Con o Senza query di testo)
        if (cleanedGenere != null || cleanedAnno != null) {
            try {
                results = filmService.ricercaFiltrata(
                        cleanedQuery,
                        cleanedGenere != null ? cleanedGenere : "",
                        cleanedAnno != null ? cleanedAnno : ""
                );
            } catch (Exception e) {
                System.err.println("Errore durante la ricerca filtrata: " + e.getMessage());
            }
        }
        // 2. CASO: SOLO RICERCA TESTUALE (Nessun filtro attivo)
        else if (!cleanedQuery.isEmpty()) {
            try {
                results = filmService.ricercaFilm(cleanedQuery, 1);
            } catch (Exception e) {
                System.err.println("Errore durante la ricerca TMDB: " + e.getMessage());
            }
        }

        model.addAttribute("query", cleanedQuery);
        model.addAttribute("genere", cleanedGenere);
        model.addAttribute("anno", cleanedAnno);
        model.addAttribute("results", results);

        return "search";
    }

    // ==========================================
    // 1. API RICERCA FILM (Mappato su /film/api/search)
    // ==========================================

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

    // ==========================================
    // 2. API RICERCA FILTRATA (Mappato su /film/api/search/filter)
    // ==========================================

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

    // ==========================================
    // 3. API CREAZIONE LISTA (Mappato su /film/api/lista)
    // ==========================================

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

    // ==========================================
    // 4. VISUALIZZAZIONE DETTAGLI (Mappato su /film/{filmId})
    // ==========================================

    /**
     * CORREZIONE APPLICATA: Si mappa su /{filmId}, che combinato con il prefisso di classe "/film"
     * risolve correttamente l'URL /film/{filmId}.
     */
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