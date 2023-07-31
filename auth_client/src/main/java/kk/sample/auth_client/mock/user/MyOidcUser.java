package kk.sample.auth_client.mock.user;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 *
 * @author Kensuke.Ito
 */
@Getter
public class MyOidcUser extends DefaultOidcUser {

    // アプリ固有のプロパティ
    private String orgParam1;
    private String orgParam2;
    private String orgParam3;

    public MyOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, OidcUserInfo userInfo, MyUser myUser) {
        super(authorities, idToken, userInfo);
        this.orgParam1 = myUser.getOrgParam1();
        this.orgParam2 = myUser.getOrgParam2();
        this.orgParam3 = myUser.getOrgParam3();
    }
}
