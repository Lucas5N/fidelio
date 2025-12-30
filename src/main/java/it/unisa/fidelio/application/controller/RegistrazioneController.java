package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.RegistrazioneRequestDTO;
import it.unisa.fidelio.presentation.UtenteDTO;
import it.unisa.fidelio.storage.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrazione")
@CrossOrigin(origins = "*")
public class RegistrazioneController {

    private final UtenteService utenteService;

    public RegistrazioneController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @PostMapping                   //DESERIALIZZA IL JSON INVIATOGLI IN OGGETTO JAVA
    public ResponseEntity<?> registra(@RequestBody RegistrazioneRequestDTO dto) {
        try {
            Utente nuovoUtente = new Utente();
            nuovoUtente.setUsername(dto.getUsername());
            nuovoUtente.setEmail(dto.getEmail());
            nuovoUtente.setPassword(dto.getPassword());
            nuovoUtente.setBio(dto.getBio());
            nuovoUtente.setDtype("Cinefilo");
            nuovoUtente.setLivelloAccesso("BASE");

            Utente utenteSalvato = utenteService.registrazione(nuovoUtente);

            // 3. AUTO-LOGIN: Trasformiamo l'entity appena salvata in un UtenteDTO
            UtenteDTO utenteLoggato = utenteService.mapToDTO(utenteSalvato);

            // 4. Restituiamo l'utente al frontend con stato 201 Created
            // Il frontend vedrà questo oggetto e saprà che l'utente è "loggato"
            return ResponseEntity.status(HttpStatus.CREATED).body(utenteLoggato);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione: " + e.getMessage());
        }
    }
}