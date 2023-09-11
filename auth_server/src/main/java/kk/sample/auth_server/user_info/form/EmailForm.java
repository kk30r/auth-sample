package kk.sample.auth_server.user_info.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.Getter
@lombok.Setter
public class EmailForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 5. Restrict Char Length in Top-level Domain [Recommended]<br>
     *
     * @see
     * https://howtodoinjava.com/java/regex/java-regex-validate-email-address/
     */
    @NotBlank(groups = UserInfoFormGroups.EMAIL.class)
    @Pattern(regexp = "^[\\\\w!#$%&’*+/=?`{|}~^-]+(?:\\\\.[\\\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,6}$",
            groups = UserInfoFormGroups.EMAIL.class)
    protected String email;
}
