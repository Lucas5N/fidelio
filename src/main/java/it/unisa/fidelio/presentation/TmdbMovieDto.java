package it.unisa.fidelio.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieDto(
        long id,
        String title,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("vote_average") double voteAverage,
        @JsonProperty("genre_ids") List<Integer> genreIds
) {}
