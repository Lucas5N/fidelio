package it.unisa.fidelio.storage.api_data;

import it.unisa.fidelio.presentation.TmdbMovieDto;

import java.util.List;

public record TmdbMovieListResponse(
        int page,
        List<TmdbMovieDto> results
) {}



