package kk.sample.auth_server.auth.repository;

import java.util.List;
import kk.sample.auth_server.auth.entity.Authorities;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Kensuke.Ito
 */
@Repository
public interface AuthoritiesRepository extends CrudRepository<Authorities, Authorities.PK> {

    List<Authorities> findByPk_UsersId(Long usersId);

}
