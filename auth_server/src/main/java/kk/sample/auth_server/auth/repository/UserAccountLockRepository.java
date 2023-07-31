package kk.sample.auth_server.auth.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import kk.sample.auth_server.auth.entity.UserAccountLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Kensuke.Ito
 */
@Repository
public interface UserAccountLockRepository extends JpaRepository<UserAccountLock, Long> {

    UserAccountLock findOneByUsername(String username);

    /**
     * 更新用ロック取得
     *
     * @param username
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000"))
    @Query("SELECT l FROM UserAccountLock l WHERE l.username = :username")
    List<UserAccountLock> findByUsernameForUpdate(@Param("username") String username);
}
