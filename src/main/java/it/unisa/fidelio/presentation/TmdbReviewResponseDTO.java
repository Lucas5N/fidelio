package it.unisa.fidelio.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbReviewResponseDTO(
        long id,
        int page,
        List<TmdbReviewDTO> results,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_results") int totalResults
) {
    public record TmdbReviewDTO(
            String id,
            String author,
            @JsonProperty("author_details") AuthorDetails authorDetails,
            String content,
            @JsonProperty("created_at") String createdAt,
            String url
    ) {}

    public record AuthorDetails(
            String name,
            String username,
            @JsonProperty("avatar_path") String avatarPath,
            Double rating
    ) {}
}
