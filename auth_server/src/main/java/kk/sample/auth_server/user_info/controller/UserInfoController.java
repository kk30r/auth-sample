package kk.sample.auth_server.user_info.controller;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import kk.sample.auth_server.auth.entity.UserInfo;
import kk.sample.auth_server.auth.repository.UserInfoRepository;
import kk.sample.auth_server.auth.userdetails.UserDetailsExt;
import kk.sample.auth_server.user_info.form.UserInfoForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author Kensuke.Ito
 */
@Controller
@lombok.RequiredArgsConstructor(onConstructor = @__(
        @Autowired))
public class UserInfoController {

    // TODO : Service や Logic 層を作るならそちらへ移動
    protected final UserInfoRepository userInfoRepository;

    protected final AuthenticationSuccessHandler formAuthenticationSuccessHandler;

    protected final Validator validator;

    /**
     * 初期表示用
     *
     * @param userDetail
     * @param model
     * @return
     */
    @GetMapping("/user_info")
    public String userInfoGet(@AuthenticationPrincipal UserDetailsExt userDetail,
                              Model model) {
        final UserInfoForm form = new UserInfoForm();
        BeanUtils.copyProperties(userDetail, form);
        model.addAttribute("userInfoForm", form);
        return "user_info";
    }

    /**
     * 設定用
     *
     * @param userDetail
     * @param userInfoForm
     * @param model
     * @param result
     * @param request
     * @param response
     * @param authentication
     * @return
     * @throws Exception
     */
    @PostMapping("/user_info")
    @Transactional(rollbackFor = Exception.class)
    public String userInfoPost(@AuthenticationPrincipal UserDetailsExt userDetail,
                               @Validated @ModelAttribute UserInfoForm userInfoForm,
                               BindingResult result,
                               Model model,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Authentication authentication) throws Exception {
        model.addAttribute("userInfoForm", userInfoForm);

        if (result.hasErrors()) {
            final List<String> errorList = Lists.newArrayList();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return "/user_info";
        }

        {
            final UserInfo userInfo = new UserInfo();
            BeanUtils.copyProperties(userInfoForm, userInfo);
            userInfo.setUsersId(userDetail.getId());
            userInfoRepository.save(userInfo);

            BeanUtils.copyProperties(userInfoForm, userDetail);
        }

        formAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // 正常にリダイレクトされるはず
        return "user_info";
    }

}
