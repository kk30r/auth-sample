package kk.sample.auth_client.mock.service;

import java.util.List;
import kk.sample.auth_client.mock.repository.MyUserRepository;
import kk.sample.auth_client.mock.user.MyOidcUser;
import kk.sample.auth_client.mock.user.MyUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 *
 * @author Kensuke.Ito
 */
@RequiredArgsConstructor
@Service
public class WebOidcUserService extends OidcUserService {

    private final MyUserRepository myUserRepository;

    /**
     * デフォルトの OidcUserService との使い分けが必要
     *
     * @param userRequest
     * @return
     * @throws OAuth2AuthenticationException
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        final OidcUser oidcUser = super.loadUser(userRequest);
        String username = oidcUser.getPreferredUsername();
        if (ObjectUtils.isEmpty(username)) {
            username = oidcUser.getName();
        }

        // DBからユーザー情報・ロールを取得する
        final MyUser myUser = myUserRepository.findById(username);
        // ロールの付与
        final List<OAuth2UserAuthority> authList = myUser.getAuthorities()
                .stream()
                .map(role -> new OAuth2UserAuthority(role.getAuthority(),
                oidcUser.getAttributes()))
                .toList();

        return new MyOidcUser(authList,
                userRequest.getIdToken(),
                oidcUser.getUserInfo(),
                myUser);
    }
}
