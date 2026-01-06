package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecensioneRepository extends JpaRepository<Recensione, Integer> {
    List<Recensione> findByFilmTmdbIdOrderByNumLikeDesc(Long filmTmdbId);
    List<Recensione> findByAutore(Utente autore);
}