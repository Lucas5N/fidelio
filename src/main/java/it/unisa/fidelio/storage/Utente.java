package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "utente")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "dtype", nullable = false, length = 31)
    private String dtype; // es. "Cinefilo", "Giornalista", ecc.

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cognome", nullable = false)
    private String cognome;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // sarà salvata HASHATA con BCrypt

    @Lob
    @Column(name = "bio")
    private String bio;

    @Lob
    @Column(name = "immagine_profilo", columnDefinition = "LONGBLOB")
    private byte[] immagineProfilo;

    @Column(name = "data_registrazione")
    private Instant dataRegistrazione;

    @Column(name = "data_ultimo_accesso")
    private Instant dataUltimoAccesso;

    @ColumnDefault("0")
    @Column(name = "num_film_visti")
    private Integer numFilmVisti;

    @Column(name = "testata_giornalistica")
    private String testataGiornalistica;

    @Column(name = "casa_produzione")
    private String casaProduzione;

    @Column(name = "credit_reference")
    private String creditReference;

    @Column(name = "livello_accesso", length = 50)
    private String livelloAccesso;

    // Relazioni (rimaste invariate)
    @OneToMany(mappedBy = "autore")
    private Set<Commento> commenti = new LinkedHashSet<>();

    @OneToMany(mappedBy = "creatore")
    private Set<Community> communitiesCreate = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "community_membri",
            joinColumns = @JoinColumn(name = "utente_id"),
            inverseJoinColumns = @JoinColumn(name = "community_id")
    )
    private Set<Community> communitiesIscritte = new LinkedHashSet<>();

    @OneToMany(mappedBy = "proprietario")
    private Set<ListaPrivata> listePrivate = new LinkedHashSet<>();

    @OneToOne(mappedBy = "proprietario")
    private ListaRaccomandati listaRaccomandati;

    @OneToMany(mappedBy = "mittente")
    private Set<MessaggioPrivato> messaggiInviati = new LinkedHashSet<>();

    @OneToMany(mappedBy = "destinatario")
    private Set<MessaggioPrivato> messaggiRicevuti = new LinkedHashSet<>();

    @OneToMany(mappedBy = "autore")
    private Set<Recensione> recensioni = new LinkedHashSet<>();

    @OneToMany(mappedBy = "autore")
    private Set<Segnalazione> segnalazioniInviate = new LinkedHashSet<>();

    @OneToMany(mappedBy = "gestore")
    private Set<Segnalazione> segnalazioniGestite = new LinkedHashSet<>();

    @OneToMany(mappedBy = "autore")
    private Set<Thread> threads = new LinkedHashSet<>();

    @OneToMany(mappedBy = "utente")
    private Set<UtenteAttori> utenteAttori = new LinkedHashSet<>();

    @OneToMany(mappedBy = "utente")
    private Set<UtenteGeneri> utenteGeneri = new LinkedHashSet<>();


}