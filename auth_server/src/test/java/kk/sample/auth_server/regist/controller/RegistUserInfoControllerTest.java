package kk.sample.auth_server.regist.controller;

import static kk.sample.auth_server.regist.controller.RegistPhoneNumberControllerTest.CONFIRM_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author kensuke
 */
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Rollback
public class RegistUserInfoControllerTest {

    protected static final ThreadLocal<MvcResult> MVC_RESULT = new ThreadLocal();

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        CONFIRM_NUMBER.remove();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_index_withoutSession() throws Exception {
        mockMvc.perform(get("/unauth/regist/user_info"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPhoneNumber_index() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/unauth/regist/user_info")
                .sessionAttr(RegistPhoneNumberController.KEY_CONFIRMED_PHONENUM, "09058020034"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("/unauth/regist/user_info",
                     mvcResult.getModelAndView().getViewName());

        MVC_RESULT.set(mvcResult);
    }

}
