package kk.sample.auth_server.regist.controller;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import kk.sample.auth_server.auth.userdetails.UserDetailsExt;
import kk.sample.auth_server.auth.userdetails.UserDetailsServiceExt;
import kk.sample.auth_server.controller.ControllerIF;
import kk.sample.auth_server.regist.form.RegistConfirmCodeForm;
import kk.sample.auth_server.regist.form.RegistUserInfoForm;
import kk.sample.auth_server.regist.service.RegistEmailService;
import kk.sample.auth_server.regist.service.RegistEmailService.ConfirmContext;
import kk.sample.auth_server.user_info.form.EmailForm;
import kk.sample.auth_server.user_info.form.PasswordForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author Kensuke.Ito
 */
@Controller
@RequestMapping(RegistUserInfoController.BASE_PATH)
@lombok.RequiredArgsConstructor
public class RegistUserInfoController implements ControllerIF {

    public static final String BASE_PATH = "/unauth/regist/user_info";

    public static final String KEY_CONFIRM_CODE = "RegistUserInfoController.confirm_code";

    public static final String KEY_FORM = "RegistUserInfoController.user_info_form";

    public static final String KEY_CONFIRMED_EMAIL = "RegistUserInfoController.confirmed_email";

    protected final UserDetailsServiceExt userDetailsService;

    protected final RegistEmailService registEmailService;

    /**
     * 初期表示用
     *
     * @param confirmedPhoneNum
     * @param model
     * @param request
     * @return
     */
    @GetMapping("")
    public String index(@SessionAttribute(RegistPhoneNumberController.KEY_CONFIRMED_PHONENUM) String confirmedPhoneNum,
                        Model model,
                        HttpServletRequest request) {
        model.addAttribute("phoneNumber", confirmedPhoneNum);
        model.addAttribute("userInfoForm", new RegistUserInfoForm());
        model.addAttribute("passwordForm", new PasswordForm());
        model.addAttribute("emailForm", new EmailForm());
        return BASE_PATH;
    }

    /**
     * 送信
     *
     * @param confirmedPhoneNum
     * @param userInfoForm
     * @param userInfoResult
     * @param passordForm
     * @param passordResult
     * @param emailForm
     * @param emailResult
     * @param model
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("")
    public String post(@SessionAttribute(RegistPhoneNumberController.KEY_CONFIRMED_PHONENUM) String confirmedPhoneNum,
                       @Validated @ModelAttribute RegistUserInfoForm userInfoForm,
                       BindingResult userInfoResult,
                       @Validated @ModelAttribute PasswordForm passordForm,
                       BindingResult passordResult,
                       @Validated @ModelAttribute EmailForm emailForm,
                       BindingResult emailResult,
                       Model model,
                       HttpServletRequest request) {
        model.addAttribute("phoneNumber", confirmedPhoneNum);
        model.addAttribute("userInfoForm", userInfoForm);
        model.addAttribute("passwordForm", passordForm);
        model.addAttribute("emailForm", emailForm);

        if (hasValidationError(model, userInfoResult, passordResult, emailResult) == true) {
            return BASE_PATH;
        }

        // メールアドレスが入力されてれば確認コード送信
        if (StringUtils.isBlank(emailForm.getEmail()) == false) {
            registEmailService.send(emailForm.getEmail(),
                                    confirmContext -> {
                                        request.getSession().setAttribute(KEY_CONFIRM_CODE,
                                                                          confirmContext);
                                    });
            request.getSession().setAttribute(KEY_FORM,
                                              Pair.of(userInfoForm, passordForm));
            return BASE_PATH + "_confirm";
        }

        return registProcess(userInfoForm,
                             passordForm,
                             confirmedPhoneNum,
                             null,
                             request);
    }

    /**
     * email 疎通確認 送信
     *
     * @param confirmedPhoneNum
     * @param confirmCodeForm
     * @param confirmCodeResult
     * @param emailForm
     * @param emailResult
     * @param model
     * @param request
     * @return
     */
    @PostMapping("/confirm")
    public String emailConfirm(@SessionAttribute(RegistPhoneNumberController.KEY_CONFIRMED_PHONENUM) String confirmedPhoneNum,
                               @Validated @ModelAttribute RegistConfirmCodeForm confirmCodeForm,
                               BindingResult confirmCodeResult,
                               @Validated @ModelAttribute EmailForm emailForm,
                               BindingResult emailResult,
                               Model model,
                               HttpServletRequest request) {
        model.addAttribute("confirmCodeForm", confirmCodeForm);
        model.addAttribute("emailForm", emailForm);

        if (hasValidationError(model, emailResult, confirmCodeResult) == true) {
            return BASE_PATH + "_confirm";
        }

        final ConfirmContext confirmContext
                = (ConfirmContext) request.getSession().getAttribute(KEY_CONFIRM_CODE);
        if (confirmContext == null
                || StringUtils.isNoneEmpty(confirmContext.getEmail(), confirmContext.getConfirmCode()) == false) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(503),
                                              "failed status for confirm phone number");
        }

        final boolean confirmResult = registEmailService.confirm(emailForm.getEmail(),
                                                                 confirmCodeForm.getConfirmCode(),
                                                                 email -> confirmContext);
        if (confirmResult == false) {
            final List<String> errorList = Lists.newArrayList(new ObjectError("confirmCode",
                                                                              "confirmCode.invalid").getDefaultMessage());
            model.addAttribute("validationError", errorList);
            return BASE_PATH + "_confirm";
        }

        final Pair<RegistUserInfoForm, PasswordForm> formPair
                = (Pair) request.getSession().getAttribute(KEY_FORM);
        registProcess(formPair.getLeft(),
                      formPair.getRight(),
                      confirmedPhoneNum,
                      emailForm.getEmail(),
                      request);

        return "redirect:" + RegistUserInfoController.BASE_PATH;
    }

    /**
     * 登録処理
     *
     * @param userInfoForm
     * @param passordForm
     * @param confirmedPhoneNum
     * @param confirmedEmail
     * @param request
     * @return
     */
    protected String registProcess(RegistUserInfoForm userInfoForm,
                                   PasswordForm passordForm,
                                   String confirmedPhoneNum,
                                   String confirmedEmail,
                                   HttpServletRequest request) {
        registUserInfo(userInfoForm, passordForm, confirmedPhoneNum, confirmedEmail);

        // flush session
        request.getSession().removeAttribute(RegistPhoneNumberController.KEY_CONFIRMED_PHONENUM);
        request.getSession().removeAttribute(KEY_CONFIRM_CODE);
        request.getSession().removeAttribute(KEY_FORM);

        return "redirect:/login";
    }

    /**
     *
     * @param userInfoForm
     * @param passordForm
     * @param confirmedPhoneNum
     * @param confirmedEmail
     */
    protected void registUserInfo(RegistUserInfoForm userInfoForm,
                                  PasswordForm passordForm,
                                  String confirmedPhoneNum,
                                  String confirmedEmail) {
        final UserDetailsExt user = new UserDetailsExt();

        BeanUtils.copyProperties(userInfoForm, user);
        BeanUtils.copyProperties(passordForm, user);
        user.setUsername(confirmedPhoneNum);
        user.setEmail(confirmedEmail);
        user.setId(null);
        user.setEnabled(true);

        userDetailsService.createUser(user);
    }

}
