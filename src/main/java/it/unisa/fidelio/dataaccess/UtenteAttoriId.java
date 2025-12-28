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
public class UtenteAttoriId implements Serializable {
    private static final long serialVersionUID = 175122069129113345L;
    @Column(name = "utente_id", nullable = false)
    private Integer utenteId;

    @Column(name = "attore", nullable = false, length = 100)
    private String attore;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UtenteAttoriId entity = (UtenteAttoriId) o;
        return Objects.equals(this.utenteId, entity.utenteId) &&
                Objects.equals(this.attore, entity.attore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utenteId, attore);
    }

}