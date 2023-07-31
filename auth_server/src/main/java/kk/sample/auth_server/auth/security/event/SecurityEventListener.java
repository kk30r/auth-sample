package kk.sample.auth_server.auth.security.event;

import jakarta.servlet.ServletException;
import java.io.IOException;
import kk.sample.auth_server.auth.service.UserAccountLockService;
import kk.sample.auth_server.auth.userdetails.UserDetailsExt;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.RequiredArgsConstructor
@Component
public class SecurityEventListener {

    protected final UserAccountLockService userAccountLockService;

    /**
     * 認証成功イベント<br>
     * <br>
     * OAuth2AuthorizationCodeRequestAuthenticationToken について Event で対応すると
     * OAuth2AuthorizationEndpointFilter の
     * authenticationSuccessHandler.onAuthenticationSuccess にて <br>
     * "response is already committed" となるのでこちらで対応。
     *
     * @param event
     * @throws IOException
     * @throws ServletException
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) throws IOException, ServletException {
        final Authentication authentication = event.getAuthentication();
        /**
         * form ログイン
         */
        if (authentication instanceof UsernamePasswordAuthenticationToken authToken) {
            final UserDetailsExt userDetail = (UserDetailsExt) authToken.getPrincipal();
            final String username = userDetail.getUsername();
            userAccountLockService.clearLock(username);
        }
    }

    /**
     * 認証失敗イベント
     *
     * @param event
     */
    @EventListener
    public void authFailureBadCredentialsEventHandler(AuthenticationFailureBadCredentialsEvent event) {
        if (event.getException().getClass().equals(UsernameNotFoundException.class)) {
            // 存在しないユーザ名でのログイン失敗
            return;
        }

        // 存在するユーザ名でのログイン失敗
        final String username = (String) event.getAuthentication().getPrincipal();
        userAccountLockService.incrementFailureCount(username);
    }

}
