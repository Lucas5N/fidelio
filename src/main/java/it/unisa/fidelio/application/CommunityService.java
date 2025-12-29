package it.unisa.fidelio.application;

import it.unisa.fidelio.storage.Community;
import it.unisa.fidelio.storage.CommunityRepository;
import it.unisa.fidelio.storage.Utente;
import it.unisa.fidelio.storage.UtenteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepo;
    private final UtenteRepository utenteRepo;

    public CommunityService(CommunityRepository communityRepo, UtenteRepository utenteRepo) {
        this.communityRepo = communityRepo;
        this.utenteRepo = utenteRepo;
    }

    public Community creaCommunity(String nome, String descrizione, int creatoreId) {
        Utente creatore = utenteRepo.findById(creatoreId).orElseThrow();

        // Controllo business: Solo i Fedeli possono creare community?
        // if (!"Fedele".equals(creatore.getDtype())) throw new RuntimeException("Solo i fedeli possono creare community");

        Community c = new Community();
        c.setNome(nome);
        c.setDescrizione(descrizione);
        c.setCreatore(creatore);
        c.setDataCreazione(Instant.from(LocalDateTime.now()));
        c.setNumMembri(1); // Il creatore è il primo membro

        return communityRepo.save(c);
    }

    public List<Community> trovaTutte() {
        return communityRepo.findAll();
    }
}
