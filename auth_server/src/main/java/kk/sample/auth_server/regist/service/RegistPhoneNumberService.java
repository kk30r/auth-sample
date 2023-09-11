package kk.sample.auth_server.regist.service;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kensuke.Ito
 */
@Service
@lombok.extern.slf4j.Slf4j
public class RegistPhoneNumberService {

    @Value("${phone_number.confirm_code.expire_millis:1800000}")
    protected long expireMillis = TimeUnit.MINUTES.toMillis(30);

    /**
     * 疎通確認
     *
     * @param phoneNumber
     * @param onConfirmCode
     */
    public void send(@Nonnull String phoneNumber,
                     @Nonnull Consumer<ConfirmContext> onConfirmCode) {
        final ConfirmContext confirmContext = generateConfirmCode(phoneNumber);
        sendSms(confirmContext);

        onConfirmCode.accept(confirmContext);
    }

    /**
     * 確認用数値生成
     *
     * @param phoneNumber
     * @return
     */
    protected ConfirmContext generateConfirmCode(@Nonnull String phoneNumber) {
        final int num = ThreadLocalRandom.current().nextInt(0, 10000);
        final String confirmCode = StringUtils.leftPad(Integer.toString(num), 4, "0");

        final long expireAt = System.currentTimeMillis() + expireMillis;

        return new ConfirmContext(phoneNumber, confirmCode, expireAt);
    }

    /**
     * TODO : モック的な機能<br>
     *
     * @param confirmContext
     */
    protected void sendSms(@Nonnull final ConfirmContext confirmContext) {
        log.info("confirm for phone number : " + confirmContext.getConfirmCode());
    }

    /**
     * 番号確認
     *
     * @param phoneNumber
     * @param confirmCode
     * @param confirmCodeRetriever
     * @return
     */
    public boolean confirm(@Nonnull String phoneNumber,
                           @Nonnull String confirmCode,
                           @Nonnull Function<String, ConfirmContext> confirmCodeRetriever) {
        if (StringUtils.isBlank(phoneNumber) == true) {
            throw new IllegalArgumentException();
        }

        final ConfirmContext confirmContext = confirmCodeRetriever.apply(phoneNumber);
        if (confirmContext.getExpireAt() < System.currentTimeMillis()) {
            throw new IllegalStateException("confirm code is expired");
        }
        return (StringUtils.equals(phoneNumber,
                                   confirmContext.getPhoneNumber())
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

        protected final String phoneNumber;

        protected final String confirmCode;

        protected final long expireAt;
    }
}
