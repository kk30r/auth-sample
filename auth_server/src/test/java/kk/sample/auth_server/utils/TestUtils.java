/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kk.sample.auth_server.utils;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

/**
 *
 * @author Kensuke.Ito
 */
public class TestUtils {

    /**
     *
     * @param mvcResult
     * @return
     */
    public static MockHttpSession copySession(MvcResult mvcResult) {
        HttpSession sessionOrg = mvcResult.getRequest().getSession();
        final MockHttpSession newSession = new MockHttpSession();
        Collections.list(sessionOrg.getAttributeNames()).stream()
                .forEach(name -> {
                    newSession.setAttribute(name, sessionOrg.getAttribute(name));
                });
        return newSession;
    }
}
