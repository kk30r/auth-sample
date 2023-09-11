package kk.sample.auth_server.user_info.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.Getter
@lombok.Setter
public class PasswordForm implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    @NotBlank
    @Pattern(regexp = PASSWORD_PATTERN)
    protected String password;

    @NotBlank
    @Pattern(regexp = PASSWORD_PATTERN)
    protected String confirmPassword;

    /**
     *
     * @return
     */
    @AssertTrue(message = "パスワードが一致しません")
    public boolean isSamePassword() {
        return (StringUtils.isAnyBlank(password, confirmPassword) == false
                && StringUtils.equals(password, confirmPassword));
    }
}
