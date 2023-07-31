package kk.sample.auth_server.preference.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import jakarta.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;
import kk.sample.auth_server.user_info.form.UserInfoFormGroups;
import kk.sample.auth_server.preference.model.ClientPreferenceModel;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kensuke.Ito
 */
@Service
public class ClientPreferenceResource {

    @lombok.Getter
    protected final Map<String, Map<String, Class<? extends UserInfoFormGroups>>> preferenceMap;

    /**
     * コンストラクタ
     *
     * @param settingPath
     * @throws Exception
     */
    public ClientPreferenceResource(@Value("${client_preference.setting_path}") File settingPath) throws Exception {
        preferenceMap = load(settingPath);
    }

    /**
     * 設定のロード
     *
     * @param settingPath
     * @return
     * @throws Exception
     */
    protected Map<String, Map<String, Class<? extends UserInfoFormGroups>>> load(File settingPath) throws Exception {
        final ObjectMapper objMapper = new ObjectMapper(new YAMLFactory());

        final List<ClientPreferenceModel> dest
                = objMapper.readValue(settingPath, new TypeReference<List<ClientPreferenceModel>>() {
                              });

        final Map<String, Map<String, Class<? extends UserInfoFormGroups>>> map
                = Maps.newHashMap();
        dest.stream()
                .forEach(model -> {
                    final Map<String, Class<? extends UserInfoFormGroups>> fieldMap
                            = map.computeIfAbsent(model.getClientId(),
                                                  k -> Maps.newHashMap());

                    model.getRequiredFields().stream()
                            .filter(field -> BooleanUtils.isTrue(field.getRequired())
                            && StringUtils.isNotBlank(field.getField()))
                            .forEach(field -> {
                                try {
                                    String clazName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getField());
                                    Class claz = Class.forName(UserInfoFormGroups.class.getTypeName() + "$" + clazName);
                                    fieldMap.put(field.getField(),
                                                 claz);
                                } catch (Exception e) {
                                    throw new IllegalStateException("can't find validation group interface", e);
                                }
                            });
                });

        return map;
    }

    /**
     * Preference map の取得
     *
     * @param clientId
     * @return
     */
    @Nonnull
    public Map<String, Class<? extends UserInfoFormGroups>> getPreference(@Nonnull String clientId) {
        return Validate.notNull(preferenceMap.get(clientId));
    }

}
