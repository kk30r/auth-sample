package kk.sample.auth_server.auth.config;

import java.util.List;
import java.util.stream.Collectors;
import kk.sample.auth_server.auth.service.OAuth2RequestService;
import kk.sample.auth_server.preference.service.ClientPreferenceService;
import kk.sample.auth_server.preference.web.servlet.mvc.method.annotation.PreferenceServletModelAttributeMethodProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

/**
 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addArgumentResolvers}
 * を利用してしまうと 処理が ServletModelAttributeMethodProcessor に食われてしまうので置換を実施。
 *
 * @author Kensuke.Ito
 */
@Configuration
public class PreferenceConfig implements ApplicationContextAware, InitializingBean {

    @lombok.Setter
    ApplicationContext applicationContext;

    /**
     * {@inheritDoc }
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        RequestMappingHandlerAdapter adapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
        List<HandlerMethodArgumentResolver> argResolversOrg = adapter.getArgumentResolvers();

        // ServletModelAttributeMethodProcessor を置換
        List<HandlerMethodArgumentResolver> argtResolversNew = argResolversOrg.stream()
                .map(argResolver -> {
                    if (argResolver instanceof ServletModelAttributeMethodProcessor) {
                        OAuth2RequestService oauth2RequestService
                                = applicationContext.getBean(OAuth2RequestService.class);
                        ClientPreferenceService clientPreferenceService
                                = applicationContext.getBean(ClientPreferenceService.class);
                        return new PreferenceServletModelAttributeMethodProcessor(oauth2RequestService,
                                                                                  clientPreferenceService);
                    }
                    return argResolver;
                }).collect(Collectors.toList());

        adapter.setArgumentResolvers(argtResolversNew);
    }
}
