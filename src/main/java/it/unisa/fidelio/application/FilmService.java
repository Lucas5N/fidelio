package it.unisa.fidelio.application;

import it.unisa.fidelio.application.GenreService;
import it.unisa.fidelio.application.TmdbClient;
import it.unisa.fidelio.presentation.FilmCardDto;
import it.unisa.fidelio.presentation.GestioneFilmRequestDTO;
import it.unisa.fidelio.presentation.TmdbMovieDto;
import it.unisa.fidelio.storage.ListaPrivata;
import it.unisa.fidelio.storage.ListaPrivataRepository;
import it.unisa.fidelio.storage.UtenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FilmService {

    private final TmdbClient tmdbClient;
    private final GenreService genreService;
    private final ListaPrivataRepository listaPrivataRepo;
    private final UtenteRepository utenteRepo;
    private final String posterBase;

    // Mappa nome lista → stato corrispondente
    private static final Map<String, String> NOME_TO_STATO = Map.of(
            "Film Visti", "VISTO",
            "Da Vedere", "DA_VEDERE",
            "Preferiti", "PREFERITO"
    );

    public FilmService(TmdbClient tmdbClient,
                       GenreService genreService,
                       ListaPrivataRepository listaPrivataRepo,
                       UtenteRepository utenteRepo,
                       @Value("${tmdb.poster-base}") String posterBase) {
        this.tmdbClient = tmdbClient;
        this.genreService = genreService;
        this.listaPrivataRepo = listaPrivataRepo;
        this.utenteRepo = utenteRepo;
        this.posterBase = posterBase;
    }


    // ===================================================================
    // 1. RICERCA FILM SU TMDB
    // ===================================================================
    public List<FilmCardDto> ricercaFilm(String query, int page) {
        var response = tmdbClient.searchMovies(query, page);
        Map<Integer, String> genreMap = genreService.getGenreMap();

        return response.results().stream()
                .map(movieDto -> toFilmCardDto(movieDto, genreMap))
                .toList();
    }

    // ===================================================================
    // 2. AGGIUNGI / AGGIORNA FILM NELLA LISTA PERSONALE
    // ===================================================================
    @Transactional
    public void gestisciFilmNellaLista(String username, GestioneFilmRequestDTO request) {
        var utente = utenteRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        String statoRichiesto = request.getStato().toUpperCase();
        String nomeLista = switch (statoRichiesto) {
            case "VISTO" -> "Film Visti";
            case "DA_VEDERE" -> "Da Vedere";
            case "PREFERITO" -> "Preferiti";
            default -> throw new IllegalArgumentException("Stato non valido: " + request.getStato());
        };

        ListaPrivata lista = listaPrivataRepo
                .findByProprietarioUsernameAndNome(username, nomeLista)
                .orElseThrow(() -> new IllegalStateException("Lista predefinita mancante: " + nomeLista));

        Long tmdbId = request.getTmdbId();

        // Aggiungi sempre il tmdbId alla lista
        lista.getTmdbIds().add(tmdbId);

        // Gestisci data visione (solo per "Film Visti")
        if ("VISTO".equals(statoRichiesto)) {
            if (request.getDataVisione() != null) {
                lista.getDataVisioneTmdb().put(tmdbId, request.getDataVisione());
            }
        } else {
            // Rimuovi eventuale data visione se sposto da "Visti" a altra lista
            lista.getDataVisioneTmdb().remove(tmdbId);
        }

        listaPrivataRepo.save(lista);
    }

    // ===================================================================
    // 3. RECUPERA TUTTI I FILM DELL'UTENTE (dalle 3 liste speciali)
    // ===================================================================
    public List<FilmCardDto> getMieiFilm(String username) {
        List<ListaPrivata> liste = listaPrivataRepo.findByProprietarioUsernameAndNomeIn(
                username,
                List.of("Film Visti", "Da Vedere", "Preferiti")
        );

        Set<Long> tuttiTmdbId = new HashSet<>();
        for (ListaPrivata lista : liste) {
            tuttiTmdbId.addAll(lista.getTmdbIds());
        }

        if (tuttiTmdbId.isEmpty()) {
            return List.of();
        }

        Map<Integer, String> genreMap = genreService.getGenreMap();

        return tuttiTmdbId.stream()
                .map(tmdbId -> {
                    var movieDto = tmdbClient.getMovieDetails(tmdbId);
                    return toFilmCardDto(movieDto, genreMap);
                })
                .toList();
    }

    // ===================================================================
    // 4. (Opzionale futuro) RACCOMANDATI
    // ===================================================================
    public List<FilmCardDto> getFilmRaccomandati(String username) {
        // TODO: implementare FRA quando sarà il momento
        return List.of();
    }

    // ===================================================================
    // METODO PRIVATO DI MAPPING (riutilizzato da HomeService)
    // ===================================================================
    private FilmCardDto toFilmCardDto(TmdbMovieDto m, Map<Integer, String> genreMap) {
        Integer year = null;
        if (m.releaseDate() != null && m.releaseDate().length() >= 4) {
            year = Integer.parseInt(m.releaseDate().substring(0, 4));
        }

        String posterUrl = (m.posterPath() == null) ? null : posterBase + m.posterPath();

        List<String> genreNames = (m.genreIds() == null || m.genreIds().isEmpty())
                ? List.of()
                : m.genreIds().stream()
                .map(id -> genreMap.getOrDefault(id, "Sconosciuto"))
                .toList();

        return new FilmCardDto(
                m.id(),
                m.title(),
                year,
                m.voteAverage(),
                posterUrl,
                m.genreIds() == null ? List.of() : m.genreIds(),
                genreNames
        );
    }
}