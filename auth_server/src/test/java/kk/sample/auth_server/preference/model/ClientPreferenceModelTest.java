package kk.sample.auth_server.preference.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kk.sample.auth_server.preference.model.ClientPreferenceModel.ClientPreferenceFieldModel;
import org.junit.jupiter.api.Test;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames.Token;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

/**
 *
 * @author kensuke
 */
public class ClientPreferenceModelTest {

    @Test
    public void test() throws Exception {
        ObjectMapper objMapper = new ObjectMapper(new YAMLFactory());

        List<ClientPreferenceModel> src = Lists.newArrayList();
        {
            ClientPreferenceModel pref = new ClientPreferenceModel();
            src.add(pref);
            pref.setClientId("messaging-client");

            pref.setRequiredFields(Lists.newArrayList());
            {
                ClientPreferenceFieldModel field = new ClientPreferenceFieldModel();
                field.setField("address");
                pref.getRequiredFields().add(field);
            }
            {
                ClientPreferenceFieldModel field = new ClientPreferenceFieldModel();
                field.setField("address2");
                field.setRequired(false);
                pref.getRequiredFields().add(field);
            }
        }

        File file = new File("src/test/resources/client_pref.yml");
        objMapper.writeValue(file, src);

        List<ClientPreferenceModel> dest
                = objMapper.readValue(file, new TypeReference<List<ClientPreferenceModel>>() {
                              });
        System.out.println(new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(dest));
    }

    @Test
    public void test2() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        ClassLoader classLoader = JdbcRegisteredClientRepository.class.getClassLoader();
        List<com.fasterxml.jackson.databind.Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());

        Map<String, Object> map = new HashMap();
        map.put(Token.AUTHORIZATION_CODE_TIME_TO_LIVE, Duration.ofMinutes(5));
        map.put(Token.ACCESS_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(5));
        map.put(Token.ACCESS_TOKEN_FORMAT, OAuth2TokenFormat.SELF_CONTAINED);
        map.put(Token.DEVICE_CODE_TIME_TO_LIVE, Duration.ofMinutes(5));
        map.put(Token.REUSE_REFRESH_TOKENS, true);
        map.put(Token.REFRESH_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(60));
        map.put(Token.ID_TOKEN_SIGNATURE_ALGORITHM, SignatureAlgorithm.RS256);

        System.out.println(objectMapper.writeValueAsString(map));
    }

    @Test
    public void test3() throws Exception {
        System.out.println(Duration.ofMinutes(5));
        System.out.println(Duration.parse("PT5M").toString());

    }
}
