package kk.sample.auth_server.user_info.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kk.sample.auth_server.auth.entity.UserInfo;
import kk.sample.auth_server.auth.repository.UserInfoRepository;
import kk.sample.auth_server.auth.userdetails.UserDetailsExt;
import kk.sample.auth_server.controller.ControllerIF;
import kk.sample.auth_server.user_info.form.EmailForm;
import kk.sample.auth_server.user_info.form.UserInfoForm;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author Kensuke.Ito
 */
@Controller
@lombok.RequiredArgsConstructor
public class UserInfoController implements ControllerIF {

    // TODO : Service や Logic 層を作るならそちらへ移動
    protected final UserInfoRepository userInfoRepository;

    protected final AuthenticationSuccessHandler formAuthenticationSuccessHandler;

    /**
     * 初期表示用
     *
     * @param userDetail
     * @param model
     * @return
     */
    @GetMapping("/user_info")
    public String index(@AuthenticationPrincipal UserDetailsExt userDetail,
                        Model model) {
        final UserInfoForm userInfoForm = new UserInfoForm();
        BeanUtils.copyProperties(userDetail, userInfoForm);
        model.addAttribute("userInfoForm", userInfoForm);

        final EmailForm emailForm = new EmailForm();
        BeanUtils.copyProperties(userDetail, emailForm);
        model.addAttribute("emailForm", emailForm);

        return "user_info";
    }

    /**
     * 設定用
     *
     * @param userDetail
     * @param userInfoForm
     * @param userInfoResult
     * @param emailForm
     * @param emailResult
     * @param model
     * @param request
     * @param response
     * @param authentication
     * @return
     * @throws Exception
     */
    @PostMapping("/user_info")
    @Transactional(rollbackFor = Exception.class)
    public String post(@AuthenticationPrincipal UserDetailsExt userDetail,
                       @Validated @ModelAttribute UserInfoForm userInfoForm,
                       BindingResult userInfoResult,
                       @Validated @ModelAttribute EmailForm emailForm,
                       BindingResult emailResult,
                       Model model,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) throws Exception {
        model.addAttribute("userInfoForm", userInfoForm);
        model.addAttribute("emailForm", emailForm);

        if (hasValidationError(model, userInfoResult, emailResult) == true) {
            return "/user_info";
        }
        {
            final UserInfo userInfo = new UserInfo();
            BeanUtils.copyProperties(userInfoForm, userInfo);
            BeanUtils.copyProperties(emailForm, userInfo);
            userInfo.setUsersId(userDetail.getId());
            userInfoRepository.save(userInfo);

            BeanUtils.copyProperties(userInfoForm, userDetail);
            BeanUtils.copyProperties(emailForm, userDetail);
        }

        formAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // 正常にリダイレクトされるはず
        return "user_info";
    }

}
