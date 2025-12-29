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

@Getter
@Setter
@Entity
@Table(name = "lista_privata")
public class ListaPrivata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Lob
    @Column(name = "descrizione")
    private String descrizione;

    @ColumnDefault("1")
    @Column(name = "privata")
    private Boolean privata;

    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Utente proprietario;

    // LATO ATTIVO: Definisce la tabella di giunzione
    @ManyToMany
    @JoinTable(
            name = "lista_privata_film",
            joinColumns = @JoinColumn(name = "lista_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    private Set<Film> film = new LinkedHashSet<>();
}