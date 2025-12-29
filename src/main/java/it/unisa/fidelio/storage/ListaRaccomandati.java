package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "lista_raccomandati")
public class ListaRaccomandati {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @Column(name = "data_ultimo_aggiornamento")
    private Instant dataUltimoAggiornamento;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Utente proprietario;

    // LATO ATTIVO
    @ManyToMany
    @JoinTable(
            name = "lista_raccomandati_film",
            joinColumns = @JoinColumn(name = "lista_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    private Set<Film> film = new LinkedHashSet<>();
}