package it.unisa.fidelio.dataaccess;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbMovieListResponse(
        int page,
        List<TmdbMovieDto> results
) {}



