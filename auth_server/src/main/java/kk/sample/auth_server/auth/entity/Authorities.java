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
public class Authorities {

    @EmbeddedId
    private PK pk;

    public String getAuthority() {
        return pk.getAuthority();
    }

    @Embeddable
    @lombok.Data
    public static class PK {

        @Column(name = "users_id", insertable = false, updatable = false)
        protected Long usersId;

        @Column(name = "authority")
        protected String authority;
    }

}
