package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.RecensioneService;
import it.unisa.fidelio.application.TmdbClient;
import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.storage.Utente;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/film/{filmId}/recensioni")
public class GestioneRecensioneController {

    private final RecensioneService recensioneService;
    private final UtenteService utenteService;
    private final TmdbClient tmdbClient;

    public GestioneRecensioneController(RecensioneService recensioneService,
                                        UtenteService utenteService,
                                        TmdbClient tmdbClient) {
        this.recensioneService = recensioneService;
        this.utenteService = utenteService;
        this.tmdbClient = tmdbClient;
    }

    @GetMapping
    public String elencoRecensioni(@PathVariable Long filmId, Model model) {
        model.addAttribute("recensioni", recensioneService.getTutteLeRecensioni(filmId));
        model.addAttribute("movieDetails", tmdbClient.getMovieDetails(filmId));
        return "recensioni/lista";
    }

    @PostMapping("/nuova")
    public String salvaRecensione(@PathVariable Long filmId,
                                  @RequestParam String testo,
                                  @RequestParam Double voto,
                                  @RequestParam(required = false, defaultValue = "false") boolean spoilerAlert,
                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        Utente utenteLoggato = utenteService.findByEmail(userDetails.getUsername());
        if (utenteLoggato == null) {
            throw new IllegalStateException("Utente non trovato");
        }

        recensioneService.scriviRecensione(utenteLoggato, filmId, testo, voto, spoilerAlert);
        return "redirect:/movies/" + filmId;
    }

    @PostMapping("/{recensioneId}/like")
    public String like(@PathVariable Long filmId,
                       @PathVariable Integer recensioneId,
                       @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        Utente utenteLoggato = utenteService.findByEmail(userDetails.getUsername());
        if (utenteLoggato == null) {
            throw new IllegalStateException("Utente non trovato");
        }

        recensioneService.aggiungiLike(recensioneId, utenteLoggato.getId());
        return "redirect:/movies/" + filmId;
    }

    @PostMapping("/{recensioneId}/dislike")
    public String dislike(@PathVariable Long filmId,
                          @PathVariable Integer recensioneId,
                          @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        Utente utenteLoggato = utenteService.findByEmail(userDetails.getUsername());
        if (utenteLoggato == null) {
            throw new IllegalStateException("Utente non trovato");
        }

        recensioneService.aggiungiDislike(recensioneId, utenteLoggato.getId());
        return "redirect:/movies/" + filmId;
    }

    @PostMapping("/{recensioneId}/commenta")
    public String commenta(@PathVariable Long filmId,
                           @PathVariable Integer recensioneId,
                           @RequestParam String testo,
                           @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        Utente utenteLoggato = utenteService.findByEmail(userDetails.getUsername());
        if (utenteLoggato == null) {
            throw new IllegalStateException("Utente non trovato");
        }

        recensioneService.aggiungiCommento(recensioneId, utenteLoggato, testo);
        return "redirect:/movies/" + filmId;
    }

    @PostMapping("/{recensioneId}/elimina")
    public String eliminaRecensione(@PathVariable Long filmId,
                                    @PathVariable Integer recensioneId,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        Utente utenteLoggato = utenteService.findByEmail(userDetails.getUsername());
        if (utenteLoggato == null) {
            throw new IllegalStateException("Utente non trovato");
        }

        recensioneService.eliminaRecensione(recensioneId, utenteLoggato);
        return "redirect:/movies/" + filmId;
    }

    @PostMapping("/{recensioneId}/segnala")
    public String segnalaRecensione(@PathVariable Long filmId,
                                    @PathVariable Integer recensioneId,
                                    @RequestParam(required = false) String motivo,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        Utente utenteLoggato = utenteService.findByEmail(userDetails.getUsername());
        if (utenteLoggato == null) {
            throw new IllegalStateException("Utente non trovato");
        }

        try {
            recensioneService.segnalaRecensione(recensioneId, utenteLoggato.getId(), motivo);
        } catch (IllegalArgumentException e) {
            // già segnalata o propria recensione
        }
        return "redirect:/movies/" + filmId;
    }
}