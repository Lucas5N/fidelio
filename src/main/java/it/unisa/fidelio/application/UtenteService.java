package it.unisa.fidelio.application;

import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import it.unisa.fidelio.presentation.UtenteDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Base64;

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
        // Estrai e trimma i campi
        String username = utente.getUsername() != null ? utente.getUsername().trim() : null;
        String email = utente.getEmail() != null ? utente.getEmail().trim().toLowerCase() : null;
        String password = utente.getPassword();
        String nome = utente.getNome() != null ? utente.getNome().trim() : null;
        String cognome = utente.getCognome() != null ? utente.getCognome().trim() : null;

        // === VALIDAZIONI CON REGEX CORRETTE ===

        // Username: 3-20 caratteri, solo lettere, numeri e underscore
        if (username == null || username.isBlank() || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("Username non valido: deve contenere da 3 a 20 caratteri e solo lettere, numeri o underscore (_).");
        }

        // Email: formato standard moderno (supporta + , subdomini, TLD lunghi)
        if (email == null || email.isBlank() || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Formato email non valido. Esempio corretto: nome@dominio.com");
        }

        // Password: almeno 8 caratteri, con maiuscola, minuscola, numero e almeno un carattere speciale
        if (password == null || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$")) {
            throw new IllegalArgumentException("Password troppo debole: deve avere almeno 8 caratteri, includere una maiuscola, una minuscola, un numero e un carattere speciale (!@#$%^&*).");
        }

        // Nome: 2-30 caratteri, lettere accentate, apostrofo, trattino e spazio
        if (nome == null || nome.isBlank() || !nome.matches("^[A-Za-zÀ-ÿ' -]{2,30}$")) {
            throw new IllegalArgumentException("Nome non valido: deve contenere da 2 a 30 caratteri e solo lettere (anche accentate), apostrofo ('), trattino (-) o spazio.");
        }

// Cognome: stessa regola del nome
        if (cognome == null || cognome.isBlank() || !cognome.matches("^[A-Za-zÀ-ÿ' -]{2,30}$")) {
            throw new IllegalArgumentException("Cognome non valido: deve contenere da 2 a 30 caratteri e solo lettere (anche accentate), apostrofo ('), trattino (-) o spazio.");
        }

        // Controlli unicità
        if (utenteRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Questa email è già registrata.");
        }
        if (utenteRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Questo username è già in uso.");
        }

        // Hash della password
        utente.setPassword(passwordEncoder.encode(password));

        // Normalizza campi
        utente.setEmail(email);
        utente.setUsername(username);
        utente.setNome(nome);
        utente.setCognome(cognome);

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
        UtenteDTO dto = new UtenteDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setDtype(u.getDtype());
        dto.setBio(u.getBio());
        dto.setLivelloAccesso(u.getLivelloAccesso());

        if (u.getImmagineProfilo() != null) {
            String base64 = java.util.Base64.getEncoder().encodeToString(u.getImmagineProfilo());
            dto.setImmagineProfilo("data:image/jpeg;base64," + base64);
        }

        return dto;
    }
}