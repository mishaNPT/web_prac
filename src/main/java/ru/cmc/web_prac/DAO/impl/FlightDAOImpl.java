package ru.cmc.web_prac.DAO.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.cmc.web_prac.DAO.FlightDAO;
import ru.cmc.web_prac.classes.Flight;
import ru.cmc.web_prac.classes.Airline;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class FlightDAOImpl extends CommonDAOImpl<Flight, Long> implements FlightDAO {

    public FlightDAOImpl() {
        super(Flight.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Flight getByFlightNumber(String flightNumber) {
        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber", Flight.class);
        query.setParameter("flightNumber", flightNumber);

        List<Flight> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getByAirline(Airline airline) {
        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f WHERE f.airline = :airline ORDER BY f.departureTime", Flight.class);
        query.setParameter("airline", airline);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getByRoute(String departureAirport, String arrivalAirport) {
        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f WHERE f.departureAirport = :departure AND f.arrivalAirport = :arrival ORDER BY f.departureTime",
                Flight.class);
        query.setParameter("departure", departureAirport);
        query.setParameter("arrival", arrivalAirport);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getByDepartureDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f WHERE f.departureTime BETWEEN :startOfDay AND :endOfDay ORDER BY f.departureTime",
                Flight.class);
        query.setParameter("startOfDay", startOfDay);
        query.setParameter("endOfDay", endOfDay);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> searchFlights(String departureAirport, String arrivalAirport, LocalDateTime departureDate) {
        // Поиск рейсов в пределах дня от указанной даты
        LocalDateTime startOfDay = departureDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f WHERE f.departureAirport = :departure " +
                        "AND f.arrivalAirport = :arrival " +
                        "AND f.departureTime BETWEEN :startOfDay AND :endOfDay " +
                        "AND f.availableSeats > 0 " +
                        "ORDER BY f.departureTime",
                Flight.class);
        query.setParameter("departure", departureAirport);
        query.setParameter("arrival", arrivalAirport);
        query.setParameter("startOfDay", startOfDay);
        query.setParameter("endOfDay", endOfDay);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getAvailableFlights() {
        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f WHERE f.availableSeats > 0 ORDER BY f.departureTime", Flight.class);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getAllSorted(String sortBy) {
        String orderBy = switch (sortBy.toLowerCase()) {
            case "price" -> "ORDER BY f.price ASC";
            case "route" -> "ORDER BY f.departureAirport, f.arrivalAirport";
            case "date" -> "ORDER BY f.departureTime";
            default -> "ORDER BY f.departureTime";
        };

        TypedQuery<Flight> query = entityManager.createQuery(
                "SELECT f FROM Flight f " + orderBy, Flight.class);
        return query.getResultList();
    }

    @Override
    public void updateAvailableSeats(Long flightId, Integer newAvailableSeats) {
        Query query = entityManager.createQuery(
                "UPDATE Flight f SET f.availableSeats = :seats WHERE f.id = :flightId");
        query.setParameter("seats", newAvailableSeats);
        query.setParameter("flightId", flightId);
        query.executeUpdate();
    }

    @Override
    public void decrementAvailableSeats(Long flightId) {
        Query query = entityManager.createQuery(
                "UPDATE Flight f SET f.availableSeats = f.availableSeats - 1 WHERE f.id = :flightId AND f.availableSeats > 0");
        query.setParameter("flightId", flightId);
        query.executeUpdate();
    }

    @Override
    public void incrementAvailableSeats(Long flightId) {
        Query query = entityManager.createQuery(
                "UPDATE Flight f SET f.availableSeats = f.availableSeats + 1 WHERE f.id = :flightId");
        query.setParameter("flightId", flightId);
        query.executeUpdate();
    }
}