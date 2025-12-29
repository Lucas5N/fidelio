package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.LoginRequestDTO;
import it.unisa.fidelio.presentation.UtenteDTO;
import it.unisa.fidelio.storage.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permette chiamate da frontend esterni (es. React/Vue)
public class AutenticazioneController {

    private final UtenteService utenteService;

    public AutenticazioneController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    /**
     * Endpoint per il LOGIN.
     * Riceve email e password, restituisce i dati dell'utente se validi.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            UtenteDTO utenteLoggato = utenteService.login(request.getEmail(), request.getPassword());

            if (utenteLoggato != null) {
                return ResponseEntity.ok(utenteLoggato);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Credenziali non valide: email o password errati.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il login: " + e.getMessage());
        }
    }

    /**
     * Endpoint per la REGISTRAZIONE.
     * Riceve un oggetto Utente, cripta la password e lo salva.
     */
    @PostMapping("/registrazione")
    public ResponseEntity<?> registrazione(@RequestBody Utente utente) {
        try {
            Utente nuovoUtente = utenteService.registrazione(utente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuovoUtente);
        } catch (IllegalArgumentException e) {
            // Gestisce errori come "Email già in uso"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione: " + e.getMessage());
        }
    }
}