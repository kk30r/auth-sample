package kk.sample.auth_server.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author Kensuke.Ito
 */
@Entity
@Table(name = "USERS")
@lombok.Data
public class Users {

    @Id
    protected Long id;

    @Column(name = "username", nullable = false)
    protected String username;

    @Column(name = "password", nullable = false)
    protected String password;

    @Column(name = "enabled", nullable = false)
    protected boolean enabled;

}
