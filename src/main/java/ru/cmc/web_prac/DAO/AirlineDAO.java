package ru.cmc.web_prac.DAO;

import ru.cmc.web_prac.classes.Airline;

import java.util.List;

public interface AirlineDAO extends CommonDAO<Airline, Long> {

    Airline getByName(String name);

    List<Airline> getAllSortedByName();
}