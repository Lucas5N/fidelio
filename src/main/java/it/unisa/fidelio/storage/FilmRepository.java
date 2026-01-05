package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FilmRepository extends JpaRepository<Film, Long> {

    List<Film> findByTitoloContainingIgnoreCase(String titolo);
    List<Film> findByGenere(String genere);
    List<Film> findByAnno(Integer anno);
    List<Film> findByRegistaContainingIgnoreCase(String regista);
    List<Film> findAllByOrderByVotoMedioDesc();
}