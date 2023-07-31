/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kk.sample.auth_client.web.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import kk.sample.auth_client.mock.user.MyOidcUser;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Kensuke.Ito
 */
@RestController
public class ClientController {

    @GetMapping("/")
    public Map<?, ?> index(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            final OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) principal;

            Map<String, Object> maps = new LinkedHashMap<>();
            maps.put("name", auth.getName());

            OAuth2User user = auth.getPrincipal();
            maps.putAll(user.getAttributes());

            Object details = auth.getDetails();
            maps.put("details", details.getClass().toString());

            Object credentials = auth.getCredentials();
            maps.put("credentials", credentials.getClass().toString());

            maps.put("getAuthorities", auth.getAuthorities());
            return maps;
        } else if (principal instanceof MyOidcUser) {
            final MyOidcUser auth = (MyOidcUser) principal;

            return Map.of("name", auth.getName(),
                          "getAuthorities", auth.getAuthorities());
        }
        return Map.of("name", principal.getName());
    }
}
