package kk.sample.auth_server.regist.service;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kensuke.Ito
 */
@Service
@lombok.extern.slf4j.Slf4j
public class RegistEmailService {

    /**
     * 疎通確認
     *
     * @param email
     * @param onConfirmCode
     */
    public void send(@Nonnull String email,
                     @Nonnull Consumer<String> onConfirmCode) {
        final String confirmNumber = generateConfirmNumber();
        sendEmail(email, confirmNumber);

        onConfirmCode.accept(confirmNumber);
    }

    /**
     * 確認用数値生成
     *
     * @return
     */
    protected String generateConfirmNumber() {
        final int confirmNumber = ThreadLocalRandom.current().nextInt(0, 10000);
        return StringUtils.leftPad(Integer.toString(confirmNumber), 4, "0");
    }

    /**
     * TODO : モック的な機能<br>
     *
     * @param email
     * @param confirmNumber
     */
    protected void sendEmail(@Nonnull String email,
                             @Nonnull String confirmNumber) {
        log.info("confirm for email : " + confirmNumber);
    }

    /**
     * 番号確認
     *
     * @param email
     * @param confirmCode
     * @param confirmCodeRetriever
     * @return
     */
    public boolean confirm(@Nonnull String email,
                           @Nonnull String confirmCode,
                           @Nonnull Function<String, ConfirmContext> confirmCodeRetriever) {
        if (StringUtils.isBlank(email) == true) {
            throw new IllegalArgumentException();
        }

        final ConfirmContext confirmContext = confirmCodeRetriever.apply(email);
        if (confirmContext.getExpireAt() < System.currentTimeMillis()) {
            throw new IllegalStateException("confirm code is expired");
        }
        return (StringUtils.equals(email,
                                   confirmContext.getEmail())
                && StringUtils.equals(confirmCode,
                                      confirmContext.getConfirmCode()));
    }

    /**
     *
     */
    @lombok.RequiredArgsConstructor
    @lombok.Getter
    public static class ConfirmContext implements Serializable {

        private static final long serialVersionUID = 1L;

        protected final String email;

        protected final String confirmCode;

        protected final long expireAt;
    }
}
