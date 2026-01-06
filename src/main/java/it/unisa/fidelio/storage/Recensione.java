package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

// src/main/java/it/unisa/fidelio/storage/Recensione.java

@Getter
@Setter
@Entity
@Table(name = "recensione")
public class Recensione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "testo", nullable = false)
    private String testo;

    @Column(name = "voto", nullable = false)
    private Double voto;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @ColumnDefault("0")
    @Column(name = "spoiler_alert")
    private Boolean spoilerAlert;

    @ColumnDefault("0")
    @Column(name = "num_like")
    private Integer numLike;

    @ColumnDefault("0")
    @Column(name = "num_dislike")
    private Integer numDislike;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "autore_id", nullable = false)
    private Utente autore;

    @Column(name = "film_tmdb_id", nullable = false)
    private Long filmTmdbId;

    @OneToMany(mappedBy = "recensione")
    private Set<Commento> commento = new LinkedHashSet<>();

    @OneToMany(mappedBy = "recensione")
    private Set<Segnalazione> segnalazione = new LinkedHashSet<>();

}