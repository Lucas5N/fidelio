package it.unisa.fidelio.dataaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class UtenteGeneriId implements Serializable {
    private static final long serialVersionUID = -3782749226246532886L;
    @Column(name = "utente_id", nullable = false)
    private Integer utenteId;

    @Column(name = "genere", nullable = false, length = 100)
    private String genere;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UtenteGeneriId entity = (UtenteGeneriId) o;
        return Objects.equals(this.genere, entity.genere) &&
                Objects.equals(this.utenteId, entity.utenteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genere, utenteId);
    }

}