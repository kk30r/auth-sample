package kk.sample.auth_server.auth.service;

import java.util.List;
import javax.annotation.Nullable;
import kk.sample.auth_server.auth.entity.UserAccountLock;
import kk.sample.auth_server.auth.repository.UserAccountLockRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Kensuke.Ito
 */
@Service
@lombok.extern.slf4j.Slf4j
public class UserAccountLockService {

    protected final int accountLockThreshold;

    protected final UserAccountLockRepository userAccountLockRepository;

    /**
     * コンストラクタ
     *
     * @param accountLockThreshold
     * @param userAccountLockRepository
     */
    public UserAccountLockService(@Value("${app.security.account_lock.threshold:5}") int accountLockThreshold,
                                  UserAccountLockRepository userAccountLockRepository) {
        this.accountLockThreshold = accountLockThreshold;
        this.userAccountLockRepository = userAccountLockRepository;
    }

    /**
     * ロックの判定
     *
     * @param username
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean isLocked(String username) {
        username = StringUtils.lowerCase(username);
        final UserAccountLock userAccountLock
                = userAccountLockRepository.findOneByUsername(username);
        if (userAccountLock == null) {
            return false;
        }

        // TODO : 時間でのクリア
        final boolean locked = (userAccountLock.getFailureCount() >= accountLockThreshold);
        if (locked == true) {
            log.error("user account locked!!");
        }
        return locked;
    }

    /**
     * ロックのクリア
     *
     * @param username
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearLock(String username) {
        username = StringUtils.lowerCase(username);
        final UserAccountLock userAccountLock
                = findOneByUsernameForUpdate(username);
        if (userAccountLock == null) {
            return;
        }

        userAccountLock.setFailureCount(0L);
        userAccountLockRepository.save(userAccountLock);
    }

    /**
     * 失敗回数のカウントアップ
     *
     * @param username
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementFailureCount(String username) {
        username = StringUtils.lowerCase(username);
        UserAccountLock userAccountLock
                = findOneByUsernameForUpdate(username);
        if (userAccountLock == null) {
            userAccountLock = new UserAccountLock();
            userAccountLock.setUsername(username);
            userAccountLock.setFailureCount(0L);
        }
        if (userAccountLock.getFailureCount() <= accountLockThreshold) {
            // アタックがあった場合オーバーフローが発生する可能性がある為
            // 閾値を越えてなければカウントアップ
            userAccountLock.setFailureCount(userAccountLock.getFailureCount() + 1);
            userAccountLockRepository.save(userAccountLock);
        }
    }

    /**
     * リストで返ってしまう為
     *
     * @param username
     * @return
     */
    @Nullable
    protected UserAccountLock findOneByUsernameForUpdate(String username) {
        username = StringUtils.lowerCase(username);
        final List<UserAccountLock> userAccountLockList
                = userAccountLockRepository.findByUsernameForUpdate(username);
        if (CollectionUtils.isEmpty(userAccountLockList) == true) {
            return null;
        } else if (userAccountLockList.size() != 1) {
            throw new IllegalStateException("expect only one row result but "
                    + userAccountLockList.size());
        }

        return userAccountLockList.get(0);
    }
}
