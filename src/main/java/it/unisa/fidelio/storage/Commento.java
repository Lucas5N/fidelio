package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "commento")
public class Commento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Lob
    @Column(nullable = false)
    private String testo;

    @Column(name = "data_creazione")
    private Instant dataCreazione = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autore_id", nullable = false)
    private Utente autore;

    // --- LOGICA IBRIDA ---

    // Riferimento forte per recensioni su Aiven
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recensione_id") // Può essere NULL
    private Recensione recensione;

    // Riferimento debole per recensioni TMDB
    @Column(name = "tmdb_review_id") // E' una stringa, es. "548b13..."
    private String tmdbReviewId;

    // Metodo helper per capire se è un commento a TMDB
    public boolean isExternalComment() {
        return tmdbReviewId != null;
    }
}