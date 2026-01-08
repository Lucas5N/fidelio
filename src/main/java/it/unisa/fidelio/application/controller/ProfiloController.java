package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTANTE
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ProfiloController {

    private final UtenteService utenteService;
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder; // Aggiunto per gestire il cambio password

    public ProfiloController(UtenteService utenteService, UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteService = utenteService;
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profilo")
    public String visualizzaProfilo(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Utente utente = utenteService.findByEmail(userDetails.getUsername());

        model.addAttribute("utenteCorrente", utente);
        model.addAttribute("recensioni", utente.getRecensioni());
        // model.addAttribute("listePrivate", utente.getListePrivate());

        return "profiloUtente";
    }

    @PostMapping("/profilo/aggiorna")
    public String aggiornaProfilo(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam("nome") String nome,
                                  @RequestParam("cognome") String cognome,
                                  @RequestParam("username") String username,
                                  @RequestParam("email") String email,
                                  @RequestParam("viaENumCivico") String viaENumCivico,
                                  @RequestParam(value = "nuovaPassword", required = false) String nuovaPassword,
                                  @RequestParam("bio") String bio,
                                  // Campi specifici (opzionali in base al ruolo)
                                  @RequestParam(value = "testata", required = false) String testata,
                                  @RequestParam(value = "casa", required = false) String casa,
                                  @RequestParam(value = "credit", required = false) String credit,
                                  @RequestParam(value = "immagine", required = false) MultipartFile immagine) {

        Utente utente = utenteService.findByEmail(userDetails.getUsername());

        // 1. Aggiornamento dati base
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setBio(bio);
        utente.setViaENumCivico(viaENumCivico);

        // Nota: Cambiare email/username potrebbe richiedere un nuovo login in base alla config di Security
        // Qui lo permettiamo direttamente:
        utente.setUsername(username);
        utente.setEmail(email);

        // 2. Gestione Password (solo se l'utente ha scritto qualcosa)
        if (nuovaPassword != null && !nuovaPassword.isBlank()) {
            utente.setPassword(passwordEncoder.encode(nuovaPassword));
        }

        // 3. Gestione Ruoli Specifici
        if ("Critico".equals(utente.getDtype())) {
            utente.setTestataGiornalistica(testata);
        } else if ("Fedele".equals(utente.getDtype())) {
            utente.setCasaProduzione(casa);
            utente.setCreditReference(credit);
        }

        // 4. Immagine
        if (immagine != null && !immagine.isEmpty()) {
            try {
                utente.setImmagineProfilo(immagine.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        utenteRepository.save(utente);

        // Se l'email (che è l'ID di login) è cambiata, Spring Security potrebbe disconnettere l'utente.
        // Per semplicità facciamo redirect al profilo, se slogga l'utente dovrà rifare login.
        return "redirect:/profilo";
    }
}