package it.unisa.fidelio.application;

import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import it.unisa.fidelio.presentation.UtenteDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepo;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepo, PasswordEncoder passwordEncoder) {
        this.utenteRepo = utenteRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UtenteDTO login(String email, String passwordChiaro) {
        // Cerchiamo l'utente tramite email
        return utenteRepo.findByEmail(email)
                .filter(utente -> passwordEncoder.matches(passwordChiaro, utente.getPassword()))
                .map(this::mapToDTO)
                .orElse(null);
    }

    //SE QUALCOSA VA STORTO FA ROLLBACK
    @Transactional
    public Utente registrazione(Utente utente) {
        if (utenteRepo.existsByEmail(utente.getEmail())) {
            throw new IllegalArgumentException("Email già in uso");
        }
        if (utenteRepo.existsByUsername(utente.getUsername())) {
            throw new IllegalArgumentException("Username già in uso");
        }
        utente.setPassword(passwordEncoder.encode(utente.getPassword()));

        if (utente.getDataRegistrazione() == null) {
            utente.setDataRegistrazione(Instant.now());
        }

        return utenteRepo.save(utente);
    }

    public Utente getProfilo(int id) {
        return utenteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato con ID: " + id));
    }

    @Transactional
    public void aggiornaBio(int id, String nuovaBio) {
        Utente u = getProfilo(id);
        u.setBio(nuovaBio);
        utenteRepo.save(u);
    }

    private UtenteDTO mapToDTO(Utente u) {
        return new UtenteDTO(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getDtype(),
                u.getBio(),
                u.getImmagineProfilo(),
                u.getLivelloAccesso()
        );
    }
}