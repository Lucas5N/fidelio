package it.unisa.fidelio.presentation;

public record PopularReviewViewDTO(
        String author,
        String username,
        String avatarInitial,
        String starsText,
        String dateLabel,
        String contentPreview,
        String url
) {}
