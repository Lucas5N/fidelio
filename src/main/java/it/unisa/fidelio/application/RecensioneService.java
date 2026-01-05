package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.PopularReviewViewDTO;
import it.unisa.fidelio.presentation.RecensioneDTO;
import it.unisa.fidelio.presentation.TmdbReviewResponseDTO;
import it.unisa.fidelio.storage.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepo;
    private final FilmRepository filmRepo;

    private final TmdbClient tmdbClient;

    public RecensioneService(RecensioneRepository recensioneRepo, FilmRepository filmRepo, TmdbClient tmdbClient) {
        this.recensioneRepo = recensioneRepo;
        this.filmRepo = filmRepo;
        this.tmdbClient = tmdbClient;
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

    public java.util.List<PopularReviewViewDTO> getPopularReviewsFromTmdb(Long tmdbId, int limit) {
        TmdbReviewResponseDTO res = tmdbClient.getMovieReviews(tmdbId, 1);
        if (res == null || res.results() == null) return java.util.List.of();

        return res.results().stream()
                .limit(limit)
                .map(this::toPopularReviewView)
                .toList();
    }

    private PopularReviewViewDTO toPopularReviewView(TmdbReviewResponseDTO.TmdbReviewDTO r) {
        String username = r.authorDetails() != null && r.authorDetails().username() != null
                ? r.authorDetails().username()
                : r.author();

        String avatarInitial = (username != null && !username.isBlank())
                ? ("" + Character.toUpperCase(username.charAt(0)))
                : "U";

        // TMDB author_details.rating è su scala 10, spesso null
        String starsText = ratingToStars(r.authorDetails() != null ? r.authorDetails().rating() : null);

        String dateLabel = toSimpleDateLabel(r.createdAt());

        String contentPreview = (r.content() == null) ? "" : r.content().trim();
        if (contentPreview.length() > 420) contentPreview = contentPreview.substring(0, 420) + "…";

        return new PopularReviewViewDTO(
                r.author(),
                username,
                avatarInitial,
                starsText,
                dateLabel,
                contentPreview,
                r.url()
        );
    }

    private String ratingToStars(Double rating10) {
        if (rating10 == null) return "—";
        // 10 -> 5 stelle: arrotondo a mezze? per ora a intere
        int stars = (int) Math.round(rating10 / 2.0); // 0..5
        stars = Math.max(0, Math.min(5, stars));

        String full = "★★★★★";
        String empty = "☆☆☆☆☆";
        return full.substring(0, stars) + empty.substring(0, 5 - stars);
    }

    private String toSimpleDateLabel(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.length() < 10) return "";
        // prende YYYY-MM-DD
        return isoDateTime.substring(0, 10);
    }

}