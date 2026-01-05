package it.unisa.fidelio.presentation;

import java.time.Instant;

public record RecensioneDTO(
        Integer id,
        String testo,
        Double voto,
        Instant dataCreazione,
        boolean spoilerAlert,
        Integer numLike,
        Integer numDislike,
        String usernameAutore,
        String titoloFilm,
        Long filmTmdbId  // per link a dettagli TMDB
) {}

