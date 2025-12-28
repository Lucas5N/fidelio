package it.unisa.fidelio.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RecensioneRepository extends JpaRepository<Recensione, Integer> {

    // Tutte le recensioni di un film specifico
    List<Recensione> findByFilm(Film film);

    // Tutte le recensioni scritte da un utente
    List<Recensione> findByAutore(Utente autore);

    // Recensioni più popolari (per numero di like)
    List<Recensione> findByFilmOrderByNumLikeDesc(Film film);
}