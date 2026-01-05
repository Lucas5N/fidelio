package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.FilmService;
import it.unisa.fidelio.presentation.FilmCardDto;
import it.unisa.fidelio.presentation.GestioneFilmRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/film")
@CrossOrigin(origins = "*")
public class GestioneFilmController {

    private final FilmService filmService;

    public GestioneFilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    /**
     * Ricerca film su TMDB
     * GET /api/film/search?query=Inception&page=1
     */
    @GetMapping("/search")
    public ResponseEntity<List<FilmCardDto>> ricercaFilm(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        List<FilmCardDto> risultati = filmService.ricercaFilm(query, page);
        return ResponseEntity.ok(risultati);
    }

    /**
     * Aggiungi o aggiorna un film nella lista personale dell'utente
     * POST /api/film/lista
     * Body: { "tmdbId": 27205, "stato": "VISTO", "dataVisione": "2025-12-25"}
     */
    @PostMapping("/lista")
    public ResponseEntity<String> gestisciFilmNellaLista(
            @RequestBody GestioneFilmRequestDTO request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Utente non autenticato");
        }
        filmService.gestisciFilmNellaLista(principal.getName(), request);
        return ResponseEntity.ok("Film aggiunto/aggiornato nella tua lista con successo");
    }

    /**
     * Recupera tutti i film dell'utente (da tutte le 3 liste speciali)
     * GET /api/film/miei
     */
    @GetMapping("/miei")
    public ResponseEntity<List<FilmCardDto>> mieiFilm(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(null);
        }
        List<FilmCardDto> mieiFilm = filmService.getMieiFilm(principal.getName());
        return ResponseEntity.ok(mieiFilm);
    }

    /**
     * Film raccomandati basati sui generi dei film visti
     * GET /api/film/raccomandati
     */
    @GetMapping("/raccomandati")
    public ResponseEntity<List<FilmCardDto>> filmRaccomandati(Principal principal) {
        if (principal == null) {
            // Se non loggato, restituisci popolari
            List<FilmCardDto> popolari = filmService.getFilmPopolari(1);
            return ResponseEntity.ok(popolari);
        }

        List<FilmCardDto> raccomandati = filmService.getFilmRaccomandati(principal.getName());
        return ResponseEntity.ok(raccomandati);
    }
}