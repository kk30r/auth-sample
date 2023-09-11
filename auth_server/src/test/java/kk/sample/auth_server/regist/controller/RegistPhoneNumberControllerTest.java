package kk.sample.auth_server.regist.controller;

import kk.sample.auth_server.regist.service.RegistPhoneNumberService;
import kk.sample.auth_server.regist.service.RegistPhoneNumberService.ConfirmContext;
import static kk.sample.auth_server.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 電話番号登録テスト<br>
 * TODO : validation test
 *
 * @author kensuke
 */
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Rollback
public class RegistPhoneNumberControllerTest {

    protected static final ThreadLocal<MvcResult> MVC_RESULT = new ThreadLocal();

    protected static final ThreadLocal<ConfirmContext> CONFIRM_NUMBER = new ThreadLocal();

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    public static class MockConfig {

        /**
         * Mock for keep number
         *
         * @return
         */
        @Bean
        @Primary
        public RegistPhoneNumberService registPhoneNumberServiceMock() {
            return new RegistPhoneNumberService() {
                @Override
                protected ConfirmContext generateConfirmCode(String phoneNumber) {
                    ConfirmContext ctx = super.generateConfirmCode(phoneNumber);
                    CONFIRM_NUMBER.set(ctx);
                    return ctx;
                }

            };
        }
    }

    @BeforeEach
    public void setup() {
        CONFIRM_NUMBER.remove();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_index() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/unauth/regist/phone_number"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("/unauth/regist/phone_number",
                     mvcResult.getModelAndView().getViewName());

        MVC_RESULT.set(mvcResult);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_post() throws Exception {
        testPhoneNumber_index();
        MvcResult mvcResult = MVC_RESULT.get();

        MockHttpSession newSession = copySession(mvcResult);
        mvcResult = mockMvc.perform(post("/unauth/regist/phone_number")
                .session(newSession)
                .param("phoneNumber", "09058020034")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("/unauth/regist/phone_number_confirm",
                     mvcResult.getModelAndView().getViewName());

        MVC_RESULT.set(mvcResult);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_confirmNumber() throws Exception {
        testPhoneNumber_post();
        MvcResult mvcResult = MVC_RESULT.get();

        MockHttpSession newSession = copySession(mvcResult);
        mvcResult = mockMvc.perform(post("/unauth/regist/phone_number/confirm")
                .session(newSession)
                .param("confirmCode", CONFIRM_NUMBER.get().getConfirmCode())
                .param("phoneNumber", "09058020034")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isFound())
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertEquals(RegistUserInfoController.BASE_PATH, redirectedUrl);
    }
}
