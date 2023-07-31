package kk.sample.auth_server.common.service.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kensuke.Ito
 */
@Service
@lombok.extern.slf4j.Slf4j
public class JpaService {

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * イテレーション処理
     *
     * @param <E>
     * @param entityClaz
     * @param criteriaBuilderCallback
     * @param entityCallback
     */
    public <E> void iteration(final Class<E> entityClaz,
                              final BiConsumer<CriteriaBuilder, CriteriaQuery<E>> criteriaBuilderCallback,
                              final Consumer<E> entityCallback) {
        iteration(entityClaz,
                  criteriaBuilderCallback,
                  entityCallback,
                  1000);
    }

    /**
     * イテレーション処理
     *
     * @param <E>
     * @param entityClaz
     * @param criteriaBuilderCallback
     * @param entityCallback
     * @param clearThreshold
     */
    public <E> void iteration(final Class<E> entityClaz,
                              final BiConsumer<CriteriaBuilder, CriteriaQuery<E>> criteriaBuilderCallback,
                              final Consumer<E> entityCallback,
                              int clearThreshold) {
        if (clearThreshold < 1) {
            clearThreshold = 1000;
        }

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<E> criteriaQuery = builder.createQuery(entityClaz);
        criteriaBuilderCallback.accept(builder, criteriaQuery);
        final Query query = entityManager.createQuery(criteriaQuery);

        org.hibernate.query.Query hibernateQuery = query.unwrap(
                org.hibernate.query.Query.class);
        hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);

        int count = 0;
        try (ScrollableResults<E> results = hibernateQuery.scroll()) {
            while (results.next()) {
                count++;
                final E entity = results.get();
                if (log.isDebugEnabled()) {
                    log.debug("ProductEntity: " + entity);
                }
                entityCallback.accept(entity);
                if (count % clearThreshold == 0) {
                    entityManager.clear();
                }
            }
            entityManager.clear();
        }

    }
}
