package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.CommentoDTO;
import it.unisa.fidelio.presentation.PopularReviewViewDTO;
import it.unisa.fidelio.presentation.TmdbReviewResponseDTO;
import it.unisa.fidelio.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecensioneServiceTest {

    @Mock
    private RecensioneRepository recensioneRepo;
    @Mock
    private CommentoRepository commentoRepo;
    @Mock
    private TmdbClient tmdbClient;
    @Mock
    private RecensioneInterazioneRepository recensioneInterazioneRepository;
    @Mock
    private UtenteRepository utenteRepository;
    @Mock
    private SegnalazioneRepository segnalazioneRepository;

    @InjectMocks
    private RecensioneService recensioneService;

    // ===================================================================================
    // 1. TEST DEI METODI "GETTER" (Coprono le barre rosse 0% iniziali)
    // ===================================================================================

    @Test
    void testGetLikeGiaFatti() {
        int userId = 1;
        Recensione r1 = new Recensione(); r1.setId(10);
        RecensioneInterazione i1 = new RecensioneInterazione(); i1.setRecensione(r1);

        // CORRETTO: Usa Set.of perché il Repository restituisce un Set
        when(recensioneInterazioneRepository.findByUtenteIdAndTipo(userId, RecensioneInterazione.TipoInterazione.LIKE))
                .thenReturn(Set.of(i1));

        Set<Integer> result = recensioneService.getLikeGiaFatti(userId);

        assertEquals(1, result.size());
        assertTrue(result.contains(10));
    }

    @Test
    void testGetDislikeGiaFatti() {
        int userId = 1;
        Recensione r1 = new Recensione(); r1.setId(5);
        RecensioneInterazione i1 = new RecensioneInterazione(); i1.setRecensione(r1);

        when(recensioneInterazioneRepository.findByUtenteIdAndTipo(userId, RecensioneInterazione.TipoInterazione.DISLIKE))
                .thenReturn(Set.of(i1));

        Set<Integer> result = recensioneService.getDislikeGiaFatti(userId);
        assertTrue(result.contains(5));
    }

    @Test
    void testGetSegnalazioniGiaFatte() {
        int userId = 99;
        Recensione r = new Recensione(); r.setId(100);
        Segnalazione s = new Segnalazione(); s.setRecensione(r);

        // SegnalazioneRepository solitamente restituisce List, ma adattiamo in base all'uso
        when(segnalazioneRepository.findByAutoreId(userId)).thenReturn(List.of(s));

        Set<Integer> result = recensioneService.getSegnalazioniGiaFatte(userId);
        assertTrue(result.contains(100));
    }

    @Test
    void testGetTutteLeRecensioniSegnalate() {
        Long targetFilmId = 555L;
        Long otherFilmId = 999L;

        Recensione r1 = new Recensione(); r1.setId(1); r1.setFilmTmdbId(targetFilmId);
        Recensione r2 = new Recensione(); r2.setId(2); r2.setFilmTmdbId(otherFilmId);

        Segnalazione s1 = new Segnalazione(); s1.setRecensione(r1);
        Segnalazione s2 = new Segnalazione(); s2.setRecensione(r2);

        when(segnalazioneRepository.findAll()).thenReturn(List.of(s1, s2));

        Set<Integer> result = recensioneService.getTutteLeRecensioniSegnalate(targetFilmId);

        assertEquals(1, result.size());
        assertTrue(result.contains(1));
    }

    // ===================================================================================
    // 2. TEST LOGICA COMPLESSA (TMDB + Mapping Commenti + Stelle)
    // ===================================================================================

    @Test
    void testGetTutteLeRecensioni_Completo() {
        Long filmId = 123L;

        // --- Setup Recensione Locale ---
        Utente u = new Utente(); u.setUsername("Mario"); u.setDtype("Standard");
        Recensione rLoc = new Recensione();
        rLoc.setId(1);
        rLoc.setAutore(u);
        rLoc.setVoto(4.0); // 4.0 -> ★★★★☆
        rLoc.setDataCreazione(Instant.now());
        rLoc.setTesto("Locale");
        rLoc.setNumLike(0); rLoc.setNumDislike(0);

        when(recensioneRepo.findByFilmTmdbIdOrderByNumLikeDesc(filmId)).thenReturn(List.of(rLoc));

        // Mock commenti vuoti per questa recensione
        when(commentoRepo.findByRecensioneIdOrderByDataCreazioneAsc(1)).thenReturn(Collections.emptyList());

        // --- Setup TMDB Response (Record) ---
        // Caso A: Rating presente (8.0 -> /2 = 4 stelle)
        TmdbReviewResponseDTO.AuthorDetails detailsWithRating = new TmdbReviewResponseDTO.AuthorDetails(
                "Name1", "User1", null, 8.0);
        TmdbReviewResponseDTO.TmdbReviewDTO reviewWithRating = new TmdbReviewResponseDTO.TmdbReviewDTO(
                "id1", "Author1", detailsWithRating, "Content1", "2023-01-01T10:00:00Z", "url1");

        // Caso B: Rating null (-> "—")
        TmdbReviewResponseDTO.AuthorDetails detailsNullRating = new TmdbReviewResponseDTO.AuthorDetails(
                "Name2", "User2", null, null);
        TmdbReviewResponseDTO.TmdbReviewDTO reviewNullRating = new TmdbReviewResponseDTO.TmdbReviewDTO(
                "id2", "Author2", detailsNullRating, "Content2", "2023-01-01", "url2");

        TmdbReviewResponseDTO tmdbRes = new TmdbReviewResponseDTO(
                123, 1, List.of(reviewWithRating, reviewNullRating), 1, 2
        );

        when(tmdbClient.getMovieReviews(filmId, 1)).thenReturn(tmdbRes);

        // --- Esecuzione ---
        List<PopularReviewViewDTO> result = recensioneService.getTutteLeRecensioni(filmId);

        // --- Verifiche ---
        assertEquals(3, result.size()); // 1 locale + 2 TMDB

        // Verifica stelle recensione locale (4.0 -> 4 stelle)
        assertEquals("★★★★☆", result.get(0).starsText());

        // Verifica stelle TMDB (8.0 -> 4 stelle)
        assertEquals("★★★★☆", result.get(1).starsText());

        // Verifica stelle TMDB Null (-> "—")
        assertEquals("—", result.get(2).starsText());
    }

    @Test
    void testGetTutteLeRecensioni_VerificaMappingCommenti() {
        // Questo test copre indirettamente il metodo privato mapCommentoToDto
        Long filmId = 100L;

        Utente autoreRec = new Utente();
        autoreRec.setUsername("Reviewer");
        autoreRec.setDtype("S");

        Recensione r = new Recensione();
        r.setId(1);
        r.setAutore(autoreRec);
        r.setVoto(3.0);
        r.setDataCreazione(Instant.now());
        r.setTesto("T");

        // --- FIX: Inizializziamo Like e Dislike per evitare NullPointerException ---
        r.setNumLike(0);
        r.setNumDislike(0);
        // --------------------------------------------------------------------------

        // Setup Commento
        Utente autoreComm = new Utente();
        autoreComm.setUsername("Commentator");
        autoreComm.setDtype("Standard");

        Commento c = new Commento();
        c.setId(50);
        c.setTesto("Mio Commento");
        c.setAutore(autoreComm);
        c.setDataCreazione(Instant.parse("2023-01-01T12:00:00Z"));

        when(recensioneRepo.findByFilmTmdbIdOrderByNumLikeDesc(filmId)).thenReturn(List.of(r));
        when(commentoRepo.findByRecensioneIdOrderByDataCreazioneAsc(r.getId())).thenReturn(List.of(c));
        when(tmdbClient.getMovieReviews(anyLong(), anyInt())).thenReturn(null); // TMDB vuoto

        List<PopularReviewViewDTO> result = recensioneService.getTutteLeRecensioni(filmId);

        // Verifichiamo che i commenti siano stati mappati correttamente nel DTO
        assertFalse(result.isEmpty());
        List<CommentoDTO> commentiDto = result.get(0).comments();
        assertEquals(1, commentiDto.size());

        CommentoDTO dto = commentiDto.get(0);
        assertEquals("Commentator", dto.username());
        assertEquals("Mio Commento", dto.getTesto());
    }

    // ===================================================================================
    // 3. TEST SCRITTURA E CANCELLAZIONE (TRANSAZIONALI)
    // ===================================================================================

    @Test
    void testScriviRecensione() {
        Utente autore = new Utente();
        Long filmId = 555L;
        String testo = "Recensione test";
        double voto = 4.5;
        boolean spoiler = true;

        // Intercettiamo l'oggetto salvato
        when(recensioneRepo.save(any(Recensione.class))).thenAnswer(i -> i.getArgument(0));

        Recensione result = recensioneService.scriviRecensione(autore, filmId, testo, voto, spoiler);

        assertNotNull(result);
        assertEquals(filmId, result.getFilmTmdbId());
        assertEquals(0, result.getNumLike());     // Default
        assertEquals(0, result.getNumDislike());  // Default
        assertNotNull(result.getDataCreazione()); // Data settata
    }

    @Test
    void testEliminaRecensione_AdminPuoCancellareAltrui() {
        Integer recId = 10;
        Utente admin = new Utente();
        admin.setId(1);
        admin.setAmministratore(true);

        Utente altroUser = new Utente();
        altroUser.setId(2);

        Recensione r = new Recensione();
        r.setId(recId);
        r.setAutore(altroUser);

        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));

        recensioneService.eliminaRecensione(recId, admin);

        verify(recensioneRepo).delete(r);
    }

    @Test
    void testEliminaRecensione_UtenteNonAutorizzato_LanciaEccezione() {
        Integer recId = 10;
        Utente userNormale = new Utente();
        userNormale.setId(1);
        userNormale.setAmministratore(false);

        Utente altroUser = new Utente();
        altroUser.setId(2);

        Recensione r = new Recensione();
        r.setId(recId);
        r.setAutore(altroUser);

        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));

        assertThrows(SecurityException.class, () ->
                recensioneService.eliminaRecensione(recId, userNormale)
        );
        verify(recensioneRepo, never()).delete(any());
    }

    // ===================================================================================
    // 4. TEST INTERAZIONI (LIKE / DISLIKE)
    // ===================================================================================

    @Test
    void testAggiungiLike_Successo() {
        int userId = 1;
        int recId = 10;

        Utente autore = new Utente(); autore.setId(2);
        Recensione r = new Recensione();
        r.setId(recId);
        r.setAutore(autore);
        r.setNumLike(0);

        when(recensioneInterazioneRepository.existsByUtenteIdAndRecensioneIdAndTipo(anyInt(), anyInt(), any())).thenReturn(false);
        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));
        when(utenteRepository.getReferenceById(userId)).thenReturn(new Utente());

        recensioneService.aggiungiLike(recId, userId);

        assertEquals(1, r.getNumLike());
        verify(recensioneRepo).save(r);
        verify(recensioneInterazioneRepository).save(any(RecensioneInterazione.class));
    }

    @Test
    void testAggiungiDislike_Successo() {
        // Copre il ramo 'else' di gestisciInterazione
        int recId = 20;
        int userId = 2;
        Utente autore = new Utente(); autore.setId(99);
        Recensione r = new Recensione();
        r.setId(recId);
        r.setAutore(autore);
        r.setNumDislike(5);

        when(recensioneInterazioneRepository.existsByUtenteIdAndRecensioneIdAndTipo(userId, recId, RecensioneInterazione.TipoInterazione.DISLIKE)).thenReturn(false);
        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));
        when(utenteRepository.getReferenceById(userId)).thenReturn(new Utente());

        recensioneService.aggiungiDislike(recId, userId);

        assertEquals(6, r.getNumDislike());
        verify(recensioneRepo).save(r);
        // Verifica salvataggio tipo DISLIKE
        verify(recensioneInterazioneRepository).save(argThat(i -> i.getTipo() == RecensioneInterazione.TipoInterazione.DISLIKE));
    }

    @Test
    void testAggiungiLike_GiaPresente_NonFaNulla() {
        int userId = 1;
        int recId = 10;
        when(recensioneInterazioneRepository.existsByUtenteIdAndRecensioneIdAndTipo(
                userId, recId, RecensioneInterazione.TipoInterazione.LIKE)).thenReturn(true);

        recensioneService.aggiungiLike(recId, userId);

        verify(recensioneRepo, never()).findById(any());
        verify(recensioneRepo, never()).save(any());
    }

    @Test
    void testAggiungiLike_AutoreVotaSeStesso_NonFaNulla() {
        int userId = 1;
        int recId = 10;
        Utente autore = new Utente(); autore.setId(userId);
        Recensione r = new Recensione(); r.setId(recId); r.setAutore(autore);

        when(recensioneInterazioneRepository.existsByUtenteIdAndRecensioneIdAndTipo(anyInt(), anyInt(), any())).thenReturn(false);
        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));

        recensioneService.aggiungiLike(recId, userId);

        verify(recensioneRepo, never()).save(any());
    }

    // ===================================================================================
    // 5. TEST COMMENTI E SEGNALAZIONI (ECCEZIONI)
    // ===================================================================================

    @Test
    void testAggiungiCommento() {
        int recId = 30;
        String testo = "Commento";
        Utente autore = new Utente();
        Recensione r = new Recensione(); r.setId(recId);

        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));

        recensioneService.aggiungiCommento(recId, autore, testo);

        verify(commentoRepo).save(any(Commento.class));
    }

    @Test
    void testSegnalaRecensione_MotivoNullo() {
        int recId = 10;
        int autoreSegId = 5;
        Recensione r = new Recensione();
        Utente autoreRec = new Utente(); autoreRec.setId(99);
        r.setId(recId); r.setAutore(autoreRec);

        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));
        when(segnalazioneRepository.existsByRecensioneIdAndAutoreId(recId, autoreSegId)).thenReturn(false);
        when(utenteRepository.getReferenceById(autoreSegId)).thenReturn(new Utente());

        // Motivo NULL -> default
        recensioneService.segnalaRecensione(recId, autoreSegId, null);

        verify(segnalazioneRepository).save(argThat(s -> s.getMotivo().equals("Contenuto inappropriato")));
    }

    @Test
    void testSegnalaRecensione_AutoreSegnalaSeStesso() {
        int id = 1;
        Utente u = new Utente(); u.setId(id);
        Recensione r = new Recensione(); r.setAutore(u);
        when(recensioneRepo.findById(anyInt())).thenReturn(Optional.of(r));

        assertThrows(IllegalArgumentException.class, () -> recensioneService.segnalaRecensione(10, id, "Spam"));
    }

    @Test
    void testSegnalaRecensione_GiaSegnalata() {
        int recId = 10;
        int autoreId = 5;
        Recensione r = new Recensione();
        Utente autoreRec = new Utente(); autoreRec.setId(99);
        r.setAutore(autoreRec);

        when(recensioneRepo.findById(recId)).thenReturn(Optional.of(r));
        when(segnalazioneRepository.existsByRecensioneIdAndAutoreId(recId, autoreId)).thenReturn(true);

        // Verifica eccezione
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                recensioneService.segnalaRecensione(recId, autoreId, "Spam")
        );
        assertEquals("Hai già segnalato questa recensione.", e.getMessage());
    }
}