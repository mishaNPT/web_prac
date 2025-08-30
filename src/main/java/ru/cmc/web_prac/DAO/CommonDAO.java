package ru.cmc.web_prac.DAO;

import ru.cmc.web_prac.classes.CommonEntity;
import java.util.Collection;

public interface CommonDAO<T extends CommonEntity<ID>, ID> {
    T getById(ID id);

    Collection<T> getAll();

    void save(T entity);

    void saveCollection(Collection<T> entities);

    void delete(T entity);

    void update(T entity);
}