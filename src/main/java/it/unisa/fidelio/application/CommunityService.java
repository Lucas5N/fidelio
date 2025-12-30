package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.CommunityDTO;
import it.unisa.fidelio.presentation.UtenteDTO; // Assumo esista
import it.unisa.fidelio.storage.Community;
import it.unisa.fidelio.storage.CommunityRepository;
import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepo;
    private final UtenteRepository utenteRepo;

    @Autowired
    public CommunityService(CommunityRepository communityRepo, UtenteRepository utenteRepo) {
        this.communityRepo = communityRepo;
        this.utenteRepo = utenteRepo;
    }


    public List<CommunityDTO> findAll() {
        return communityRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CommunityDTO findById(Integer id) {
        Community community = communityRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Community non trovata con ID: " + id));
        return mapToDTO(community);
    }

    public CommunityDTO creaCommunity(CommunityDTO communityDTO, Integer creatoreId) {
        Utente creatore = utenteRepo.findById(creatoreId)
                .orElseThrow(() -> new EntityNotFoundException("Utente creatore non trovato"));

        Community community = new Community();
        community.setNome(communityDTO.getNome());
        community.setDescrizione(communityDTO.getDescrizione());
        community.setCreatore(creatore);
        community.setDataCreazione(Instant.now());
        community.setNumMembri(0); // Inizialmente 0 o 1 se il creatore si iscrive in automatico

        Community saved = communityRepo.save(community);
        return mapToDTO(saved);
    }

    public CommunityDTO aggiornaCommunity(Integer communityId, CommunityDTO datiAggiornati) {
        Community community = communityRepo.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community non trovata"));

        community.setNome(datiAggiornati.getNome());
        community.setDescrizione(datiAggiornati.getDescrizione());

        return mapToDTO(communityRepo.save(community));
    }

    public void eliminaCommunity(Integer communityId) {
        if (!communityRepo.existsById(communityId)) {
            throw new EntityNotFoundException("Impossibile eliminare: Community non trovata");
        }
        communityRepo.deleteById(communityId);
    }


    public void iscriviUtente(Integer communityId, Integer utenteId) {
        Community community = communityRepo.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community non trovata"));

        Utente utente = utenteRepo.findById(utenteId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));


        if (!utente.getCommunitiesIscritte().contains(community)) {
            utente.getCommunitiesIscritte().add(community);

            // Aggiorniamo manualmente il contatore (denormalizzato)
            community.setNumMembri(community.getNumMembri() + 1);

            utenteRepo.save(utente);
            communityRepo.save(community);
        }
    }

    public void disiscriviUtente(Integer communityId, Integer utenteId) {
        Community community = communityRepo.findById(communityId).orElseThrow();
        Utente utente = utenteRepo.findById(utenteId).orElseThrow();

        if (utente.getCommunitiesIscritte().contains(community)) {
            utente.getCommunitiesIscritte().remove(community);

            int nuoviMembri = Math.max(0, community.getNumMembri() - 1);
            community.setNumMembri(nuoviMembri);

            utenteRepo.save(utente);
            communityRepo.save(community);
        }
    }


    private CommunityDTO mapToDTO(Community entity) {
        CommunityDTO dto = new CommunityDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDescrizione(entity.getDescrizione());
        dto.setDataCreazione(entity.getDataCreazione());
        dto.setNumMembri(entity.getNumMembri());

        if (entity.getCreatore() != null) {
            dto.setCreatoreUsername(entity.getCreatore().getUsername());
        }

        return dto;
    }
}