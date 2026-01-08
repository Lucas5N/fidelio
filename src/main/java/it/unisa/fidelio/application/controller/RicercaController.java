package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.FilmService;
import it.unisa.fidelio.presentation.FilmCardDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/search")
public class RicercaController {

    private final FilmService filmService;

    public RicercaController(FilmService filmService) {
        this.filmService = filmService;
    }

    /**
     * Cattura il percorso base "/search" grazie all'annotation di classe.
     */
    @GetMapping
    public String search(@RequestParam(value = "q", required = false) String query,
                         @RequestParam(value = "genere", required = false) String genere,
                         @RequestParam(value = "anno", required = false) String anno,
                         Model model) {

        String cleanedQuery = (query != null) ? query.trim() : "";
        String cleanedGenere = (genere != null && !genere.isEmpty()) ? genere : null;
        String cleanedAnno = (anno != null && !anno.isEmpty()) ? anno : null;

        List<?> results = Collections.emptyList();

        // 1. CASO: RICERCA FILTRATA ATTIVA
        if (cleanedGenere != null || cleanedAnno != null) {
            // ... (Logica omessa per brevità, usa la tua)
            results = filmService.ricercaFiltrata(
                    cleanedQuery,
                    cleanedGenere != null ? cleanedGenere : "",
                    cleanedAnno != null ? cleanedAnno : ""
            );
        }
        // 2. CASO: SOLO RICERCA TESTUALE
        else if (!cleanedQuery.isEmpty()) {
            // ... (Logica omessa per brevità, usa la tua)
            results = filmService.ricercaFilm(cleanedQuery, 1);
        }

        model.addAttribute("query", cleanedQuery);
        model.addAttribute("genere", cleanedGenere);
        model.addAttribute("anno", cleanedAnno);
        model.addAttribute("results", results);

        return "search";
    }
}