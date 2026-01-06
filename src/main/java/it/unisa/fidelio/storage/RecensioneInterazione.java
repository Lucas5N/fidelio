package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "recensione_interazione")
public class RecensioneInterazione {

    // Getter e Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "recensione_id", nullable = false)
    private Recensione recensione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInterazione tipo;

    @Column(name = "data_interazione", nullable = false)
    private LocalDateTime dataInterazione = LocalDateTime.now();

    // Enum interno
    public enum TipoInterazione {
        LIKE, DISLIKE
    }
    public RecensioneInterazione() {}

    public RecensioneInterazione(Utente utente, Recensione recensione, TipoInterazione tipo) {
        this.utente = utente;
        this.recensione = recensione;
        this.tipo = tipo;
    }

}