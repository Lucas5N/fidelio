package it.unisa.fidelio.businesslogic;

import it.unisa.fidelio.dataaccess.Segnalazione;
import it.unisa.fidelio.dataaccess.SegnalazioneRepository;
import it.unisa.fidelio.dataaccess.Utente;
import it.unisa.fidelio.dataaccess.UtenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final SegnalazioneRepository segnalazioneRepo;
    private final UtenteRepository utenteRepo;

    public AdminService(SegnalazioneRepository segnalazioneRepo, UtenteRepository utenteRepo) {
        this.segnalazioneRepo = segnalazioneRepo;
        this.utenteRepo = utenteRepo;
    }

    public List<Segnalazione> getSegnalazioniAperte() {
        return segnalazioneRepo.findByStato("APERTA");
    }

    public void chiudiSegnalazione(int idSegnalazione, String esito, int idAdmin) {
        Segnalazione s = segnalazioneRepo.findById(idSegnalazione).orElseThrow();
        Utente admin = utenteRepo.findById(idAdmin).orElseThrow();

        s.setStato("CHIUSA");
        s.setEsito(esito);
        s.setGestore(admin); // Associa l'admin che ha risolto
        segnalazioneRepo.save(s);
    }
}
