package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.FilmService;
import it.unisa.fidelio.presentation.FilmCardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}