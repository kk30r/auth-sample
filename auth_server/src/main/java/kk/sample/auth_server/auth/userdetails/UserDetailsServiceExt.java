package kk.sample.auth_server.auth.userdetails;

import com.google.common.collect.Lists;
import jakarta.persistence.criteria.Root;
import java.util.List;
import kk.sample.auth_server.auth.entity.Authorities;
import kk.sample.auth_server.auth.entity.UserInfo;
import kk.sample.auth_server.auth.entity.Users;
import kk.sample.auth_server.auth.repository.AuthoritiesRepository;
import kk.sample.auth_server.auth.repository.UserInfoRepository;
import kk.sample.auth_server.auth.repository.UsersRepository;
import kk.sample.auth_server.auth.service.UserAccountLockService;
import kk.sample.auth_server.common.service.jpa.JpaService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.RequiredArgsConstructor
public class UserDetailsServiceExt implements UserDetailsManager,
        UserDetailsPasswordService {

    protected final JpaService jpaService;

    protected final UsersRepository usersRepository;

    protected final UserInfoRepository userInfoRepository;

    protected final AuthoritiesRepository authoritiesRepository;

    protected final UserAccountLockService userAccountLockService;

    /**
     * {@inheritDoc }
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        username = username.toLowerCase();

        final UserDetailsExt userDetails = new UserDetailsExt();

        if (userAccountLockService.isLocked(username) == true) {
            userDetails.setAccountNonLocked(false);
        }

        final Users users = usersRepository.findByUsername(username);
        if (users == null) {
            throw new UsernameNotFoundException(username);
        }
        BeanUtils.copyProperties(users, userDetails);

        final UserInfo userInfo = userInfoRepository.findByUsersId(users.getId());
        if (userInfo != null) {
            BeanUtils.copyProperties(userInfo,
                                     userDetails);
        }

        {
            final List<GrantedAuthority> authoritis = Lists.newArrayList();
            jpaService.iteration(Authorities.class,
                                 (builder, query) -> {
                                     Root<Authorities> root = query.from(
                                             Authorities.class);
                                     query.where(builder.equal(root.get("pk").get("usersId"),
                                                               users.getId()));
                                 },
                                 entity -> {
                                     authoritis.add(new SimpleGrantedAuthority(
                                             entity.getAuthority()));
                                 });
            userDetails.setAuthorities(authoritis);
        }

        return userDetails;
    }

    @Override
    public void createUser(UserDetails user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean userExists(String username) {
        return usersRepository.existsByUsername(username);
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
