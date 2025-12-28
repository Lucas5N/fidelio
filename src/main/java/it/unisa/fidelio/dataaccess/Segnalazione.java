package it.unisa.fidelio.dataaccess;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "segnalazione")
public class Segnalazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Convert(disableConversion = true)
    @Column(name = "data_segnalazione")
    private Instant dataSegnalazione;

    @ColumnDefault("'APERTA'")
    @Column(name = "stato", length = 50)
    private String stato;

    @Column(name = "esito")
    private String esito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "autore_id", nullable = false)
    private Utente autore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recensione_id", nullable = false)
    private Recensione recensione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestore_id")
    private Utente gestore;

}