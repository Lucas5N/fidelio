package it.unisa.fidelio.presentation;

import java.util.List;

public record PopularReviewViewDTO(
        String username,
        String authorDisplayName,
        String avatarInitial,
        String starsText,
        String dateLabel,
        String contentPreview,
        String externalUrl,
        boolean local,
        Integer localReviewId,
        String tmdbReviewId,
        int numLike,        // ← AGGIUNGI QUESTO
        int numDislike,     // ← AGGIUNGI QUESTO
        List<CommentoDTO> comments,
        String dtype
) {}