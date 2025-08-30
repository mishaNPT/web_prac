package ru.cmc.web_prac.DAO;

import ru.cmc.web_prac.classes.Flight;
import ru.cmc.web_prac.classes.Airline;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightDAO extends CommonDAO<Flight, Long> {
    Flight getByFlightNumber(String flightNumber);

    List<Flight> getByAirline(Airline airline);

    List<Flight> getByRoute(String departureAirport, String arrivalAirport);

    List<Flight> getByDepartureDate(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Flight> searchFlights(String departureAirport, String arrivalAirport, LocalDateTime departureDate);

    List<Flight> getAvailableFlights();

    List<Flight> getAllSorted(String sortBy);

    void updateAvailableSeats(Long flightId, Integer newAvailableSeats);

    void decrementAvailableSeats(Long flightId);

    void incrementAvailableSeats(Long flightId);
}