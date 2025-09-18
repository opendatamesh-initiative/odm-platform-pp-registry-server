package org.opendatamesh.platform.pp.registry.utils.services;

import org.hibernate.Hibernate;
import org.opendatamesh.platform.pp.registry.exceptions.InternalException;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.Collection;

@Component
public class EntityInitAndDetachService {

    @PersistenceContext
    private EntityManager entityManager;

    public void initializeEntityAndDetach(Object entity) {
        initializeEntity(entity);
        entityManager.detach(entity);
    }

    private void initializeEntity(Object entity) {
        if (entity == null) return;
        Hibernate.initialize(entity);
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Collection<?> collection = (Collection<?>) field.get(entity);
                    if (collection != null) {
                        collection.forEach(this::initializeEntity);
                    }
                } catch (IllegalAccessException e) {
                    throw new InternalException(e.getMessage(), e);
                }
            }
        }
    }
}
