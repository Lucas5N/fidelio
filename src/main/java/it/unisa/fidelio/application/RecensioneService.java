package it.unisa.fidelio.application;


import it.unisa.fidelio.storage.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepo;
    private final UtenteRepository utenteRepo;
    private final FilmRepository filmRepo;

    public RecensioneService(RecensioneRepository recensioneRepo, UtenteRepository utenteRepo, FilmRepository filmRepo) {
        this.recensioneRepo = recensioneRepo;
        this.utenteRepo = utenteRepo;
        this.filmRepo = filmRepo;
    }

    public Recensione scriviRecensione(int autoreId, Long filmId, String testo, double voto) {
        Utente autore = utenteRepo.findById(autoreId).orElseThrow(() -> new IllegalArgumentException("Utente non valido"));
        Film film = filmRepo.findById(filmId).orElseThrow(() -> new IllegalArgumentException("Film non valido"));

        Recensione r = new Recensione();
        r.setAutore(autore);
        r.setFilm(film);
        r.setTesto(testo);
        r.setVoto(voto);
        r.setDataCreazione(Instant.from(LocalDateTime.now()));

        return recensioneRepo.save(r);
    }

    public List<Recensione> getRecensioniPerFilm(Long filmId) {
        Film f = filmRepo.findById(filmId).orElseThrow();
        return recensioneRepo.findByFilm(f);
    }
}
