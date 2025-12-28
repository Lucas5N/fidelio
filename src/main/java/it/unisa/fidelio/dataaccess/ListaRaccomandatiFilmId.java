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
public class ListaRaccomandatiFilmId implements Serializable {
    private static final long serialVersionUID = 1671319998250947140L;
    @Column(name = "lista_id", nullable = false)
    private Integer listaId;

    @Column(name = "film_id", nullable = false)
    private Integer filmId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ListaRaccomandatiFilmId entity = (ListaRaccomandatiFilmId) o;
        return Objects.equals(this.listaId, entity.listaId) &&
                Objects.equals(this.filmId, entity.filmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listaId, filmId);
    }

}