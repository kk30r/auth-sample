package kk.sample.auth_server.controller;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 *
 * @author Kensuke.Ito
 */
public interface ControllerIF {

    /**
     * エラー発生チェック
     *
     * @param model
     * @param bindingResults
     * @return
     */
    default boolean hasValidationError(Model model,
                                       BindingResult... bindingResults) {
        List<String> errorList = null;
        for (BindingResult result : bindingResults) {
            if (result.hasErrors()) {
                if (errorList == null) {
                    errorList = Lists.newArrayList();
                }
                for (ObjectError error : result.getAllErrors()) {
                    errorList.add(error.getDefaultMessage());
                }
                model.addAttribute("validationError", errorList);
                return true;
            }
        }
        return false;
    }
}
