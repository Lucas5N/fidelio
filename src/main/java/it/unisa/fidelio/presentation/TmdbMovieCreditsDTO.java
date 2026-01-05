package it.unisa.fidelio.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbMovieCreditsDTO(
        Long id,
        List<CastDto> cast,
        List<CrewDto> crew
) {
    public record CastDto(
            Integer id,
            String name,
            String character,
            @JsonProperty("profile_path") String profilePath,
            Integer order
    ) {}

    public record CrewDto(
            Integer id,
            String name,
            String job,
            String department,
            @JsonProperty("profile_path") String profilePath
    ) {}
}

