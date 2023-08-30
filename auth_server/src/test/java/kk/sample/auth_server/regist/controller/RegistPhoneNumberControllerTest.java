package kk.sample.auth_server.regist.controller;

import static kk.sample.auth_server.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author kensuke
 */
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Rollback
public class RegistPhoneNumberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_init() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/unauth/regist/phone_number"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("unauth/regist/phone_number",
                     mvcResult.getModelAndView().getViewName());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_post() throws Exception {
        MvcResult mvcResult;

        {
            mvcResult = mockMvc.perform(get("/unauth/regist/phone_number"))
                    .andExpect(status().isOk())
                    .andReturn();
            assertEquals("unauth/regist/phone_number",
                         mvcResult.getModelAndView().getViewName());
        }

        MockHttpSession newSession = copySession(mvcResult);
        mvcResult = mockMvc.perform(post("/unauth/regist/phone_number")
                .session(newSession)
                .param("phoneNumber", "09058020034")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("unauth/regist/phone_number_confirm",
                     mvcResult.getModelAndView().getViewName());

    }

}
