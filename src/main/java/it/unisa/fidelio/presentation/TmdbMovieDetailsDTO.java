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

        // ✅ aggiunte utili per la pagina dettagli
        @JsonProperty("runtime") Integer runtime,                 // minuti
        @JsonProperty("tagline") String tagline,                  // tagline
        @JsonProperty("original_language") String originalLanguage,
        @JsonProperty("status") String status,                    // Released, etc
        @JsonProperty("homepage") String homepage,

        @JsonProperty("genres") List<TmdbGenre> genres,  // lista di oggetti {id, name}
        @JsonProperty("production_countries") List<TmdbCountry> productionCountries

) {

    // Record per il campo genres
    public record TmdbGenre(int id, String name) {}

    public record TmdbCountry(
            @JsonProperty("iso_3166_1") String iso31661,
            String name
    ) {}

}