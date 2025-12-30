package it.unisa.fidelio.presentation;
import java.time.LocalDate;
public class GestioneFilmRequestDTO {

    private Long tmdbId;

    private String stato;  // "VISTO", "DA_VEDERE", "PREFERITO"

    private LocalDate dataVisione;  // obbligatoria solo se stato = "VISTO", altrimenti può essere null

    // Costruttore vuoto (necessario per Jackson/Spring)
    public GestioneFilmRequestDTO() {}

    // Costruttore completo (comodo per test)
    public GestioneFilmRequestDTO(Long tmdbId, String stato, LocalDate dataVisione) {
        this.tmdbId = tmdbId;
        this.stato = stato;
        this.dataVisione = dataVisione;
    }

    // Getter e Setter
    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public LocalDate getDataVisione() {
        return dataVisione;
    }

    public void setDataVisione(LocalDate dataVisione) {
        this.dataVisione = dataVisione;
    }
}