package kk.sample.auth_server.auth.service;

import java.util.stream.Collectors;
import kk.sample.auth_server.auth.userdetails.UserDetailsExt;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kensuke.Ito
 */
@Service
@lombok.RequiredArgsConstructor
public class OAuth2TokenContextService {

    /**
     * ユーザ詳細を取得
     *
     * @param context
     * @return
     */
    public UserDetailsExt getUserDetails(OAuth2TokenContext context) {
        return (UserDetailsExt) context.getPrincipal().getPrincipal();
    }

    /**
     * ユーザ詳細を取得
     *
     * @param context
     * @return
     */
    public UserDetailsExt getUserDetails(OAuth2AuthorizationCodeRequestAuthenticationToken context) {
        final UsernamePasswordAuthenticationToken token
                = (UsernamePasswordAuthenticationToken) context.getPrincipal();
        return (UserDetailsExt) token.getPrincipal();
    }

    /**
     * OidcUserInfo への変換
     *
     * @param context
     * @return
     */
    public OidcUserInfo toOidcUserInfo(OAuth2TokenContext context) {
        return toOidcUserInfo(getUserDetails(context));
    }

    /**
     * OidcUserInfo への変換
     *
     * @param context
     * @return
     */
    public OidcUserInfo toOidcUserInfo(OAuth2AuthenticationContext context) {
        OAuth2Authorization oauth2Auth = context.get(OAuth2Authorization.class);
        OidcIdToken oidcIdToken = oauth2Auth.getToken(OidcIdToken.class).getToken();

        return OidcUserInfo.builder()
                .claims(claims -> claims.putAll(oidcIdToken.getClaims()))
                .build();
    }

    /**
     * OidcUserInfo への変換
     *
     * @param userDetails
     * @return
     */
    public OidcUserInfo toOidcUserInfo(UserDetails userDetails) {
        return oidcUserInfoBuilder(userDetails)
                .build();
    }

    protected OidcUserInfo.Builder oidcUserInfoBuilder(UserDetails userDetails) {
        OidcUserInfo.Builder oidcUserInfoBuilder = OidcUserInfo.builder()
                .subject(userDetails.getUsername());
        if (userDetails instanceof UserDetailsExt userDetailsExt) {
            oidcUserInfoBuilder.familyName(userDetailsExt.getLastName())
                    .givenName(userDetailsExt.getFirstName())
                    .address(userDetailsExt.getAddress());

            oidcUserInfoBuilder.claim("authorities",
                                      userDetails.getAuthorities().stream()
                                              .map(auth -> auth.getAuthority())
                                              .collect(Collectors.toList()));

        }
        return oidcUserInfoBuilder;
    }
}
