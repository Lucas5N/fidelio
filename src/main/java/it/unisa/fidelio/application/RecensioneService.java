package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.CommentoDTO;
import it.unisa.fidelio.presentation.PopularReviewViewDTO;
import it.unisa.fidelio.presentation.TmdbReviewResponseDTO;
import it.unisa.fidelio.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepo;
    private final CommentoRepository commentoRepo;
    private final TmdbClient tmdbClient;
    private final RecensioneInterazioneRepository recensioneInterazioneRepository;
    private final UtenteRepository utenteRepository;
    private final SegnalazioneRepository segnalazioneRepository;

    @Autowired
    public RecensioneService(RecensioneRepository recensioneRepo,
                             CommentoRepository commentoRepo,
                             TmdbClient tmdbClient,
                             RecensioneInterazioneRepository recensioneInterazioneRepository,
                             UtenteRepository utenteRepository,
                             SegnalazioneRepository segnalazioneRepository) {
        this.recensioneRepo = recensioneRepo;
        this.commentoRepo = commentoRepo;
        this.tmdbClient = tmdbClient;
        this.recensioneInterazioneRepository = recensioneInterazioneRepository;
        this.utenteRepository = utenteRepository;
        this.segnalazioneRepository = segnalazioneRepository;
    }

    // === METODI PER THREE-TIER ARCHITECTURE (MovieController) ===

    public java.util.Set<Integer> getLikeGiaFatti(int utenteId) {
        return recensioneInterazioneRepository
                .findByUtenteIdAndTipo(utenteId, RecensioneInterazione.TipoInterazione.LIKE)
                .stream()
                .map(inter -> inter.getRecensione().getId())
                .collect(java.util.stream.Collectors.toSet());
    }

    public java.util.Set<Integer> getDislikeGiaFatti(int utenteId) {
        return recensioneInterazioneRepository
                .findByUtenteIdAndTipo(utenteId, RecensioneInterazione.TipoInterazione.DISLIKE)
                .stream()
                .map(inter -> inter.getRecensione().getId())
                .collect(java.util.stream.Collectors.toSet());
    }

    public java.util.Set<Integer> getSegnalazioniGiaFatte(int utenteId) {
        return segnalazioneRepository
                .findByAutoreId(utenteId)
                .stream()
                .map(seg -> seg.getRecensione().getId())
                .collect(java.util.stream.Collectors.toSet());
    }

    public List<PopularReviewViewDTO> getTutteLeRecensioni(Long filmId) {
        List<PopularReviewViewDTO> listaFinale = new ArrayList<>();

        // RECENSIONI LOCALI
        List<Recensione> locali = recensioneRepo.findByFilmTmdbIdOrderByNumLikeDesc(filmId);
        for (Recensione r : locali) {
            List<CommentoDTO> commentiLocali = commentoRepo
                    .findByRecensioneIdOrderByDataCreazioneAsc(r.getId())
                    .stream()
                    .map(this::mapCommentoToDto)
                    .collect(Collectors.toList());

            listaFinale.add(new PopularReviewViewDTO(
                    r.getAutore().getUsername(),
                    r.getAutore().getUsername(),
                    r.getAutore().getUsername().substring(0, 1).toUpperCase(),
                    generateStarsText(r.getVoto()),
                    r.getDataCreazione().toString().substring(0, 10),
                    r.getTesto(),
                    null,
                    true,
                    r.getId(),
                    null,
                    r.getNumLike(),
                    r.getNumDislike(),
                    commentiLocali,
                    r.getAutore().getDtype()  // per badge critico
            ));
        }

        // RECENSIONI TMDB
        TmdbReviewResponseDTO tmdbRes = tmdbClient.getMovieReviews(filmId, 1);
        if (tmdbRes != null && tmdbRes.results() != null) {
            tmdbRes.results().stream().limit(10).forEach(tr -> {
                listaFinale.add(new PopularReviewViewDTO(
                        tr.author(),
                        tr.author(),
                        "T",
                        ratingToStars(tr.authorDetails() != null ? tr.authorDetails().rating() : null),
                        tr.createdAt().length() >= 10 ? tr.createdAt().substring(0, 10) : tr.createdAt(),
                        tr.content(),
                        tr.url(),
                        false,
                        null,
                        tr.id(),
                        0,
                        0,
                        List.of(),
                        null
                ));
            });
        }

        return listaFinale;
    }

    @Transactional
    public Recensione scriviRecensione(Utente autore, Long filmTmdbId, String testo, double voto, boolean spoiler) {
        Recensione r = new Recensione();
        r.setAutore(autore);
        r.setFilmTmdbId(filmTmdbId);
        r.setTesto(testo);
        r.setVoto(voto);
        r.setSpoilerAlert(spoiler);
        r.setDataCreazione(Instant.now());
        r.setNumLike(0);
        r.setNumDislike(0);
        return recensioneRepo.save(r);
    }

    @Transactional
    public void aggiornaRecensione(Integer recensioneId, String testo, Double voto, Utente utenteCorrente) {
        Recensione r = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));
        if (!r.getAutore().getId().equals(utenteCorrente.getId())) {
            throw new SecurityException("Non autorizzato");
        }
        r.setTesto(testo);
        r.setVoto(voto);
        recensioneRepo.save(r);
    }

    @Transactional
    public void eliminaRecensione(Integer recensioneId, Utente utenteCorrente) {
        Recensione recensione = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));

        if (!recensione.getAutore().getId().equals(utenteCorrente.getId())) {
            throw new SecurityException("Non autorizzato");
        }

        recensioneRepo.delete(recensione);
    }

    @Transactional
    public void aggiungiLike(Integer recensioneId, int utenteId) {
        if (recensioneInterazioneRepository.existsByUtenteIdAndRecensioneIdAndTipo(
                utenteId, recensioneId, RecensioneInterazione.TipoInterazione.LIKE)) {
            return;
        }

        Recensione r = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));

        if (r.getAutore().getId().equals(utenteId)) {
            return;
        }

        r.setNumLike(r.getNumLike() + 1);
        recensioneRepo.save(r);

        RecensioneInterazione inter = new RecensioneInterazione();
        inter.setUtente(utenteRepository.getReferenceById(utenteId));
        inter.setRecensione(r);
        inter.setTipo(RecensioneInterazione.TipoInterazione.LIKE);
        recensioneInterazioneRepository.save(inter);
    }

    @Transactional
    public void aggiungiDislike(Integer recensioneId, int utenteId) {
        if (recensioneInterazioneRepository.existsByUtenteIdAndRecensioneIdAndTipo(
                utenteId, recensioneId, RecensioneInterazione.TipoInterazione.DISLIKE)) {
            return;
        }

        Recensione r = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));

        if (r.getAutore().getId().equals(utenteId)) {
            return;
        }

        r.setNumDislike(r.getNumDislike() + 1);
        recensioneRepo.save(r);

        RecensioneInterazione inter = new RecensioneInterazione();
        inter.setUtente(utenteRepository.getReferenceById(utenteId));
        inter.setRecensione(r);
        inter.setTipo(RecensioneInterazione.TipoInterazione.DISLIKE);
        recensioneInterazioneRepository.save(inter);
    }

    @Transactional
    public void aggiungiCommento(Integer recensioneId, Utente autore, String testo) {
        Recensione recensione = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));

        // Removed restriction: authors can now comment on their own reviews
        Commento commento = new Commento();
        commento.setAutore(autore);
        commento.setTesto(testo);
        commento.setDataCreazione(Instant.now());
        commento.setRecensione(recensione);
        commentoRepo.save(commento);
    }

    // === FUNZIONE SEGNALAZIONE RECENSIONE ===
    @Transactional
    public void segnalaRecensione(Integer recensioneId, int autoreId, String motivo) {
        Recensione recensione = recensioneRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));

        // 1. Non puoi segnalare la tua recensione
        if (recensione.getAutore().getId().equals(autoreId)) {
            throw new IllegalArgumentException("Non puoi segnalare la tua recensione.");
        }

        // 2. Non puoi segnalare due volte la stessa recensione
        if (segnalazioneRepository.existsByRecensioneIdAndAutoreId(recensioneId, autoreId)) {
            throw new IllegalArgumentException("Hai già segnalato questa recensione.");
        }

        // Crea la segnalazione
        Segnalazione segnalazione = new Segnalazione();
        segnalazione.setRecensione(recensione);
        segnalazione.setAutore(utenteRepository.getReferenceById(autoreId));
        segnalazione.setMotivo(motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Contenuto inappropriato");
        segnalazione.setDataSegnalazione(Instant.now());
        segnalazione.setStato("APERTA");  // come da tuo @ColumnDefault

        segnalazioneRepository.save(segnalazione);
    }

    private CommentoDTO mapCommentoToDto(Commento c) {
        String username = c.getAutore().getUsername();
        return new CommentoDTO(
                c.getId(),
                c.getTesto(),
                username,
                c.getDataCreazione().toString().substring(0, 10),
                username.substring(0, 1).toUpperCase(),
                c.getAutore().getDtype()
        );
    }

    private String generateStarsText(Double voto) {
        if (voto == null) return "☆☆☆☆☆";
        int v = voto.intValue();
        return "★".repeat(Math.min(5, v)) + "☆".repeat(Math.max(0, 5 - v));
    }

    private String ratingToStars(Double rating10) {
        if (rating10 == null) return "—";
        int stars = (int) Math.round(rating10 / 2.0);
        stars = Math.max(0, Math.min(5, stars));
        return "★".repeat(stars) + "☆".repeat(5 - stars);
    }
}