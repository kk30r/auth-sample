package kk.sample.auth_server.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kensuke.Ito
 */
@Component
@ConfigurationProperties(prefix = "spring.security")
@lombok.Getter
@lombok.Setter
public class SecurityConfigProperties {

    protected String loginUrl = "/login";
}
