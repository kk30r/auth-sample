package kk.sample.auth_server.regist.service;

import jakarta.annotation.Nonnull;
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
public class RegistPhoneNumberService {

    /**
     * 疎通確認
     *
     * @param phoneNumber
     * @param onConfirmNumber
     */
    public void send(@Nonnull String phoneNumber,
                     @Nonnull Consumer<String> onConfirmNumber) {
        final String confirmNumber = generateConfirmNumber();
        sendSms(phoneNumber, confirmNumber);

        onConfirmNumber.accept(confirmNumber);
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
     * @param phoneNumber
     * @param confirmNumber
     */
    protected void sendSms(@Nonnull String phoneNumber,
                           @Nonnull String confirmNumber) {
        log.info(confirmNumber);
    }

    /**
     * 番号確認
     *
     * @param phoneNumber
     * @param confirmNumber
     * @param confirmNumberRetriever
     * @return
     */
    public boolean confirm(@Nonnull String phoneNumber,
                           @Nonnull String confirmNumber,
                           @Nonnull Function<String, String> confirmNumberRetriever) {
        final String expectConfirmNumber = confirmNumberRetriever.apply(phoneNumber);
        if (StringUtils.isBlank(phoneNumber) == true) {
            throw new IllegalArgumentException();
        }
        return StringUtils.equals(confirmNumber, expectConfirmNumber);
    }
}
