package it.unisa.fidelio.application.controller; // O semplicemente 'application' se non usi sottocartelle

import it.unisa.fidelio.application.CommunityService;
import it.unisa.fidelio.presentation.CommunityDTO;
import it.unisa.fidelio.presentation.ThreadDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class CommunityController {

    private final CommunityService communityService;

    @Autowired
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    // --- ENDPOINTS CRUD ---

    /**
     * GET /api/community
     * Restituisce tutte le community
     */
    @GetMapping
    public ResponseEntity<List<CommunityDTO>> getAllCommunities() {
        List<CommunityDTO> communities = communityService.findAll();
        return ResponseEntity.ok(communities);
    }

    //TODO forse non scelta finale chiedere l'id in URL (token sicurezza?)
    /**
     * GET /api/community/{id}
     * Restituisce una singola community per ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getCommunityById(@PathVariable Integer id) {
        try {
            CommunityDTO community = communityService.findById(id);
            return ResponseEntity.ok(community);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/community
     * Crea una nuova community.
     * Richiede i dati nel body e l'ID del creatore come parametro query.
     * Esempio: POST /api/community?creatoreId=1
     */
    @PostMapping
    public ResponseEntity<CommunityDTO> createCommunity(
            @RequestBody CommunityDTO communityDTO,
            @RequestParam Integer creatoreId) {

        // TODO: In produzione, estrarre creatoreId dal Principal/SecurityContext
        try {
            CommunityDTO created = communityService.creaCommunity(communityDTO, creatoreId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/community/{id}
     * Aggiorna i dati di una community esistente
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommunityDTO> updateCommunity(
            @PathVariable Integer id,
            @RequestBody CommunityDTO communityDTO) {

        try {
            CommunityDTO updated = communityService.aggiornaCommunity(id, communityDTO);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/community/{id}
     * Elimina una community
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Integer id) {
        try {
            communityService.eliminaCommunity(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- ENDPOINTS ISCRIZIONE ---

    /**
     * POST /api/community/{id}/iscrizione
     * Iscrive un utente alla community.
     * Esempio: POST /api/community/5/iscrizione?utenteId=10
     */
    @PostMapping("/{id}/iscrizione")
    public ResponseEntity<String> joinCommunity(
            @PathVariable Integer id,
            @RequestParam Integer utenteId) {

        try {
            communityService.iscriviUtente(id, utenteId);
            return ResponseEntity.ok("Iscrizione avvenuta con successo");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/community/{id}/iscrizione
     * Rimuove l'iscrizione di un utente.
     * Esempio: DELETE /api/community/5/iscrizione?utenteId=10
     */
    @DeleteMapping("/{id}/iscrizione")
    public ResponseEntity<String> leaveCommunity(
            @PathVariable Integer id,
            @RequestParam Integer utenteId) {

        try {
            communityService.disiscriviUtente(id, utenteId);
            return ResponseEntity.ok("Disiscrizione avvenuta con successo"); // O 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        // ==========================================
    // ENDPOINTS PER I THREAD (Nested Resources)
    // ==========================================

    /**
     * GET /api/community/{communityId}/threads
     * Legge tutti i thread di quella community
     */
    @GetMapping("/{communityId}/threads")
    public ResponseEntity<List<ThreadDTO>> getThreads(@PathVariable Integer communityId) {
        try {
            return ResponseEntity.ok(communityService.findThreadsByCommunity(communityId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/community/{communityId}/threads
     * Crea un thread IN quella community
     */
    @PostMapping("/{communityId}/threads")
    public ResponseEntity<?> createThread(
            @PathVariable Integer communityId,
            @RequestBody ThreadDTO threadDTO,
            @RequestParam Integer autoreId) {
        try {
            ThreadDTO created = communityService.creaThread(communityId, threadDTO, autoreId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * DELETE /api/community/threads/{threadId}
     * Elimina un thread specifico.
     * Nota: Qui non serve l'ID della community nell'URL perché l'ID del thread è univoco.
     */
    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<Void> deleteThread(@PathVariable Integer threadId) {
        try {
            communityService.eliminaThread(threadId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}