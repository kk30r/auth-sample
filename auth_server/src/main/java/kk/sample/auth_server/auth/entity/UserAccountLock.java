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
@Table(name = "USER_ACCOUNT_LOCK")
@lombok.Data
public class UserAccountLock {

    @Id
    @Column(name = "username")
    protected String username;

    @Column(name = "failure_count", nullable = false)
    protected Long failureCount;

}
