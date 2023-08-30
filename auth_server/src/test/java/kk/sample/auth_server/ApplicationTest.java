package kk.sample.auth_server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import kk.sample.auth_server.preference.resource.ClientPreferenceResource;
import kk.sample.auth_server.user_info.form.UserInfoFormGroups;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author kensuke
 */
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Rollback
public class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     *
     * @return @throws Exception
     */
    public MvcResult init() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/oauth2/authorize")
                .param("response_type", "code")
                .param("scope", "openid")
                .param("client_id", "messaging-client")
                .param("redirect_uri",
                       "http://127.0.0.1:8090/login/oauth2/code/auth-server-sample"))
                .andExpect(status().isFound())
                .andReturn();

        RequestCache requestCache = new HttpSessionRequestCache();
        requestCache.saveRequest(mvcResult.getRequest(),
                                 mvcResult.getResponse());

        return mvcResult;
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testMain() throws Exception {
        init();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testLogin() throws Exception {
        authorize();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void test_tokenRefresh() throws Exception {
        AuthDto authDto = authorize();
        MvcResult mvcResult;

        // トークンリフレッシュ
        {
            mvcResult = mockMvc.perform(post("/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    // "client_id=messaging-client&client_secret=secret&messaging-client=secret&grant_type=authorization_code
                    .param("client_id", "messaging-client")
                    .param("client_secret", "secret")
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", authDto.refreshToken))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        ObjectMapper objMapper = new ObjectMapper();

        // ユーザ情報取得
        // 古いものも新しいものも期限内ならば利用可能
        {
            final String content = mvcResult.getResponse().getContentAsString();
            final JsonNode jsonNode = objMapper.readTree(content);
            String newAccessToken = jsonNode.findValue("access_token").asText();

            mvcResult = mockMvc.perform(post("/userinfo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + authDto.accessToken))
                    // {grant_type=[authorization_code
                    .andExpect(status().isOk())
                    .andReturn();

            mvcResult = mockMvc.perform(post("/userinfo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + newAccessToken))
                    // {grant_type=[authorization_code
                    .andExpect(status().isOk())
                    .andReturn();
        }

    }

    /**
     * USER2
     *
     * @throws Exception
     */
    @Test
    public void testLogin_unsatisfiedField() throws Exception {
        MvcResult mvcResult = init();

        MockHttpSession newSession = copySession(mvcResult);

        // ログイン
        {
            mvcResult = mockMvc.perform(post("/login")
                    .session(newSession)
                    .param("username", "USER2")
                    .param("password", "password")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isFound())
                    .andReturn();
            assertEquals("http://localhost/oauth2/authorize?continue",
                         mvcResult.getResponse().getRedirectedUrl());
        }

        // 情報が足りず、入力画面へ
        {
            newSession = copySession(mvcResult);
            mvcResult = mockMvc.perform(get("/oauth2/authorize")
                    .session(newSession)
                    .param("response_type", "code")
                    .param("scope", "openid")
                    .param("client_id", "messaging-client")
                    .param("redirect_uri",
                           "http://127.0.0.1:8090/login/oauth2/code/messaging-client-oidc"))
                    .andExpect(status().isFound())
                    .andReturn();
            assertEquals("/user_info",
                         mvcResult.getResponse().getRedirectedUrl());
        }

        // バリデーションエラーにより入力画面へ
        {
            newSession = copySession(mvcResult);
            mvcResult = mockMvc.perform(post("/user_info")
                    .session(newSession)
                    .param("firstName", "USER2_F")
                    .param("lastName", "USER2_L")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andReturn();
            final List<String> errors
                    = (List<String>) mvcResult.getModelAndView().getModel().get("validationError");
            assertEquals(1, errors.size());
            assertEquals("must not be blank", errors.get(0));
        }

        {
            newSession = copySession(mvcResult);
            mvcResult = mockMvc.perform(post("/user_info")
                    .session(newSession)
                    .param("firstName", "USER2_F")
                    .param("lastName", "USER2_L")
                    .param("address", "ADDRESS2")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isFound())
                    .andReturn();
            assertEquals("http://localhost/oauth2/authorize?continue",
                         mvcResult.getResponse().getRedirectedUrl());
        }
    }

    @Autowired
    protected ClientPreferenceResource clientPreferenceResource;

    /**
     * USER2
     *
     * @throws Exception
     */
    @Test
    public void testLogin_unsatisfiedField_onSSO() throws Exception {

        AuthDto authDto = authorize();
        MvcResult mvcResult;

        MockHttpSession newSession = authDto.session;

        // SSO
        {
            mvcResult = mockMvc.perform(get("/oauth2/authorize")
                    .session(newSession)
                    .param("response_type", "code")
                    .param("scope", "openid")
                    .param("client_id", "messaging-client")
                    .param("redirect_uri",
                           "http://127.0.0.1:8090/login/oauth2/code/auth-server-sample")
                    .param("state", "VxUuVQDfNkJAY-VkwRUv5L_SHV33jkNoAq7jRY9eCng%3D")
                    .param("nonce", "NTOT5oHJ40qaWHq-NfrQQyEH1FWCs9hdXG0rjqs5mpM"))
                    .andExpect(status().isFound())
                    .andReturn();

            String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
            assertTrue(redirectedUrl.startsWith("http://127.0.0.1:8090/login/oauth2/code/auth-server-sample?code="));
        }

        newSession = copySession(mvcResult);

        // SSO
        {
            mvcResult = mockMvc.perform(get("/oauth2/authorize")
                    .session(newSession)
                    .param("response_type", "code")
                    .param("scope", "openid")
                    .param("client_id", "messaging-client")
                    .param("redirect_uri",
                           "http://127.0.0.1:8090/login/oauth2/code/auth-server-sample")
                    .param("state", "VxUuVQDfNkJAY-VkwRUv5L_SHV33jkNoAq7jRY9eCng%3D")
                    .param("nonce", "NTOT5oHJ40qaWHq-NfrQQyEH1FWCs9hdXG0rjqs5mpM"))
                    .andExpect(status().isFound())
                    .andReturn();

            String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
            assertTrue(redirectedUrl.startsWith("http://127.0.0.1:8090/login/oauth2/code/auth-server-sample?code="));
        }

        try {
            clientPreferenceResource.getPreference("messaging-client")
                    .put("address2", UserInfoFormGroups.Address2.class);

            // SSO with unsatisfiedField
            {
                mvcResult = mockMvc.perform(get("/oauth2/authorize")
                        .session(newSession)
                        .param("response_type", "code")
                        .param("scope", "openid")
                        .param("client_id", "messaging-client")
                        .param("redirect_uri",
                               "http://127.0.0.1:8090/login/oauth2/code/auth-server-sample")
                        .param("state", "VxUuVQDfNkJAY-VkwRUv5L_SHV33jkNoAq7jRY9eCng%3D")
                        .param("nonce", "NTOT5oHJ40qaWHq-NfrQQyEH1FWCs9hdXG0rjqs5mpM"))
                        .andExpect(status().isFound())
                        .andReturn();

                String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
                assertEquals("/user_info", redirectedUrl);
            }
        } finally {
            clientPreferenceResource.getPreference("messaging-client")
                    .remove("address2");
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testLogin_failAndLock() throws Exception {
        MvcResult mvcResult = init();

        MockHttpSession newSession;

        for (int i = 0; i < 5; i++) {
            newSession = copySession(mvcResult);
            mvcResult = mockMvc.perform(post("/login")
                    .session(newSession)
                    .param("username", "USER1")
                    .param("password", "hogehoge")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isFound())
                    .andReturn();
            assertEquals("/login?error",
                         mvcResult.getResponse().getRedirectedUrl());
            assertEquals(BadCredentialsException.class,
                         mvcResult.getRequest().getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION").getClass());
        }

        // ロックされているはず
        // ログイン
        {
            newSession = copySession(mvcResult);
            mvcResult = mockMvc.perform(post("/login")
                    .session(newSession)
                    .param("username", "USER1")
                    .param("password", "password")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isFound())
                    .andReturn();
            assertEquals("/login?error",
                         mvcResult.getResponse().getRedirectedUrl());
            assertEquals(LockedException.class,
                         mvcResult.getRequest().getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION").getClass());
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void test_logout() throws Exception {
        AuthDto authDto = authorize();
        AuthDto authDto2 = authorize();

        // logout
        {
            MvcResult mvcResult = mockMvc.perform(get("/logout")
                    .session(authDto.session))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        // ユーザ情報取得
        // logout されてても、access_token は活きている
        {
            MvcResult mvcResult = mockMvc.perform(post("/userinfo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + authDto.accessToken))
                    .andExpect(status().isOk())
                    .andReturn();

        }
        // logout されてても、access_token は活きている
        {
            MvcResult mvcResult = mockMvc.perform(post("/userinfo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + authDto2.accessToken))
                    .andExpect(status().isOk())
                    .andReturn();
        }

    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void test_revoke() throws Exception {
        AuthDto authDto = authorize();
        AuthDto authDto2 = authorize();

        // トークン revoke
        {
            MvcResult mvcResult = mockMvc.perform(post("/oauth2/revoke")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    // "client_id=messaging-client&client_secret=secret&messaging-client=secret&grant_type=authorization_code
                    .param("client_id", "messaging-client")
                    .param("client_secret", "secret")
                    .param("token", authDto.accessToken)
                    .param("token_type", "access_token"))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        // ユーザ情報取得
        // revoke されているのでアクセス不能
        {
            MvcResult mvcResult = mockMvc.perform(post("/userinfo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + authDto.accessToken))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

        }
        // revoke されていないトークンもアクセス不能
        {
            MvcResult mvcResult = mockMvc.perform(post("/userinfo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + authDto2.accessToken))
                    .andExpect(status().isUnauthorized())
                    .andReturn();
        }

    }

    /**
     * 共通認証処理
     *
     * @return
     * @throws Exception
     */
    protected AuthDto authorize() throws Exception {
        MvcResult mvcResult = init();

        MockHttpSession newSession = copySession(mvcResult);

        mvcResult = mockMvc.perform(post("/login")
                .session(newSession)
                .param("username", "USER1")
                .param("password", "password")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isFound())
                .andReturn();

        newSession = copySession(mvcResult);
        mvcResult = mockMvc.perform(get("/oauth2/authorize")
                .session(newSession)
                .param("response_type", "code")
                .param("scope", "openid")
                .param("client_id", "messaging-client")
                .param("redirect_uri",
                       "http://127.0.0.1:8090/login/oauth2/code/messaging-client-oidc"))
                .andExpect(status().isFound())
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertTrue(redirectedUrl.startsWith("http://127.0.0.1:8090/login/oauth2/code/messaging-client-oidc?code="));

        newSession = copySession(mvcResult);

        // トークン取得
        {
            String code = UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams().get("code").get(0);

            mvcResult = mockMvc.perform(post("/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    // "client_id=messaging-client&client_secret=secret&messaging-client=secret&grant_type=authorization_code
                    .param("client_id", "messaging-client")
                    .param("client_secret", "secret")
                    .param("grant_type", "authorization_code")
                    .param("redirect_uri", "http://127.0.0.1:8090/login/oauth2/code/messaging-client-oidc")
                    .param("code", code))
                    // {grant_type=[authorization_code
                    .andExpect(status().isOk())
                    .andReturn();
        }

        ObjectMapper objMapper = new ObjectMapper();
        String idToken;
        String accessToken;
        String refreshToken;
        {
            final String content = mvcResult.getResponse().getContentAsString();
            final JsonNode jsonNode = objMapper.readTree(content);
            accessToken = jsonNode.findValue("access_token").asText();
            idToken = jsonNode.findValue("id_token").asText();
            refreshToken = jsonNode.findValue("refresh_token").asText();

            // ユーザ情報取得
            {
                mvcResult = mockMvc.perform(post("/userinfo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Authorization", "Bearer " + accessToken))
                        // {grant_type=[authorization_code
                        .andExpect(status().isOk())
                        .andReturn();

            }
        }

        return new AuthDto(mvcResult, newSession, idToken, accessToken, refreshToken);
    }

    /**
     *
     * @param mvcResult
     * @return
     */
    protected MockHttpSession copySession(MvcResult mvcResult) {
        HttpSession sessionOrg = mvcResult.getRequest().getSession();
        final MockHttpSession newSession = new MockHttpSession();
        Collections.list(sessionOrg.getAttributeNames()).stream()
                .forEach(name -> {
                    newSession.setAttribute(name, sessionOrg.getAttribute(name));
                });
        return newSession;
    }

    @Disabled
    @Test
    public void passEndcode() {
        PasswordEncoder passEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String pass = passEncoder.encode("password");
        System.out.println(pass);
    }

    @lombok.AllArgsConstructor
    protected static class AuthDto {

        public MvcResult mvcResult;

        public MockHttpSession session;

        public String idToken;

        public String accessToken;

        public String refreshToken;
    }

}
