package kk.sample.auth_server.preference.model;

import java.util.List;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.Getter
@lombok.Setter
public class ClientPreferenceModel {

    protected String clientId;

    protected List<ClientPreferenceFieldModel> requiredFields;

    @lombok.Getter
    @lombok.Setter
    public static class ClientPreferenceFieldModel {

        protected String field;

        protected Boolean required = Boolean.TRUE;
    }
}
