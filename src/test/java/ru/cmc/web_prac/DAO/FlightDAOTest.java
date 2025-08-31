package ru.cmc.web_prac.DAO;

import org.junit.jupiter.api.Test;
import ru.cmc.web_prac.classes.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlightDAOTest extends BaseDAOTest {
    @Test
    void testSave() {
        Flight flight = new Flight();
        flight.setFlightNumber("TEST999");
        flight.setAirline(testAirline1);
        flight.setDepartureAirport("MSK");
        flight.setArrivalAirport("SPB");
        flight.setDepartureTime(LocalDateTime.of(2024, 12, 20, 8, 0));
        flight.setArrivalTime(LocalDateTime.of(2024, 12, 20, 9, 30));
        flight.setPrice(BigDecimal.valueOf(3500));
        flight.setTotalSeats(100);
        flight.setAvailableSeats(100);

        flightDAO.save(flight);
        entityManager.flush();

        assertNotNull(flight.getId());
        assertTrue(flight.getId() > 0);
    }

    @Test
    void testGetById() {
        Flight foundFlight = flightDAO.getById(testFlight1.getId());

        assertNotNull(foundFlight);
        assertEquals(testFlight1.getId(), foundFlight.getId());
        assertEquals("TEST123", foundFlight.getFlightNumber());
        assertEquals("SVO", foundFlight.getDepartureAirport());
        assertEquals("LED", foundFlight.getArrivalAirport());
        assertEquals(BigDecimal.valueOf(5000), foundFlight.getPrice());
    }

    @Test
    void testGetByIdNonExistent() {
        Flight foundFlight = flightDAO.getById(999L);
        assertNull(foundFlight);
    }

    @Test
    void testGetAll() {
        Collection<Flight> allFlights = flightDAO.getAll();

        assertNotNull(allFlights);
        assertEquals(2, allFlights.size());

        boolean found1 = allFlights.stream().anyMatch(f -> f.getFlightNumber().equals("TEST123"));
        boolean found2 = allFlights.stream().anyMatch(f -> f.getFlightNumber().equals("TEST456"));
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    void testUpdate() {
        testFlight1.setPrice(BigDecimal.valueOf(5500));
        testFlight1.setAvailableSeats(140);

        flightDAO.update(testFlight1);
        entityManager.flush();
        entityManager.clear();

        Flight updatedFlight = flightDAO.getById(testFlight1.getId());
        assertEquals(5500.00, updatedFlight.getPrice().doubleValue());
        assertEquals(140, updatedFlight.getAvailableSeats());
    }

    @Test
    void testDelete() {
        Long flightId = testFlight1.getId();

        // Сначала удаляем связанные бронирования
        bookingDAO.delete(testBooking1);

        // Теперь можем удалить рейс
        flightDAO.delete(testFlight1);

        Flight deletedFlight = flightDAO.getById(flightId);
        assertNull(deletedFlight);

        Collection<Flight> allFlights = flightDAO.getAll();
        assertEquals(1, allFlights.size());
        assertEquals("TEST456", allFlights.iterator().next().getFlightNumber());
    }

    @Test
    void testGetByFlightNumber() {
        Flight foundFlight = flightDAO.getByFlightNumber("TEST123");

        assertNotNull(foundFlight);
        assertEquals("TEST123", foundFlight.getFlightNumber());
        assertEquals(testFlight1.getId(), foundFlight.getId());
    }

    @Test
    void testGetByFlightNumberNotFound() {
        Flight foundFlight = flightDAO.getByFlightNumber("NOTFOUND");
        assertNull(foundFlight);
    }

    @Test
    void testGetByAirline() {
        List<Flight> flights = flightDAO.getByAirline(testAirline1);

        assertEquals(1, flights.size());
        assertEquals("TEST123", flights.get(0).getFlightNumber());
        assertEquals(testAirline1.getId(), flights.get(0).getAirline().getId());
    }

    @Test
    void testGetByAirlineEmpty() {
        Airline emptyAirline = new Airline();
        emptyAirline.setName("Empty Airlines");
        emptyAirline.setMilesRate(BigDecimal.valueOf(1.0));
        airlineDAO.save(emptyAirline);

        List<Flight> flights = flightDAO.getByAirline(emptyAirline);
        assertTrue(flights.isEmpty());
    }

    @Test
    void testGetByRoute() {
        List<Flight> flights = flightDAO.getByRoute("SVO", "LED");

        assertEquals(1, flights.size());
        assertEquals("TEST123", flights.get(0).getFlightNumber());
    }

    @Test
    void testGetByRouteNotFound() {
        List<Flight> flights = flightDAO.getByRoute("ABC", "XYZ");
        assertTrue(flights.isEmpty());
    }

    @Test
    void testGetByDepartureDate() {
        LocalDateTime startOfDay = LocalDateTime.of(2024, 12, 15, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 12, 15, 23, 59, 59);

        List<Flight> flights = flightDAO.getByDepartureDate(startOfDay, endOfDay);

        assertEquals(1, flights.size());
        assertEquals("TEST123", flights.get(0).getFlightNumber());
    }

    @Test
    void testGetByDepartureDateEmpty() {
        LocalDateTime startOfDay = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2025, 1, 1, 23, 59, 59);

        List<Flight> flights = flightDAO.getByDepartureDate(startOfDay, endOfDay);
        assertTrue(flights.isEmpty());
    }

    @Test
    void testSearchFlights() {
        LocalDateTime searchDate = LocalDateTime.of(2024, 12, 15, 0, 0);

        List<Flight> flights = flightDAO.searchFlights("SVO", "LED", searchDate);

        assertEquals(1, flights.size());
        assertEquals("TEST123", flights.get(0).getFlightNumber());
        assertTrue(flights.get(0).getAvailableSeats() > 0);
    }

    @Test
    void testSearchFlightsNoAvailableSeats() {
        // Устанавливаем 0 доступных мест
        testFlight1.setAvailableSeats(0);
        flightDAO.update(testFlight1);
        entityManager.flush();

        LocalDateTime searchDate = LocalDateTime.of(2024, 12, 15, 0, 0);
        List<Flight> flights = flightDAO.searchFlights("SVO", "LED", searchDate);

        assertTrue(flights.isEmpty());
    }

    @Test
    void testGetAvailableFlights() {
        List<Flight> availableFlights = flightDAO.getAvailableFlights();

        assertEquals(2, availableFlights.size());
        assertTrue(availableFlights.stream().allMatch(f -> f.getAvailableSeats() > 0));
    }

    @Test
    void testGetAvailableFlightsWithNoSeats() {
        // Убираем все доступные места
        testFlight1.setAvailableSeats(0);
        testFlight2.setAvailableSeats(0);
        flightDAO.update(testFlight1);
        flightDAO.update(testFlight2);
        entityManager.flush();

        List<Flight> availableFlights = flightDAO.getAvailableFlights();
        assertTrue(availableFlights.isEmpty());
    }

    @Test
    void testGetAllSorted() {
        // Тест сортировки по времени вылета (по умолчанию)
        List<Flight> sortedByDate = flightDAO.getAllSorted("date");

        assertEquals(2, sortedByDate.size());
        assertTrue(sortedByDate.get(0).getDepartureTime().isBefore(sortedByDate.get(1).getDepartureTime()));
        assertEquals("TEST123", sortedByDate.get(0).getFlightNumber());
        assertEquals("TEST456", sortedByDate.get(1).getFlightNumber());
    }

    @Test
    void testGetAllSortedByPrice() {
        List<Flight> sortedByPrice = flightDAO.getAllSorted("price");

        assertEquals(2, sortedByPrice.size());
        assertTrue(sortedByPrice.get(0).getPrice().compareTo(sortedByPrice.get(1).getPrice()) <= 0);
        assertEquals("TEST456", sortedByPrice.get(0).getFlightNumber()); // 4500
        assertEquals("TEST123", sortedByPrice.get(1).getFlightNumber()); // 5000
    }

    @Test
    void testGetAllSortedByRoute() {
        List<Flight> sortedByRoute = flightDAO.getAllSorted("route");

        assertEquals(2, sortedByRoute.size());
        // Проверяем что сортировка работает
        assertNotNull(sortedByRoute.get(0).getDepartureAirport());
        assertNotNull(sortedByRoute.get(1).getDepartureAirport());
    }

    @Test
    void testUpdateAvailableSeats() {
        Long flightId = testFlight1.getId();
        Integer newSeats = 100;

        flightDAO.updateAvailableSeats(flightId, newSeats);
        entityManager.flush();
        entityManager.clear();

        Flight updatedFlight = flightDAO.getById(flightId);
        assertEquals(newSeats, updatedFlight.getAvailableSeats());
    }

    @Test
    void testDecrementAvailableSeats() {
        Long flightId = testFlight1.getId();
        Integer originalSeats = testFlight1.getAvailableSeats();

        flightDAO.decrementAvailableSeats(flightId);
        entityManager.flush();
        entityManager.clear();

        Flight updatedFlight = flightDAO.getById(flightId);
        assertEquals(originalSeats - 1, updatedFlight.getAvailableSeats());
    }

    @Test
    void testDecrementAvailableSeatsWhenZero() {
        // Устанавливаем 0 мест
        testFlight1.setAvailableSeats(0);
        flightDAO.update(testFlight1);
        entityManager.flush();

        Long flightId = testFlight1.getId();
        flightDAO.decrementAvailableSeats(flightId);
        entityManager.flush();
        entityManager.clear();

        Flight updatedFlight = flightDAO.getById(flightId);
        assertEquals(0, updatedFlight.getAvailableSeats()); // Не должно стать отрицательным
    }

    @Test
    void testIncrementAvailableSeats() {
        Long flightId = testFlight1.getId();
        Integer originalSeats = testFlight1.getAvailableSeats();

        flightDAO.incrementAvailableSeats(flightId);
        entityManager.flush();
        entityManager.clear();

        Flight updatedFlight = flightDAO.getById(flightId);
        assertEquals(originalSeats + 1, updatedFlight.getAvailableSeats());
    }

    @Test
    void testSaveCollection() {
        Flight flight1 = new Flight();
        flight1.setFlightNumber("COL001");
        flight1.setAirline(testAirline1);
        flight1.setDepartureAirport("MSK");
        flight1.setArrivalAirport("EKB");
        flight1.setDepartureTime(LocalDateTime.of(2024, 12, 25, 12, 0));
        flight1.setArrivalTime(LocalDateTime.of(2024, 12, 25, 14, 30));
        flight1.setPrice(BigDecimal.valueOf(6000));
        flight1.setTotalSeats(180);
        flight1.setAvailableSeats(180);

        Flight flight2 = new Flight();
        flight2.setFlightNumber("COL002");
        flight2.setAirline(testAirline2);
        flight2.setDepartureAirport("EKB");
        flight2.setArrivalAirport("MSK");
        flight2.setDepartureTime(LocalDateTime.of(2024, 12, 26, 16, 0));
        flight2.setArrivalTime(LocalDateTime.of(2024, 12, 26, 18, 30));
        flight2.setPrice(BigDecimal.valueOf(5800));
        flight2.setTotalSeats(180);
        flight2.setAvailableSeats(180);

        List<Flight> flightsToSave = List.of(flight1, flight2);
        flightDAO.saveCollection(flightsToSave);
        entityManager.flush();

        assertNotNull(flight1.getId());
        assertNotNull(flight2.getId());

        Collection<Flight> allFlights = flightDAO.getAll();
        assertEquals(4, allFlights.size());
    }
}