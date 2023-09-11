package kk.sample.auth_server.auth.config;

import java.util.function.Function;
import kk.sample.auth_server.auth.repository.AuthoritiesRepository;
import kk.sample.auth_server.auth.repository.UserInfoRepository;
import kk.sample.auth_server.auth.repository.UsersRepository;
import kk.sample.auth_server.auth.security.web.authentication.OAuth2AuthenticationSuccessHandlerExt;
import kk.sample.auth_server.auth.service.OAuth2TokenContextService;
import kk.sample.auth_server.auth.service.UserAccountLockService;
import kk.sample.auth_server.auth.userdetails.UserDetailsServiceExt;
import kk.sample.auth_server.common.service.jpa.JpaService;
import kk.sample.auth_server.preference.service.ClientPreferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 *
 * @author Kensuke.Ito
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfigExt extends SecurityConfig {

    protected final OAuth2TokenContextService oauth2TokenContextService;

    protected final ClientPreferenceService clientPreferenceService;

    /**
     *
     * @param securityConfigProperties
     * @param oauth2TokenContextService
     * @param clientPreferenceService
     */
    public SecurityConfigExt(SecurityConfigProperties securityConfigProperties,
                             OAuth2TokenContextService oauth2TokenContextService,
                             ClientPreferenceService clientPreferenceService) {
        super(securityConfigProperties);
        this.oauth2TokenContextService = oauth2TokenContextService;
        this.clientPreferenceService = clientPreferenceService;
    }

    /**
     * authorizationResponseHandler を拡張する<br>
     * {@link org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration#applyDefaultSecurity}
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void applySecurity(HttpSecurity http)
            throws Exception {
        // OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer
                = new OAuth2AuthorizationServerConfigurer();

        // 認証成功後のチェック
        authorizationServerConfigurer.authorizationEndpoint(configurer -> {
            configurer.authorizationResponseHandler(oauth2AuthenticationSuccessHandlerExt());
        });

        RequestMatcher endpointsMatcher = authorizationServerConfigurer
                .getEndpointsMatcher();

        // ドメイン固有の方法でクレームをマッピングするユーザー情報マッパー。
        setupUserInfoEndpoint(authorizationServerConfigurer);

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/unauth/**").permitAll();
                    authorize
                            .anyRequest().authenticated();
                })
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .apply(authorizationServerConfigurer);
    }

    /**
     * ログイン成功時に不足設定の有無を確認する為に拡張<br>
     * <br>
     * Event で対応すると OAuth2AuthorizationEndpointFilter の
     * authenticationSuccessHandler.onAuthenticationSuccess にて <br>
     * "response is already committed" となるのでこちらで対応。
     *
     * @return
     */
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandlerExt() {
        return new OAuth2AuthenticationSuccessHandlerExt(oauth2TokenContextService, clientPreferenceService);
    }

    /**
     * ドメイン固有の方法でクレームをマッピングするユーザー情報マッパーを設定
     *
     * @param authorizationServerConfigurer
     */
    protected void setupUserInfoEndpoint(OAuth2AuthorizationServerConfigurer authorizationServerConfigurer) {
        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
            return oauth2TokenContextService.toOidcUserInfo(context);
        };
        authorizationServerConfigurer.oidc((oidc) -> {
            oidc.userInfoEndpoint((userInfo) -> userInfo.userInfoMapper(userInfoMapper));
        });
    }

    /**
     * IDトークンをカスタマイズ<br>
     *
     * @see
     * https://docs.spring.io/spring-authorization-server/docs/current/reference/html/guides/how-to-userinfo.html
     *
     * @param oauth2TokenContextUtils
     * @return
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(OAuth2TokenContextService oauth2TokenContextUtils) {
        return (context) -> {
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {

                UserDetails userDetails = oauth2TokenContextUtils.getUserDetails(context);
                OidcUserInfo userInfo = oauth2TokenContextUtils.toOidcUserInfo(userDetails);
                context.getClaims().claims(claims
                        -> claims.putAll(userInfo.getClaims()));
            }
        };
    }

    /**
     * JWT 変換
     *
     * @return
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // scope の代わりに authorities を返す
        // scope も返したい場合は拡張が必要
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // OAuth2TokenContextUtils でセットしているクレーム名と合わせる
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * ユーザ情報の取得
     *
     * @param jpaService
     * @param usersRepository
     * @param userInfoRepository
     * @param authoritiesRepository
     * @param userAccountLockService
     * @return
     */
    @Bean
    public UserDetailsServiceExt userDetailsService(final JpaService jpaService,
                                                    final UsersRepository usersRepository,
                                                    final UserInfoRepository userInfoRepository,
                                                    final AuthoritiesRepository authoritiesRepository,
                                                    final UserAccountLockService userAccountLockService) {
        return new UserDetailsServiceExt(jpaService,
                                         usersRepository,
                                         userInfoRepository,
                                         authoritiesRepository,
                                         userAccountLockService);
    }

    /**
     *
     * @param jdbcTemplate
     * @return
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

}
