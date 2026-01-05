package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.RegistrazioneRequestDTO;
import it.unisa.fidelio.presentation.UtenteDTO;
import it.unisa.fidelio.storage.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Base64;
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
            String username = dto.getUsername() != null ? dto.getUsername().trim() : null;
            String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
            String password = dto.getPassword();
            String nome = dto.getNome() != null ? dto.getNome().trim() : null;
            String cognome = dto.getCognome() != null ? dto.getCognome().trim() : null;

            if (nome == null || cognome == null || username == null || email == null || password == null) {
                return ResponseEntity.badRequest().body("Tutti i campi base sono obbligatori.");
            }

            Utente nuovoUtente = new Utente();
            nuovoUtente.setNome(nome);
            nuovoUtente.setCognome(cognome);
            nuovoUtente.setUsername(username);
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password);

            // --- GESTIONE IMMAGINE PROFILO ---
            if (dto.getImmagineBase64() != null && dto.getImmagineBase64().contains(",")) {
                // Rimuove l'intestazione "data:image/jpeg;base64,"
                String base64Image = dto.getImmagineBase64().split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                nuovoUtente.setImmagineProfilo(imageBytes);
            }

            String dtype = dto.getDtype() != null ? dto.getDtype() : "Cinefilo";
            nuovoUtente.setDtype(dtype);

            if ("Critico".equals(dtype)) {
                nuovoUtente.setTestataGiornalistica(dto.getTestataGiornalistica());
                nuovoUtente.setLivelloAccesso("PENDING");
            } else if ("Fedele".equals(dtype)) {
                nuovoUtente.setCasaProduzione(dto.getCasaProduzione());
                nuovoUtente.setCreditReference(dto.getCreditReference());
                nuovoUtente.setLivelloAccesso("PENDING");
            } else {
                nuovoUtente.setLivelloAccesso("BASE");
            }

            Utente salvato = utenteService.registrazione(nuovoUtente);
            return ResponseEntity.status(HttpStatus.CREATED).body(utenteService.mapToDTO(salvato));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore tecnico.");
        }
    }
}