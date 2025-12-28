package it.unisa.fidelio.businesslogic;

import it.unisa.fidelio.dataaccess.Utente;
import it.unisa.fidelio.dataaccess.UtenteRepository;
import org.springframework.stereotype.Service;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepo;

    public UtenteService(UtenteRepository utenteRepo) {
        this.utenteRepo = utenteRepo;
    }

    public Utente registrazione(Utente utente) {
        if (utenteRepo.existsByEmail(utente.getEmail())) {
            throw new IllegalArgumentException("Email già in uso");
        }
        if (utenteRepo.existsByUsername(utente.getUsername())) {
            throw new IllegalArgumentException("Username già in uso");
        }
        // Qui potresti hashare la password (es. BCrypt)
        return utenteRepo.save(utente);
    }

    public Utente login(String email, String password) {
        return utenteRepo.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide"));
    }

    public Utente getProfilo(int id) {
        return utenteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
    }

    public void aggiornaBio(int id, String nuovaBio) {
        Utente u = getProfilo(id);
        u.setBio(nuovaBio);
        utenteRepo.save(u);
    }
}
