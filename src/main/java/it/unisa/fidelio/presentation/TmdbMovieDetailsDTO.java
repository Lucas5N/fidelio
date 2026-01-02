package it.unisa.fidelio.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbMovieDetailsDTO(
        long id,
        String title,
        @JsonProperty("original_title") String originalTitle,
        @JsonProperty("overview") String overview,  // trama completa
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("vote_average") double voteAverage,
        @JsonProperty("vote_count") int voteCount,
        @JsonProperty("genres") List<TmdbGenre> genres  // lista di oggetti {id, name}
) {

    // Record per il campo genres
    public record TmdbGenre(int id, String name) {}
}