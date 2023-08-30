package kk.sample.auth_server.auth.userdetails;

import java.util.Collection;
import kk.sample.auth_server.auth.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.Getter
@lombok.Setter
public class UserDetailsExt extends Users implements UserDetails {

    protected boolean accountNonExpired = true;

    protected boolean accountNonLocked = true;

    protected boolean credentialsNonExpired = true;

    protected String firstName;

    protected String lastName;

    protected String address;

    protected String address2;

    @lombok.Setter
    protected Collection<? extends GrantedAuthority> authorities;
}
