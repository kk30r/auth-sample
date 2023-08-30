package kk.sample.auth_server.regist.controller;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import kk.sample.auth_server.regist.form.RegistConfirmCodeForm;
import kk.sample.auth_server.regist.form.RegistPhoneNumberForm;
import kk.sample.auth_server.regist.service.RegistPhoneNumberService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
@RequestMapping("/unauth/regist")
@lombok.RequiredArgsConstructor
public class RegistPhoneNumberController {

    public static final String BASE_PATH = "unauth/regist/phone_number";

    public static final String KEY_CONFIRM_CODE = RegistPhoneNumberController.class.getSimpleName() + ".confirm_code";

    public static final String KEY_CONFIRMED_PHONENUM = RegistPhoneNumberController.class.getSimpleName() + ".confirmed_phone_number";

    protected final RegistPhoneNumberService registPhoneNumberService;

    /**
     * 初期表示用
     *
     * @param model
     * @return
     */
    @GetMapping("/phone_number")
    public String phoneNumber(Model model) {
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
    @PostMapping("/phone_number")
    public String phoneNumberSent(@Validated @ModelAttribute RegistPhoneNumberForm phoneNumberForm,
                                  BindingResult result,
                                  Model model,
                                  HttpServletRequest request) {
        model.addAttribute("phoneNumberForm", phoneNumberForm);

        if (result.hasErrors()) {
            final List<String> errorList = Lists.newArrayList();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return BASE_PATH;
        }

        // FIXME : 電話番号疎通確認
        // session に保存すると別ブラウザで処理された場合に参照できなくなるので
        // そこを対応したい場合は送信キックした画面に認証コードを表示し、入力させると better かも
        registPhoneNumberService.send(phoneNumberForm.getPhoneNumber(),
                                      confirmCode -> {
                                          request.getSession().setAttribute(KEY_CONFIRM_CODE,
                                                                            Pair.of(phoneNumberForm.getPhoneNumber(),
                                                                                    confirmCode));
                                      });

        model.addAttribute("confirmCodeForm", new RegistConfirmCodeForm());
        return BASE_PATH + "_confirm";
    }

    /**
     * 電話番号疎通確認 送信
     *
     * @param confirmCodeForm
     * @param result
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/phone_number/confirm")
    public String phoneNumberConfirm(@Validated @ModelAttribute RegistConfirmCodeForm confirmCodeForm,
                                     BindingResult result,
                                     Model model,
                                     HttpServletRequest request) {
        model.addAttribute("confirmCodeForm", confirmCodeForm);

        if (result.hasErrors()) {
            final List<String> errorList = Lists.newArrayList();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return BASE_PATH + "_confirm";
        }

        final Pair<String, String> confirmPair
                = (Pair<String, String>) request.getSession().getAttribute(KEY_CONFIRM_CODE);
        if (confirmPair == null
                || StringUtils.isNoneEmpty(confirmPair.getKey(), confirmPair.getValue()) == false) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(503),
                                              "failed status for confirm phone number");
        }

        final String phoneNumber = confirmPair.getKey();

        final boolean confirmResult = registPhoneNumberService.confirm(phoneNumber,
                                                                       confirmCodeForm.getConfirmCode(),
                                                                       phoneNum -> confirmPair.getValue());
        if (confirmResult == false) {
            final List<String> errorList = Lists.newArrayList(new ObjectError("confirmCode",
                                                                              "confirmCode.invalid").getDefaultMessage());
            model.addAttribute("validationError", errorList);
            return BASE_PATH + "_confirm";
        }

        request.getSession().removeAttribute(KEY_CONFIRM_CODE);
        request.getSession().setAttribute(KEY_CONFIRMED_PHONENUM,
                                          phoneNumber);
        return "redirect:/unauth/regist/profile";
    }
}
