package it.unisa.fidelio.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.presentation.RegistrazioneRequestDTO;
import it.unisa.fidelio.presentation.UtenteDTO;
import it.unisa.fidelio.storage.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrazioneController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrazioneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtenteService utenteService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private RegistrazioneRequestDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new RegistrazioneRequestDTO();
        validDto.setUsername("MarioRossi");
        validDto.setEmail("mario.rossi@email.com");
        validDto.setPassword("password123");
        validDto.setConfermaPassword("password123");
        validDto.setNome("Mario");
        validDto.setCognome("Rossi");
        validDto.setViaEnumCivico("Via Roma 10");
        validDto.setDtype("Cinefilo");

        // Inizializzazione a null per i campi opzionali specifici del ruolo
        validDto.setTestataGiornalistica(null);
        validDto.setCasaProduzione(null);
        validDto.setCreditReference(null);
    }

    // ===================================================================================
    // TEST ESISTENTI (VALIDAZIONI BASE)
    // ===================================================================================

    @Test
    void testRegistrazione_UsernameTroppoLungo() throws Exception {
        validDto.setUsername("QuestoUsernameEVeramenteTroppoLungoPerEssereAccettatoDalSistema");
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errato: Nome utente troppo lungo o nullo"));
    }

    @Test
    void testRegistrazione_EmailFormatoErrato() throws Exception {
        validDto.setEmail("emailSenzaChiocciola.com");
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errato: E-Mail non corretta"));
    }

    @Test
    void testRegistrazione_PasswordCorta() throws Exception {
        validDto.setPassword("short");
        validDto.setConfermaPassword("short");
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errato: lunghezza password non corretta (min 8)"));
    }

    @Test
    void testRegistrazione_PasswordMismatch() throws Exception {
        validDto.setPassword("password123");
        validDto.setConfermaPassword("passwordDiversa");
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errato: conferma password errata"));
    }

    @Test
    void testRegistrazione_NomeErrato() throws Exception {
        validDto.setNome("M");
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errato: nome non corretto"));
    }

    // ===================================================================================
    // NUOVI TEST PER AUMENTARE COVERAGE
    // ===================================================================================

    @Test
    void testRegistrazione_CognomeErrato() throws Exception {
        validDto.setCognome("R");
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errato: cognome non corretto"));
    }

    @Test
    void testRegistrazione_IndirizzoVuoto() throws Exception {
        validDto.setViaEnumCivico("");

        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(utenteService.registrazione(any(Utente.class))).thenReturn(new Utente());
        when(utenteService.mapToDTO(any(Utente.class))).thenReturn(new UtenteDTO());

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegistrazione_DtypeNonValido() throws Exception {
        validDto.setDtype("Alieno");

        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(utenteService.registrazione(any(Utente.class))).thenReturn(new Utente());
        when(utenteService.mapToDTO(any(Utente.class))).thenReturn(new UtenteDTO());

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
    }

    // ===================================================================================
    // TEST RUOLI SPECIFICI (CINEFILO, CRITICO, FEDELE)
    // ===================================================================================

    @Test
    void testRegistrazione_Cinefilo_Successo() throws Exception {
        validDto.setDtype("Cinefilo");

        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(utenteService.registrazione(any(Utente.class))).thenReturn(new Utente());
        when(utenteService.mapToDTO(any(Utente.class))).thenReturn(new UtenteDTO());

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegistrazione_Critico_SenzaTestata() throws Exception {
        validDto.setDtype("Critico");
        validDto.setTestataGiornalistica(null);

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Testata giornalistica non trovata o formato errato"));
    }

    @Test
    void testRegistrazione_Critico_Successo() throws Exception {
        validDto.setDtype("Critico");
        validDto.setTestataGiornalistica("La Repubblica");
        validDto.setCasaProduzione(null);
        validDto.setCreditReference(null);

        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(utenteService.registrazione(any(Utente.class))).thenReturn(new Utente());
        when(utenteService.mapToDTO(any(Utente.class))).thenReturn(new UtenteDTO());

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegistrazione_Fedele_SenzaCasaProduzione() throws Exception {
        validDto.setDtype("Fedele");
        validDto.setCasaProduzione(null);
        validDto.setCreditReference("Nome Utente Valido");

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Casa di produzione non trovata o formato errato"));
    }

    @Test
    void testRegistrazione_Fedele_SenzaCredit() throws Exception {
        validDto.setDtype("Fedele");
        validDto.setCasaProduzione("Warner Bros");
        validDto.setCreditReference(null);

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: credit reference non valida"));
    }

    @Test
    void testRegistrazione_Fedele_CreditErrato() throws Exception {
        validDto.setDtype("Fedele");
        validDto.setCasaProduzione("Warner Bros");
        validDto.setCreditReference("Nome con numeri 123"); // Non rispetta REGEX (solo lettere)

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: credit reference non valida"));
    }

    @Test
    void testRegistrazione_Fedele_Successo() throws Exception {
        validDto.setDtype("Fedele");
        validDto.setCasaProduzione("Warner Bros");

        // CORREZIONE: Usiamo una stringa che rispetta la REGEX_GENERICA_FEDELE (solo lettere, spazi, trattini, apostrofi)
        validDto.setCreditReference("Nome Portfolio Personale");

        validDto.setTestataGiornalistica(null);

        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(utenteService.registrazione(any(Utente.class))).thenReturn(new Utente());
        when(utenteService.mapToDTO(any(Utente.class))).thenReturn(new UtenteDTO());

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
    }

    // ===================================================================================
    // TEST DUPLICATI E ECCEZIONI
    // ===================================================================================

    @Test
    void testRegistrazione_UsernameEsistente() throws Exception {
        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(true);
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username già in uso."));
    }

    @Test
    void testRegistrazione_EmailEsistente() throws Exception {
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(true);
        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email già registrata."));
    }

    @Test
    void testRegistrazione_ErroreInterno() throws Exception {
        // Simula la condizione che porta al RuntimeException: DB Error.
        // Nota: Questo test sembra essere involontariamente interrotto dal 400 del test precedente
        // se l'ambiente non viene pulito bene, ma dovrebbe comunque passare se il test precedente è corretto.

        // Assicurati che i dati siano validi per non fallire la validazione DTO/Controller
        validDto.setDtype("Cinefilo");
        validDto.setTestataGiornalistica(null);
        validDto.setCasaProduzione(null);
        validDto.setCreditReference(null);

        when(utenteService.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(utenteService.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(utenteService.registrazione(any(Utente.class))).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(post("/api/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());
    }
}