package kk.sample.auth_server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
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
public class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    /**
     *
     * @return @throws Exception
     */
    public MvcResult init() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/oauth2/authorize")
                .param("response_type", "code")
                //                .param("scope", "openid")
                .param("scope", "openid+profile")
                .param("client_id", "messaging-client")
                .param("redirect_uri",
                       "http://127.0.0.1:8090/login/oauth2/code/messaging-client-oidc"))
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
        String accessToken;
        String refreshToken;
        {
            final String content = mvcResult.getResponse().getContentAsString();
            final JsonNode jsonNode = objMapper.readTree(content);
            accessToken = jsonNode.findValue("access_token").asText();
            final String idToken = jsonNode.findValue("id_token").asText();
            refreshToken = jsonNode.findValue("refresh_token").asText();

//            System.out.println(accessToken);
//            System.out.println(idToken);
//            System.out.println(refreshToken);
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

        // トークンリフレッシュ
        {
            {
                mvcResult = mockMvc.perform(post("/oauth2/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        // "client_id=messaging-client&client_secret=secret&messaging-client=secret&grant_type=authorization_code
                        .param("client_id", "messaging-client")
                        .param("client_secret", "secret")
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken))
                        .andExpect(status().isOk())
                        .andReturn();

//                System.out.println(mvcResult.getResponse().getContentAsString());
            }

            // ユーザ情報取得
            // 古いものも新しいものも期限内ならば利用可能
            {
                final String content = mvcResult.getResponse().getContentAsString();
                final JsonNode jsonNode = objMapper.readTree(content);
                String newAccessToken = jsonNode.findValue("access_token").asText();

                mvcResult = mockMvc.perform(post("/userinfo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Authorization", "Bearer " + accessToken))
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

}
