package kk.sample.auth_server.user_info.form;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * ユーザ情報<br>
 * TODO : 入力チェック
 *
 * @author Kensuke.Ito
 */
@lombok.Getter
@lombok.Setter
public class UserInfoForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    protected String firstName;

    @NotBlank
    protected String lastName;

    @NotBlank(groups = UserInfoFormGroups.Address.class)
    protected String address;

    @NotBlank(groups = UserInfoFormGroups.Address2.class)
    protected String address2;

}
