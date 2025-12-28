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
public class CommunityMembriId implements Serializable {
    private static final long serialVersionUID = 6814495274825286397L;
    @Column(name = "community_id", nullable = false)
    private Integer communityId;

    @Column(name = "utente_id", nullable = false)
    private Integer utenteId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CommunityMembriId entity = (CommunityMembriId) o;
        return Objects.equals(this.communityId, entity.communityId) &&
                Objects.equals(this.utenteId, entity.utenteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(communityId, utenteId);
    }

}