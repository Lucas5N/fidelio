package it.unisa.fidelio.application.controller;

import it.unisa.fidelio.application.UtenteService;
import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User; // Importante: Implementazione concreta
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfiloControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UtenteService utenteService;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Utente utenteTest;

    @BeforeEach
    void setUp() {
        // --- 1. Setup Dati Utente ---
        utenteTest = new Utente();
        utenteTest.setId(1);
        utenteTest.setEmail("mario@email.com");
        utenteTest.setUsername("MarioUser");
        utenteTest.setNome("Mario");
        utenteTest.setCognome("Rossi");
        utenteTest.setViaENumCivico("Via Roma 1");
        utenteTest.setBio("Bio test");
        utenteTest.setPassword("passEncoded");
        utenteTest.setDtype("Cinefilo");
        utenteTest.setRecensioni(new HashSet<>());
        utenteTest.setImmagineProfilo(new byte[0]);
        // Se hai listePrivate, inizializzale: utenteTest.setListePrivate(new HashSet<>());

        // --- 2. Setup View Resolver (Per evitare errore Thymeleaf) ---
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        ProfiloController controller = new ProfiloController(utenteService, utenteRepository, passwordEncoder);

        // --- 3. Setup Resolver per UserDetails (Per evitare errore Constructor) ---
        // Questo pezzo di codice intercetta la richiesta di "UserDetails" e restituisce un oggetto valido
        HandlerMethodArgumentResolver userDetailsResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                // Si attiva se il parametro è di tipo UserDetails
                return UserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                // Restituisce un utente Spring Security valido
                return new User("mario@email.com", "pass", Collections.emptyList());
            }
        };

        // --- 4. Costruzione MockMvc ---
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .setCustomArgumentResolvers(userDetailsResolver) // <--- ECCO IL FIX
                .build();
    }

    @Test
    void testVisualizzaProfilo_Successo() throws Exception {
        // Mock del comportamento del service
        when(utenteService.findByEmail("mario@email.com")).thenReturn(utenteTest);

        // Non serve più .principal() perché il CustomArgumentResolver inietta l'utente automaticamente
        mockMvc.perform(get("/profilo"))
                .andExpect(status().isOk())
                .andExpect(view().name("profiloUtente"))
                .andExpect(model().attribute("utenteCorrente", utenteTest));
    }

    @Test
    void testAggiornaProfilo_Successo() throws Exception {
        when(utenteService.findByEmail("mario@email.com")).thenReturn(utenteTest);
        when(passwordEncoder.encode("nuovaPass123")).thenReturn("encodedNewPass");

        MockMultipartFile fileImmagine = new MockMultipartFile(
                "immagine", "avatar.jpg", "image/jpeg", "content".getBytes()
        );

        mockMvc.perform(multipart("/profilo/aggiorna")
                        .file(fileImmagine)
                        // Non serve .principal(), il resolver fa il lavoro sporco
                        .param("nome", "MarioNew")
                        .param("cognome", "RossiNew")
                        .param("username", "MarioUserNew")
                        .param("email", "mario.new@email.com")
                        .param("viaENumCivico", "Via Napoli 50")
                        .param("bio", "Bio Updated")
                        .param("nuovaPassword", "nuovaPass123")
                        .param("testata", "")
                        .param("casa", "")
                        .param("credit", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profilo"));

        verify(utenteRepository).save(any(Utente.class));
    }
}