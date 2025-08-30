package ru.cmc.web_prac.DAO.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.cmc.web_prac.DAO.CommonDAO;
import ru.cmc.web_prac.classes.CommonEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public abstract class CommonDAOImpl<T extends CommonEntity<ID>, ID extends Serializable>
        implements CommonDAO<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected Class<T> persistentClass;

    public CommonDAOImpl(Class<T> entityClass) {
        this.persistentClass = entityClass;
    }

    @Override
    @Transactional(readOnly = true)
    public T getById(ID id) {
        return entityManager.find(persistentClass, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<T> getAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(persistentClass);
        criteriaQuery.from(persistentClass);
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public void save(T entity) {
        entityManager.persist(entity);
    }

    @Override
    public void saveCollection(Collection<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    @Override
    public void delete(T entity) {
        T managedEntity = entityManager.find(persistentClass, entity.getId());
        if (managedEntity != null) {
            entityManager.remove(managedEntity);
        }
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }
}