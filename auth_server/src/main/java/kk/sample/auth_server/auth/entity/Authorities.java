package kk.sample.auth_server.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 *
 * @author Kensuke.Ito
 */
@Entity
@Table(name = "AUTHORITIES")
@lombok.Data
@lombok.NoArgsConstructor
public class Authorities {

    public Authorities(Long usersId, String authority) {
        this.pk = new PK(usersId, authority);
    }

    @EmbeddedId
    private PK pk;

    public String getAuthority() {
        return pk.getAuthority();
    }

    @Embeddable
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PK {

        @Column(name = "users_id", insertable = false, updatable = false)
        protected Long usersId;

        @Column(name = "authority")
        protected String authority;
    }

}
