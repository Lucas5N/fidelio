package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.RecensioneDTO;
import it.unisa.fidelio.storage.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepo;
    private final FilmRepository filmRepo;

    public RecensioneService(RecensioneRepository recensioneRepo, FilmRepository filmRepo) {
        this.recensioneRepo = recensioneRepo;
        this.filmRepo = filmRepo;
    }

    public Recensione scriviRecensione(Utente autore, Film film, String testo, double voto, boolean spoilerAlert) {
        if (testo == null || testo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il testo della recensione non può essere vuoto");
        }
        if (voto < 0 || voto > 10) {
            throw new IllegalArgumentException("Il voto deve essere tra 0 e 10");
        }

        Recensione r = new Recensione();
        r.setAutore(autore);
        r.setFilm(film);
        r.setTesto(testo.trim());
        r.setVoto(voto);
        r.setSpoilerAlert(spoilerAlert);
        r.setDataCreazione(Instant.now());
        r.setNumLike(0);
        r.setNumDislike(0);

        return recensioneRepo.save(r);
    }

    public List<Recensione> getRecensioniPerFilm(Film film) {
        return recensioneRepo.findByFilmOrderByNumLikeDesc(film);
    }

    public List<RecensioneDTO> getRecensioniDtoPerFilm(Film film) {
        return getRecensioniPerFilm(film).stream()
                .map(this::toDto)
                .toList();
    }

    private RecensioneDTO toDto(Recensione r) {
        return new RecensioneDTO(
                r.getId(),
                r.getTesto(),
                r.getVoto(),
                r.getDataCreazione(),
                r.getSpoilerAlert() != null && r.getSpoilerAlert(),
                r.getNumLike(),
                r.getNumDislike(),
                r.getAutore().getUsername(),
                r.getFilm().getTitolo(),
                r.getFilm().getId()  // ← l'ID del film È l'ID TMDB
        );
    }

    public void aggiungiLike(Integer recensioneId) {
        Recensione r = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));
        r.setNumLike(r.getNumLike() + 1);
        recensioneRepo.save(r);
    }

    public void aggiungiDislike(Integer recensioneId) {
        Recensione r = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));
        r.setNumDislike(r.getNumDislike() + 1);
        recensioneRepo.save(r);
    }

    public void cancellaRecensione(Integer recensioneId, Utente utenteCorrente) {
        Recensione r = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));
        if (!r.getAutore().getId().equals(utenteCorrente.getId())) {
            throw new SecurityException("Non autorizzato a cancellare questa recensione");
        }
        recensioneRepo.delete(r);
    }
}