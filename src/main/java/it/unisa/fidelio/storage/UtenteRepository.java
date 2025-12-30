package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {

    // Per Spring Security (caricamento utente per email)
    Optional<Utente> findByEmail(String email);

    // Controlli unicità per registrazione
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Ricerca utenti
    List<Utente> findByUsernameContainingIgnoreCase(String partialUsername);
    Optional<Utente> findByUsername(String username);
    // Per admin o filtri
    List<Utente> findByDtype(String dtype);
}