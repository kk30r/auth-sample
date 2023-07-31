package kk.sample.auth_server.preference.web.servlet.mvc.method.annotation;

import java.util.List;
import kk.sample.auth_server.auth.service.OAuth2RequestService;
import kk.sample.auth_server.preference.service.ClientPreferenceService;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

/**
 * client_id によって適用する validation group を動的に切り替える
 *
 * @author Kensuke.Ito
 */
public class PreferenceServletModelAttributeMethodProcessor extends ServletModelAttributeMethodProcessor {

    protected final OAuth2RequestService oauth2RequestService;

    protected final ClientPreferenceService clientPreferenceService;

    /**
     * コンストラクタ
     *
     * @param oauth2RequestService
     * @param clientPreferenceService
     */
    public PreferenceServletModelAttributeMethodProcessor(OAuth2RequestService oauth2RequestService,
                                                          ClientPreferenceService clientPreferenceService) {
        this(false,
             oauth2RequestService,
             clientPreferenceService);
    }

    /**
     * コンストラクタ
     *
     * @param annotationNotRequired
     * @param oauth2RequestService
     * @param clientPreferenceService
     */
    public PreferenceServletModelAttributeMethodProcessor(boolean annotationNotRequired,
                                                          OAuth2RequestService oauth2RequestService,
                                                          ClientPreferenceService clientPreferenceService) {
        super(annotationNotRequired);
        this.oauth2RequestService = oauth2RequestService;
        this.clientPreferenceService = clientPreferenceService;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        final String clientId = oauth2RequestService.getSavedClientId();
        if (clientId == null) {
            super.validateIfApplicable(binder, parameter);
            return;
        }

        final List<Class<?>> validGroups
                = clientPreferenceService.getValidGroups(clientId);
        if (validGroups.isEmpty() == false) {
            binder.validate(validGroups.toArray(Object[]::new));
        }

    }
}
