package kk.sample.auth_server.regist.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 *
 * @author Kensuke.Ito
 */
@lombok.Getter
@lombok.Setter
public class RegistConfirmCodeForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    // ハイフンなし
    @Pattern(regexp = "^\\d{4}$")
    protected String confirmCode;

}
