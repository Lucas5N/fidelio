package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RecensioneInterazioneRepository extends JpaRepository<RecensioneInterazione, Long> {

    boolean existsByUtenteIdAndRecensioneIdAndTipo(Integer utente_id, Integer recensione_id, RecensioneInterazione.TipoInterazione tipo);

    @Query("SELECT ri FROM RecensioneInterazione ri WHERE ri.utente.id = :utenteId AND ri.tipo = :tipo")
    Set<RecensioneInterazione> findByUtenteIdAndTipo(
            @Param("utenteId") Integer utenteId,
            @Param("tipo") RecensioneInterazione.TipoInterazione tipo);

    @Query("SELECT ri.recensione.id FROM RecensioneInterazione ri " +
            "WHERE ri.utente.id = :utenteId " +
            "AND ri.recensione.filmTmdbId = :filmId " +
            "AND ri.tipo = :tipo")
    Set<Integer> findRecensioneIdsByUtenteIdAndFilmTmdbIdAndTipo(
            @Param("utenteId") Integer utenteId,
            @Param("filmId") Long filmId,
            @Param("tipo") RecensioneInterazione.TipoInterazione tipo);
}