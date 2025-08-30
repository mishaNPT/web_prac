package ru.cmc.web_prac.DAO.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.cmc.web_prac.DAO.ClientDAO;
import ru.cmc.web_prac.classes.Client;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class ClientDAOImpl extends CommonDAOImpl<Client, Long> implements ClientDAO {

    public ClientDAOImpl() {
        super(Client.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Client getByEmail(String email) {
        TypedQuery<Client> query = entityManager.createQuery(
                "SELECT c FROM Client c WHERE c.email = :email", Client.class);
        query.setParameter("email", email);

        List<Client> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getByNameContaining(String namepart) {
        TypedQuery<Client> query = entityManager.createQuery(
                "SELECT c FROM Client c WHERE LOWER(c.fullName) LIKE LOWER(:namepart)", Client.class);
        query.setParameter("namepart", "%" + namepart + "%");
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> searchByNameOrEmail(String searchTerm) {
        TypedQuery<Client> query = entityManager.createQuery(
                "SELECT c FROM Client c WHERE LOWER(c.fullName) LIKE LOWER(:searchTerm) " +
                        "OR LOWER(c.email) LIKE LOWER(:searchTerm) ORDER BY c.fullName", Client.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getByBonusMilesGreaterThan(Integer miles) {
        TypedQuery<Client> query = entityManager.createQuery(
                "SELECT c FROM Client c WHERE c.bonusMiles > :miles ORDER BY c.bonusMiles DESC", Client.class);
        query.setParameter("miles", miles);
        return query.getResultList();
    }

    @Override
    public void updateBonusMiles(Long clientId, Integer newMiles) {
        Query query = entityManager.createQuery(
                "UPDATE Client c SET c.bonusMiles = :newMiles WHERE c.id = :clientId");
        query.setParameter("newMiles", newMiles);
        query.setParameter("clientId", clientId);
        query.executeUpdate();
    }

    @Override
    public void addBonusMiles(Long clientId, Integer milesToAdd) {
        Query query = entityManager.createQuery(
                "UPDATE Client c SET c.bonusMiles = c.bonusMiles + :milesToAdd WHERE c.id = :clientId");
        query.setParameter("milesToAdd", milesToAdd);
        query.setParameter("clientId", clientId);
        query.executeUpdate();
    }

    @Override
    public void deductBonusMiles(Long clientId, Integer milesToDeduct) {
        Query query = entityManager.createQuery(
                "UPDATE Client c SET c.bonusMiles = c.bonusMiles - :milesToDeduct " +
                        "WHERE c.id = :clientId AND c.bonusMiles >= :milesToDeduct");
        query.setParameter("milesToDeduct", milesToDeduct);
        query.setParameter("clientId", clientId);
        query.executeUpdate();
    }
}