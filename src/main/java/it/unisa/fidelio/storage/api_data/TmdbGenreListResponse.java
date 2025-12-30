package it.unisa.fidelio.storage.api_data;

import it.unisa.fidelio.presentation.TmdbGenre;

import java.util.List;

public record TmdbGenreListResponse(List<TmdbGenre> genres) {}

