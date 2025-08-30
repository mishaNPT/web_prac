package ru.cmc.web_prac.DAO.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.cmc.web_prac.DAO.AirlineDAO;
import ru.cmc.web_prac.classes.Airline;

import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class AirlineDAOImpl extends CommonDAOImpl<Airline, Long> implements AirlineDAO {

    public AirlineDAOImpl() {
        super(Airline.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Airline getByName(String name) {
        TypedQuery<Airline> query = entityManager.createQuery(
                "SELECT a FROM Airline a WHERE a.name = :name", Airline.class);
        query.setParameter("name", name);

        List<Airline> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Airline> getAllSortedByName() {
        TypedQuery<Airline> query = entityManager.createQuery(
                "SELECT a FROM Airline a ORDER BY a.name ASC", Airline.class);
        return query.getResultList();
    }
}