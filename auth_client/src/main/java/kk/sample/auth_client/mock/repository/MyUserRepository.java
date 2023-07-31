/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kk.sample.auth_client.mock.repository;

import java.util.Collection;
import java.util.List;
import kk.sample.auth_client.mock.user.MyUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

/**
 *
 * @author Kensuke.Ito
 */
@Repository
public class MyUserRepository {

    public MyUser findById(String username) {
        if (ObjectUtils.isEmpty(this)) {
            throw new UsernameNotFoundException("username is empty");
        }

        Collection<GrantedAuthority> authorities
                = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new MyUser(username,
                "",
                true,
                true,
                true,
                true,
                authorities);
    }
}
