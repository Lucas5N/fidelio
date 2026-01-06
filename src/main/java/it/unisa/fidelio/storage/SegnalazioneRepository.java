package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SegnalazioneRepository extends JpaRepository<Segnalazione, Integer> {

    List<Segnalazione> findByStato(String stato);

    List<Segnalazione> findByAutoreId(Integer autoreId);

    boolean existsByRecensioneIdAndAutoreId(Integer recensioneId, int autoreId);

    @Query("SELECT s.recensione.id FROM Segnalazione s " +
            "WHERE s.autore.id = :autoreId " +
            "AND s.recensione.filmTmdbId = :filmId")
    Set<Integer> findRecensioneIdsByAutoreIdAndFilmTmdbId(
            @Param("autoreId") Integer autoreId,
            @Param("filmId") Long filmId);
}