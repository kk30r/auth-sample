package kk.sample.auth_server.auth.repository;

import kk.sample.auth_server.auth.entity.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Kensuke.Ito
 */
@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {

    UserInfo findByUsersId(Long usersId);

}
