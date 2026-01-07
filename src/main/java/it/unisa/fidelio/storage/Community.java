package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "community")
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Lob
    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @ColumnDefault("0")
    @Column(name = "num_membri")
    private Integer numMembri;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creatore_id", nullable = false)
    private Utente creatore;

    // LATO PASSIVO: riferisce a 'communitiesIscritte' in Utente
    @ManyToMany(mappedBy = "communitiesIscritte")
    private Set<Utente> utentiIscritti = new LinkedHashSet<>();

    @OneToMany(mappedBy = "community")
    private Set<Thread> threads = new LinkedHashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy 
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() 
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy 
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() 
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Community community = (Community) o;
        return getId() != null && Objects.equals(getId(), community.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy 
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() 
                : getClass().hashCode();
    }
}