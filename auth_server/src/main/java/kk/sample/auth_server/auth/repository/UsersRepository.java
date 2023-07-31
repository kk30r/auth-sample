package kk.sample.auth_server.auth.repository;

import kk.sample.auth_server.auth.entity.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Kensuke.Ito
 */
@Repository
public interface UsersRepository extends CrudRepository<Users, Long> {

    /**
     * 名前による検索
     *
     * @param username
     * @return
     */
    Users findByUsername(String username);

    /**
     * 名前による存在確認
     *
     * @param username
     * @return
     */
    boolean existsByUsername(String username);
}
