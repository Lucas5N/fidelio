package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.RegistrazioneRequestDTO;
import it.unisa.fidelio.presentation.UtenteDTO;
import it.unisa.fidelio.storage.Utente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Base64;

@RestController
@RequestMapping("/api/registrazione")
@CrossOrigin(origins = "*")
public class RegistrazioneController {

    private final UtenteService utenteService;
    private final AuthenticationManager authenticationManager;

    public RegistrazioneController(UtenteService utenteService, AuthenticationManager authenticationManager) {
        this.utenteService = utenteService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    public ResponseEntity<?> registra(@Valid @RequestBody RegistrazioneRequestDTO dto) {
        try {
            String username = dto.getUsername() != null ? dto.getUsername().trim() : null;
            String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
            String nome = dto.getNome() != null ? dto.getNome().trim() : null;
            String cognome = dto.getCognome() != null ? dto.getCognome().trim() : null;
            String password = dto.getPassword();

            // Validazione campi obbligatori
            if (nome == null || nome.isEmpty() || cognome == null || cognome.isEmpty() ||
                    username == null || username.isEmpty() || email == null || email.isEmpty() ||
                    password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body("Tutti i campi obbligatori devono essere compilati.");
            }

            // Controllo unicità username e email
            if (utenteService.existsByUsername(username)) {
                return ResponseEntity.badRequest().body("Username già in uso.");
            }
            if (utenteService.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("Email già registrata.");
            }

            Utente nuovoUtente = new Utente();
            nuovoUtente.setNome(nome);
            nuovoUtente.setCognome(cognome);
            nuovoUtente.setUsername(username);
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password); // verrà criptata nel service
            nuovoUtente.setDataRegistrazione(java.time.Instant.now());
            nuovoUtente.setNumFilmVisti(0);

            // Immagine profilo
            if (dto.getImmagineBase64() != null && dto.getImmagineBase64().contains(",")) {
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

            // Salva utente (password criptata nel service)
            Utente salvato = utenteService.registrazione(nuovoUtente);

            // === LOGIN AUTOMATICO ===
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // === CREA E SALVA LA SESSIONE ===
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession(true); // crea sessione
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            // Restituisci DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(utenteService.mapToDTO(salvato));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Username o email già in uso.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la registrazione.");
        }
    }
}