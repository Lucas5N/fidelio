package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "film")
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "titolo", nullable = false)
    private String titolo;

    @Column(name = "regista")
    private String regista;

    @Column(name = "anno")
    private Integer anno;

    @Column(name = "genere", length = 100)
    private String genere;

    @Lob
    @Column(name = "trama")
    private String trama;

    @ColumnDefault("0")
    @Column(name = "voto_medio")
    private Double votoMedio;

    @Column(name = "locandina")
    private String locandina;

    // LATO PASSIVO: riferisce al campo 'film' in ListaPrivata
    @ManyToMany(mappedBy = "film")
    private Set<ListaPrivata> listePrivate = new LinkedHashSet<>();

    // LATO PASSIVO: riferisce al campo 'film' in ListaRaccomandati
    @ManyToMany(mappedBy = "film")
    private Set<ListaRaccomandati> listeRaccomandati = new LinkedHashSet<>();

    @OneToMany(mappedBy = "film")
    private Set<Recensione> recensioni = new LinkedHashSet<>();
}