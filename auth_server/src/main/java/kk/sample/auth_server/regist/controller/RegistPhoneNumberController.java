package kk.sample.auth_server.regist.controller;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import kk.sample.auth_server.controller.ControllerIF;
import kk.sample.auth_server.regist.form.RegistConfirmCodeForm;
import kk.sample.auth_server.regist.form.RegistPhoneNumberForm;
import kk.sample.auth_server.regist.service.RegistPhoneNumberService;
import kk.sample.auth_server.regist.service.RegistPhoneNumberService.ConfirmContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author Kensuke.Ito
 */
@Controller
@RequestMapping(RegistPhoneNumberController.BASE_PATH)
@lombok.RequiredArgsConstructor
public class RegistPhoneNumberController implements ControllerIF {

    public static final String BASE_PATH = "/unauth/regist/phone_number";

    public static final String KEY_CONFIRM_CODE = "RegistPhoneNumberController.confirm_code";

    public static final String KEY_CONFIRMED_PHONENUM = "RegistPhoneNumberController.confirmed_phone_number";

    protected final RegistPhoneNumberService registPhoneNumberService;

    /**
     * 初期表示用
     *
     * @param model
     * @return
     */
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("phoneNumberForm", new RegistPhoneNumberForm());
        return BASE_PATH;
    }

    /**
     * 電話番号疎通確認 送信
     *
     * @param phoneNumberForm
     * @param result
     * @param model
     * @param request
     * @return
     */
    @PostMapping("")
    public String post(@Validated @ModelAttribute RegistPhoneNumberForm phoneNumberForm,
                       BindingResult result,
                       Model model,
                       HttpServletRequest request) {
        model.addAttribute("phoneNumberForm", phoneNumberForm);

        if (hasValidationError(model, result) == true) {
            return BASE_PATH;
        }

        // FIXME : 電話番号疎通確認
        // session に保存すると別ブラウザで処理された場合に参照できなくなるので
        // そこを対応したい場合は送信キックした画面に認証コードを表示し、入力させると better かも
        registPhoneNumberService.send(phoneNumberForm.getPhoneNumber(),
                                      confirmContext -> {
                                          request.getSession().setAttribute(KEY_CONFIRM_CODE,
                                                                            confirmContext);
                                      });

        model.addAttribute("confirmCodeForm", new RegistConfirmCodeForm());
        return BASE_PATH + "_confirm";
    }

    /**
     * 電話番号疎通確認 送信
     *
     * @param confirmCodeForm
     * @param confirmCodeResult
     * @param phoneNumberForm
     * @param phoneNumberResult
     * @param model
     * @param request
     * @return
     */
    @PostMapping("/confirm")
    public String phoneNumberConfirm(@Validated @ModelAttribute RegistConfirmCodeForm confirmCodeForm,
                                     BindingResult confirmCodeResult,
                                     @Validated @ModelAttribute RegistPhoneNumberForm phoneNumberForm,
                                     BindingResult phoneNumberResult,
                                     Model model,
                                     HttpServletRequest request) {
        model.addAttribute("confirmCodeForm", confirmCodeForm);
        model.addAttribute("phoneNumberForm", phoneNumberForm);

        if (hasValidationError(model, confirmCodeResult, phoneNumberResult) == true) {
            return BASE_PATH + "_confirm";
        }

        final ConfirmContext confirmContext
                = (ConfirmContext) request.getSession().getAttribute(KEY_CONFIRM_CODE);
        if (confirmContext == null
                || StringUtils.isNoneEmpty(confirmContext.getPhoneNumber(), confirmContext.getConfirmCode()) == false) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(503),
                                              "failed status for confirm phone number");
        }

        final boolean confirmResult = registPhoneNumberService.confirm(phoneNumberForm.getPhoneNumber(),
                                                                       confirmCodeForm.getConfirmCode(),
                                                                       phoneNum -> confirmContext);
        if (confirmResult == false) {
            final List<String> errorList = Lists.newArrayList(new ObjectError("confirmCode",
                                                                              "confirmCode.invalid").getDefaultMessage());
            model.addAttribute("validationError", errorList);
            return BASE_PATH + "_confirm";
        }

        request.getSession().removeAttribute(KEY_CONFIRM_CODE);
        request.getSession().setAttribute(KEY_CONFIRMED_PHONENUM,
                                          confirmContext.getPhoneNumber());

        return "redirect:" + RegistUserInfoController.BASE_PATH;
    }
}
