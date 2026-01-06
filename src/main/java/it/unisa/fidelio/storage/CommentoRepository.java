package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentoRepository extends JpaRepository<Commento, Integer> {

    // Per le recensioni locali
    List<Commento> findByRecensioneIdOrderByDataCreazioneAsc(Integer recensioneId);

    // Per le recensioni TMDB
    List<Commento> findByTmdbReviewIdOrderByDataCreazioneAsc(String tmdbReviewId);
}