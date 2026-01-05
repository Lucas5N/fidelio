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
        return utenteRepo.findByEmail(email.toLowerCase())
                .filter(utente -> passwordEncoder.matches(passwordChiaro, utente.getPassword()))
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public Utente registrazione(Utente utente) {
        // Validazioni server-side con regex
        String username = utente.getUsername();
        String email = utente.getEmail();
        String password = utente.getPassword();
        String nome = utente.getNome();
        String cognome = utente.getCognome();

        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("Username non valido: 3-20 caratteri, solo lettere, numeri e _");
        }

        if (email == null || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Formato email non valido");
        }

        if (password == null || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*]{8,}$")) {
            throw new IllegalArgumentException("Password troppo debole: almeno 8 caratteri, con maiuscola, minuscola e numero");
        }

        if(nome == null || !nome.matches("^[A-zÀ-ù ‘-]{2,30}$")){
            throw new IllegalArgumentException("Nome non rispetta il formato (lunghezza 2-30)");
        }

        if(cognome == null || !cognome.matches("^[A-zÀ-ù ‘-]{2,30}$")){
            throw new IllegalArgumentException("Cognome non rispetta il formato (lunghezza 2-30)");
        }

        // Controlli unicità
        if (utenteRepo.existsByEmail(email.toLowerCase())) {
            throw new IllegalArgumentException("Email già in uso");
        }
        if (utenteRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username già in uso");
        }

        // Hash password
        utente.setPassword(passwordEncoder.encode(password));

        // Normalizza email
        utente.setEmail(email.toLowerCase());

        // Data registrazione
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

    public UtenteDTO mapToDTO(Utente u) {
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