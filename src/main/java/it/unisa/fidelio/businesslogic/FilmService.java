package it.unisa.fidelio.businesslogic;

import it.unisa.fidelio.dataaccess.Film;
import it.unisa.fidelio.dataaccess.FilmRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilmService {

    private final FilmRepository filmRepo;

    public FilmService(FilmRepository filmRepo) {
        this.filmRepo = filmRepo;
    }

    public List<Film> cercaFilm(String titolo) {
        return filmRepo.findByTitoloContainingIgnoreCase(titolo);
    }

    public List<Film> filtraFilm(String genere, Integer anno) {
        if (genere != null && !genere.isEmpty()) {
            return filmRepo.findByGenere(genere);
        }
        if (anno != null) {
            return filmRepo.findByAnno(anno);
        }
        return filmRepo.findAll();
    }

    public Film getDettagli(int id) {
        return filmRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Film non trovato"));
    }
}
