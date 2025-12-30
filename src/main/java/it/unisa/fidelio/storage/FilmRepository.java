package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FilmRepository extends JpaRepository<Film, Long> {

    // Ricerca base per titolo (case insensitive)
    List<Film> findByTitoloContainingIgnoreCase(String titolo);

    // Ricerca avanzata (Filtri)
    List<Film> findByGenere(String genere);
    List<Film> findByAnno(Integer anno);
    List<Film> findByRegistaContainingIgnoreCase(String regista);

    // Ordinamento per voto (Top Rated)
    List<Film> findAllByOrderByVotoMedioDesc();
}