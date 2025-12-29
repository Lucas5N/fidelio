package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SegnalazioneRepository extends JpaRepository<Segnalazione, Integer> {
        List<Segnalazione> findByStato(String stato);
}