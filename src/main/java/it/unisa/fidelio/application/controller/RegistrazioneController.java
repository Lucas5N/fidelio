package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.RegistrazioneRequestDTO;
import it.unisa.fidelio.presentation.UtenteDTO;
import it.unisa.fidelio.storage.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/registrazione")
@CrossOrigin(origins = "*")
public class RegistrazioneController {

    private final UtenteService utenteService;

    public RegistrazioneController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @PostMapping
    public ResponseEntity<?> registra(@RequestBody RegistrazioneRequestDTO dto) {
        try {
            // Validazione e trim campi base
            String username = dto.getUsername() != null ? dto.getUsername().trim() : null;
            String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
            String password = dto.getPassword();
            String nome = dto.getNome() != null ? dto.getNome().trim() : null;
            String cognome = dto.getCognome() != null ? dto.getCognome().trim() : null;


            if (nome == null || cognome == null || username == null || username.isEmpty() ||
                    email == null || email.isEmpty() ||
                    password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body("Tutti i campi obbligatori (nome, cognome, username, email, password) devono essere compilati");
            }

            // Gestione dtype
            String dtype = dto.getDtype() != null ? dto.getDtype().trim() : "Cinefilo";
            List<String> dtypeValidi = Arrays.asList("Cinefilo", "Critico", "Fedele");
            if (!dtypeValidi.contains(dtype)) {
                dtype = "Cinefilo";
            }

            // Creazione entità
            Utente nuovoUtente = new Utente();
            nuovoUtente.setUsername(username);
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password); // verrà hashata nel service
            nuovoUtente.setDtype(dtype);

            // Gestione campi esclusivi in base al dtype
            if ("Critico".equals(dtype)) {
                String testata = dto.getTestataGiornalistica();
                if (testata == null || testata.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("La testata giornalistica è obbligatoria per i Critici");
                }
                nuovoUtente.setTestataGiornalistica(testata.trim());

            } else if ("Fedele".equals(dtype)) {
                String casaProduzione = dto.getCasaProduzione();
                String creditReference = dto.getCreditReference();

                if (casaProduzione == null || casaProduzione.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("La casa di produzione è obbligatoria per i Fedeli");
                }
                if (creditReference == null || creditReference.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("Il credit reference (es. link IMDB) è obbligatorio per i Fedeli");
                }

                nuovoUtente.setCasaProduzione(casaProduzione.trim());
                nuovoUtente.setCreditReference(creditReference.trim());
            }
            // Per Cinefilo: nulla da fare, campi extra ignorati

            // Livello accesso: PENDING per profili da approvare
            if ("Critico".equals(dtype) || "Fedele".equals(dtype)) {
                nuovoUtente.setLivelloAccesso("PENDING");
            } else {
                nuovoUtente.setLivelloAccesso("BASE");
            }

            // Salvataggio
            Utente utenteSalvato = utenteService.registrazione(nuovoUtente);

            // Auto-login
            UtenteDTO utenteLoggato = utenteService.mapToDTO(utenteSalvato);

            return ResponseEntity.status(HttpStatus.CREATED).body(utenteLoggato);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // per debug in console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione. Riprova più tardi.");
        }
    }
}