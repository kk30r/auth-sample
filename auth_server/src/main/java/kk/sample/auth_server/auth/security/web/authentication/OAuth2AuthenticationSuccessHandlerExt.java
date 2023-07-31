package kk.sample.auth_server.auth.security.web.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import kk.sample.auth_server.auth.service.OAuth2TokenContextService;
import kk.sample.auth_server.auth.userdetails.UserDetailsExt;
import kk.sample.auth_server.preference.service.ClientPreferenceService;
import kk.sample.auth_server.user_info.form.UserInfoForm;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * copied
 * {@link org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter#sendAuthorizationResponse}
 *
 * @author Kensuke.Ito
 */
@lombok.RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandlerExt implements AuthenticationSuccessHandler {

    private final OAuth2TokenContextService oauth2TokenContextService;

    private final ClientPreferenceService clientPreferenceService;

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    /**
     * ログイン成功時に不足設定の有無を確認する為に拡張
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (checkUserSetup(request, response, authentication) == false) {
            return;
        }
        onAuthenticationSuccessOriginal(request, response, authentication);
    }

    /**
     * ユーザ設定確認
     *
     * @param request
     * @param response
     * @param authentication
     * @return
     * @throws IOException
     */
    protected boolean checkUserSetup(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if ((authentication instanceof OAuth2AuthorizationCodeRequestAuthenticationToken) == false) {
            throw new IllegalStateException("authentication object is unexpected");
        }

        {
            final OAuth2AuthorizationCodeRequestAuthenticationToken actAuthentication
                    = (OAuth2AuthorizationCodeRequestAuthenticationToken) authentication;
            final String clientId = actAuthentication.getClientId();

            final UserDetailsExt userDetails = oauth2TokenContextService.getUserDetails(actAuthentication);
            final UserInfoForm form = new UserInfoForm();
            BeanUtils.copyProperties(userDetails, form);

            if (clientPreferenceService.isSatisfied(clientId, form) == false) {
                this.redirectStrategy.sendRedirect(request, response, "/user_info");
                return false;
            }
        }

        return true;
    }

    /**
     * copied
     * {@link org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter#sendAuthorizationResponse}
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    protected void onAuthenticationSuccessOriginal(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication
                = (OAuth2AuthorizationCodeRequestAuthenticationToken) authentication;
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(authorizationCodeRequestAuthentication.getRedirectUri())
                .queryParam(OAuth2ParameterNames.CODE, authorizationCodeRequestAuthentication.getAuthorizationCode().getTokenValue());
        if (StringUtils.hasText(authorizationCodeRequestAuthentication.getState())) {
            uriBuilder.queryParam(
                    OAuth2ParameterNames.STATE,
                    UriUtils.encode(authorizationCodeRequestAuthentication.getState(), StandardCharsets.UTF_8));
        }
        String redirectUri = uriBuilder.build(true).toUriString();
        // build(true) -> Components are explicitly encoded
        this.redirectStrategy.sendRedirect(request, response, redirectUri);
    }

}
