package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.TmdbMovieDto;
import it.unisa.fidelio.storage.api_data.TmdbGenreListResponse;
import it.unisa.fidelio.storage.api_data.TmdbMovieListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TmdbClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String language;
    private final String region;

    public TmdbClient(
            @Value("${tmdb.base-url}") String baseUrl,
            @Value("${tmdb.api-key}") String apiKey,
            @Value("${tmdb.language}") String language,
            @Value("${tmdb.region}") String region
    ) {
        this.apiKey = apiKey;
        this.language = language;
        this.region = region;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }


    public TmdbMovieListResponse searchMovies(String query, int page) {
        return restClient.get()
                .uri(uri -> uri.path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("language", language)
                        .queryParam("region", region)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbMovieListResponse.class);
    }

    public TmdbMovieDto getMovieDetails(Long tmdbId) {
        return restClient.get()
                .uri(uri -> uri.path("/movie/" + tmdbId)
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .body(TmdbMovieDto.class);
    }

    public TmdbMovieListResponse getPopular(int page) {
        return restClient.get()
                .uri(uri -> uri.path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .queryParam("region", region)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbMovieListResponse.class);
    }

    public TmdbMovieListResponse getNowPlaying(int page) {
        return restClient.get()
                .uri(uri -> uri.path("/movie/now_playing")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .queryParam("region", region)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbMovieListResponse.class);
    }

    public TmdbGenreListResponse getMovieGenres() {
        return restClient.get()
                .uri(uri -> uri.path("/genre/movie/list")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .body(TmdbGenreListResponse.class);
    }
}
