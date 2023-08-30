package kk.sample.auth_server.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 *
 * @author Kensuke.Ito
 */
public class SecurityConfig {

    @Value("${server.servlet.session.timeout.cookie.name:JSESSIONID}")
    protected String sessionCookieName;

    /**
     * メインの IdP に関する設定<br>
     * OpenID Connect を有効化。
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        applySecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // Enable OpenID Connect 1.0
                .oidc(Customizer.withDefaults());

        http.formLogin(cutomizer -> {
            cutomizer.successHandler(savedRequestAwareAuthenticationSuccessHandler());
        });
        // Redirect to the login page when not authenticated from the
        // authorization endpoint
        http.exceptionHandling((exceptions) -> {
            exceptions.authenticationEntryPoint(
                    new LoginUrlAuthenticationEntryPoint("/login"));
        });
        http.logout(configurer -> {
            configurer.clearAuthentication(true)
                    .deleteCookies(sessionCookieName);
        });
        // Accept access tokens for User Info and/or Client Registration
        http.oauth2ResourceServer(configurer -> {
            configurer.jwt(jwtCustomizer -> {
            });
        });

        return http.build();
    }

    /**
     * セキュリティ設定
     *
     * @param http
     * @throws Exception
     */
    protected void applySecurity(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    }

    /**
     *
     * @return
     */
    @Bean("formAuthenticationSuccessHandler")
    public AuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler();
    }

    /**
     * 一般的な Spring Security の設定<br>
     * 全てのパスに対して認証を要求<br>
     * authorizationServerFilterChain で既に口が開けられている。
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers("/unauth/**").anonymous();
                    authorize
                            .anyRequest().authenticated();
                })
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                // SSO があるので認証成功後のチェックは AuthenticationSuccessHandlerExt にて対応
                .formLogin(Customizer.withDefaults());
        http.logout(configurer -> {
            configurer.clearAuthentication(true)
                    .deleteCookies(sessionCookieName);
        });

        return http.build();
    }

    /**
     * JWS に使用する 鍵を設定
     *
     * @return
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * RsaKey の取得
     *
     * @return
     */
    protected KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    /**
     * JwtDecoder
     *
     * @param jwkSource
     * @return
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * AuthorizationServerSettings
     *
     * @return
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

}
