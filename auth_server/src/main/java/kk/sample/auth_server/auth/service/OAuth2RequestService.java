package kk.sample.auth_server.auth.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author Kensuke.Ito
 */
@Service
public class OAuth2RequestService {

    protected RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * SavedRequest の取得
     *
     * @return
     */
    @Nullable
    public SavedRequest retrieveSavedRequest() {
        final ServletRequestAttributes reqAttr = Validate.notNull(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()));
        final HttpServletRequest request = reqAttr.getRequest();
        final HttpServletResponse response = reqAttr.getResponse();

        return this.requestCache.getRequest(request, response);
    }

    /**
     * 保存されているパラメータを取得
     *
     * @param paramName
     * @return
     */
    public String getSavedParameter(String paramName) {
        final SavedRequest savedRequest = retrieveSavedRequest();
        if (savedRequest == null) {
            return null;
        }

        final String[] params = savedRequest.getParameterValues(OAuth2ParameterNames.CLIENT_ID);
        if (ArrayUtils.isEmpty(params) == true) {
            return null;
        }
        return params[0];
    }

    /**
     * 保存されている client_id を取得
     *
     * @return
     */
    public String getSavedClientId() {
        return getSavedParameter(OAuth2ParameterNames.CLIENT_ID);

    }
}
