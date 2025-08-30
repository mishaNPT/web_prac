package ru.cmc.web_prac.DAO;

import ru.cmc.web_prac.classes.Client;
import java.util.List;

public interface ClientDAO extends CommonDAO<Client, Long> {
    Client getByEmail(String email);

    List<Client> getByNameContaining(String namepart);

    List<Client> searchByNameOrEmail(String searchTerm);

    List<Client> getByBonusMilesGreaterThan(Integer miles);

    void updateBonusMiles(Long clientId, Integer newMiles);

    void addBonusMiles(Long clientId, Integer milesToAdd);

    void deductBonusMiles(Long clientId, Integer milesToDeduct);
}