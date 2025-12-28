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
@Table(name = "thread")
public class Thread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "titolo", nullable = false)
    private String titolo;

    @Lob
    @Column(name = "contenuto", nullable = false)
    private String contenuto;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Convert(disableConversion = true)
    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @ColumnDefault("0")
    @Column(name = "num_risposte")
    private Integer numRisposte;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autore_id", nullable = false)
    private Utente autore;

}