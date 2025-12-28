package it.unisa.fidelio.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {

    // Per il login
    Optional<Utente> findByEmailAndPassword(String email, String password);

    // Per controlli di unicità in registrazione
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Ricerca utenti per nome (es. per Community o Admin)
    List<Utente> findByUsernameContainingIgnoreCase(String partialUsername);

    // Trova utenti bannati o per ruolo (es. tutti i Cinefili)
    List<Utente> findByDtype(String dtype);
}




