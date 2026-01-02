package it.unisa.fidelio.application;

import it.unisa.fidelio.presentation.FilmCardDto;
import it.unisa.fidelio.presentation.GestioneFilmRequestDTO;
import it.unisa.fidelio.presentation.TmdbMovieDetailsDTO;
import it.unisa.fidelio.storage.*;
import it.unisa.fidelio.presentation.TmdbMovieDto;
import it.unisa.fidelio.storage.api_data.TmdbMovieListResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final TmdbClient tmdbClient;
    private final GenreService genreService;
    private final ListaPrivataRepository listaPrivataRepo;
    private final UtenteRepository utenteRepo;
    private final FilmRepository filmRepo;
    private final RecensioneRepository recensioneRepo;
    private final String posterBase;

    private static final Map<String, String> NOME_TO_STATO = Map.of(
            "Film Visti", "VISTO",
            "Da Vedere", "DA_VEDERE",
            "Preferiti", "PREFERITO"
    );

    public FilmService(TmdbClient tmdbClient,
                       GenreService genreService,
                       ListaPrivataRepository listaPrivataRepo,
                       UtenteRepository utenteRepo,
                       FilmRepository filmRepo,
                       RecensioneRepository recensioneRepo,
                       @Value("${tmdb.poster-base}") String posterBase) {
        this.tmdbClient = tmdbClient;
        this.genreService = genreService;
        this.listaPrivataRepo = listaPrivataRepo;
        this.utenteRepo = utenteRepo;
        this.filmRepo = filmRepo;
        this.recensioneRepo = recensioneRepo;
        this.posterBase = posterBase;
    }

    // 1. RICERCA FILM SU TMDB
    public List<FilmCardDto> ricercaFilm(String query, int page) {
        TmdbMovieListResponse response = tmdbClient.searchMovies(query, page);
        Map<Integer, String> genreMap = genreService.getGenreMap();
        return response.results().stream()
                .map(movieDto -> toFilmCardDto(movieDto, genreMap))
                .toList();
    }

    // 2. FILM POPOLARI E NOW PLAYING
    public List<FilmCardDto> getFilmPopolari(int page) {
        TmdbMovieListResponse response = tmdbClient.getPopular(page);
        Map<Integer, String> genreMap = genreService.getGenreMap();
        return response.results().stream()
                .map(movieDto -> toFilmCardDto(movieDto, genreMap))
                .toList();
    }

    public List<FilmCardDto> getNowPlaying(int page) {
        TmdbMovieListResponse response = tmdbClient.getNowPlaying(page);
        Map<Integer, String> genreMap = genreService.getGenreMap();
        return response.results().stream()
                .map(movieDto -> toFilmCardDto(movieDto, genreMap))
                .toList();
    }

    // 3. DETTAGLI COMPLETI DI UN FILM (usa TmdbMovieDetailsDTO)
    public TmdbMovieDetailsDTO getDettagliFilm(Long tmdbId, Utente utenteLoggato) {
        return tmdbClient.getMovieDetails(tmdbId);
    }

    // 4. GESTIONE LISTE PERSONALI
    @Transactional
    public void gestisciFilmNellaLista(String username, GestioneFilmRequestDTO request) {
        Utente utente = utenteRepo.findByUsername(username)
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
        lista.getTmdbIds().add(tmdbId);

        if ("VISTO".equals(statoRichiesto) && request.getDataVisione() != null) {
            lista.getDataVisioneTmdb().put(tmdbId, request.getDataVisione());
        } else {
            lista.getDataVisioneTmdb().remove(tmdbId);
        }

        listaPrivataRepo.save(lista);
    }

    // 5. RECUPERO "I MIEI FILM"
    public List<FilmCardDto> getMieiFilm(String username) {
        List<ListaPrivata> liste = listaPrivataRepo.findByProprietarioUsernameAndNomeIn(
                username, List.of("Film Visti", "Da Vedere", "Preferiti"));

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
                    TmdbMovieDetailsDTO detailsDto = tmdbClient.getMovieDetails(tmdbId);
                    return toFilmCardDto(detailsDto, genreMap);
                })
                .toList();
    }

    // 6. FILM RACCOMANDATI
    public List<FilmCardDto> getFilmRaccomandati(String username) {
        Optional<ListaPrivata> listaVistiOpt = listaPrivataRepo
                .findByProprietarioUsernameAndNome(username, "Film Visti");

        if (listaVistiOpt.isEmpty() || listaVistiOpt.get().getTmdbIds().isEmpty()) {
            return getFilmPopolari(1);
        }

        Set<Long> filmVistiTmdbIds = listaVistiOpt.get().getTmdbIds();

        Set<Integer> generiPreferiti = new HashSet<>();
        Map<Integer, String> genreMap = genreService.getGenreMap();

        for (Long tmdbId : filmVistiTmdbIds) {
            TmdbMovieDetailsDTO details = tmdbClient.getMovieDetails(tmdbId);
            details.genres().stream()
                    .map(TmdbMovieDetailsDTO.TmdbGenre::id)
                    .forEach(generiPreferiti::add);
        }

        if (generiPreferiti.isEmpty()) {
            return getFilmPopolari(1);
        }

        List<FilmCardDto> popolari = getFilmPopolari(1);

        return popolari.stream()
                .filter(card -> card.genreIds() != null && card.genreIds().stream().anyMatch(generiPreferiti::contains))
                .limit(20)
                .toList();
    }

    // MAPPING PRIVATO
    private FilmCardDto toFilmCardDto(TmdbMovieDto m, Map<Integer, String> genreMap) {
        Integer year = null;
        if (m.releaseDate() != null && m.releaseDate().length() >= 4) {
            year = Integer.parseInt(m.releaseDate().substring(0, 4));
        }

        String posterUrl = m.posterPath() == null ? null : posterBase + m.posterPath();

        List<String> genreNames = m.genreIds() == null || m.genreIds().isEmpty()
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

    private FilmCardDto toFilmCardDto(TmdbMovieDetailsDTO m, Map<Integer, String> genreMap) {
        Integer year = null;
        if (m.releaseDate() != null && m.releaseDate().length() >= 4) {
            year = Integer.parseInt(m.releaseDate().substring(0, 4));
        }

        String posterUrl = m.posterPath() != null ? posterBase + m.posterPath() : null;

        List<Integer> genreIds = m.genres().stream().map(TmdbMovieDetailsDTO.TmdbGenre::id).toList();
        List<String> genreNames = m.genres().stream().map(TmdbMovieDetailsDTO.TmdbGenre::name).toList();

        return new FilmCardDto(
                m.id(),
                m.title(),
                year,
                m.voteAverage(),
                posterUrl,
                genreIds,
                genreNames
        );
    }
}