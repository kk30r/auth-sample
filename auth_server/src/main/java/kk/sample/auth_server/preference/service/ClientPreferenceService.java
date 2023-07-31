package kk.sample.auth_server.preference.service;

import com.google.common.collect.Lists;
import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.Set;
import kk.sample.auth_server.preference.resource.ClientPreferenceResource;
import kk.sample.auth_server.user_info.form.UserInfoForm;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kensuke.Ito
 */
@Service
@lombok.RequiredArgsConstructor
public class ClientPreferenceService {

    private final Validator validator;

    private final ClientPreferenceResource clientPreferenceResource;

    /**
     * ClientPreferenceModel の取得
     *
     * @param clientId
     * @return
     */
    @Nonnull
    public Set<String> getPreference(@Nonnull String clientId) {
        return Validate.notNull(clientPreferenceResource.getPreferenceMap().get(clientId))
                .keySet();
    }

    /**
     * ClientPreferenceModel の取得
     *
     * @param clientId
     * @param field
     * @return
     */
    public boolean isRequiredField(String clientId, String field) {
        return clientPreferenceResource.getPreference(clientId)
                .containsKey(field);
    }

    /**
     * validation group の取得
     *
     * @param clientId
     * @return
     */
    public List<Class<?>> getValidGroups(String clientId) {
        final List<Class<?>> validGroups
                = Lists.newArrayList(Default.class);
        validGroups.addAll(clientPreferenceResource.getPreference(clientId).values());

        return validGroups;
    }

    /**
     * チェック
     *
     * @param clientId
     * @param form
     * @return
     */
    public Set<ConstraintViolation<UserInfoForm>> validate(String clientId, UserInfoForm form) {
        List<Class<?>> validGroups = getValidGroups(clientId);

        return validator.validate(form, validGroups.toArray(Class[]::new));
    }

    /**
     * チェック
     *
     * @param clientId
     * @param form
     * @return
     */
    public boolean isSatisfied(String clientId, UserInfoForm form) {
        return validate(clientId, form).isEmpty();
    }

}
