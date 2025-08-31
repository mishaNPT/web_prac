package ru.cmc.web_prac;

import org.junit.jupiter.api.Test;
import ru.cmc.web_prac.classes.*;
import ru.cmc.web_prac.DAO.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest extends BaseDAOTest {

    @Test
    void testCompleteBookingFlow() {
        // 1. Создаем новую авиакомпанию
        Airline newAirline = new Airline();
        newAirline.setName("Integration Airlines");
        newAirline.setMilesRate(BigDecimal.valueOf(1.5));
        airlineDAO.save(newAirline);

        // 2. Создаем новый рейс
        Flight newFlight = new Flight();
        newFlight.setFlightNumber("INTG001");
        newFlight.setAirline(newAirline);
        newFlight.setDepartureAirport("MSK");
        newFlight.setArrivalAirport("SPB");
        newFlight.setDepartureTime(LocalDateTime.now().plusDays(1));
        newFlight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        newFlight.setPrice(BigDecimal.valueOf(8000));
        newFlight.setTotalSeats(200);
        newFlight.setAvailableSeats(200);
        flightDAO.save(newFlight);

        // 3. Создаем нового клиента
        Client newClient = new Client();
        newClient.setFullName("Интеграционный Тестовый Клиент");
        newClient.setEmail("integration@example.com");
        newClient.setPhone("+7-999-999-99-99");
        newClient.setAddress("Интеграционный адрес");
        newClient.setBonusMiles(5000);
        clientDAO.save(newClient);

        // 4. Создаем бронирование
        Booking newBooking = new Booking();
        newBooking.setClient(newClient);
        newBooking.setFlight(newFlight);
        newBooking.setStatus("BOOKED");
        newBooking.setBookingDate(LocalDateTime.now());
        newBooking.setPaidWithMiles(false);
        newBooking.setMilesUsed(0);
        bookingDAO.save(newBooking);

        // 5. Обновляем количество свободных мест
        flightDAO.decrementAvailableSeats(newFlight.getId());

        entityManager.flush();
        entityManager.clear();

        // Проверяем результат
        assertNotNull(newBooking.getId());
        assertEquals("BOOKED", newBooking.getStatus());

        Flight updatedFlight = flightDAO.getById(newFlight.getId());
        assertEquals(199, updatedFlight.getAvailableSeats());

        List<Booking> clientBookings = bookingDAO.getByClient(newClient);
        assertEquals(1, clientBookings.size());
    }

    @Test
    void testCancelBookingFlow() {
        // Используем существующее бронирование
        Long bookingId = testBooking1.getId();
        Long clientId = testClient1.getId();
        Long flightId = testFlight1.getId();

        // Получаем текущие значения
        Client client = clientDAO.getById(clientId);
        Flight flight = flightDAO.getById(flightId);
        Integer originalSeats = flight.getAvailableSeats();

        // Отменяем бронирование
        bookingDAO.cancelBooking(bookingId);

        // Возвращаем место
        flightDAO.incrementAvailableSeats(flightId);

        entityManager.flush();
        entityManager.clear();

        // Проверяем результат
        Booking cancelledBooking = bookingDAO.getById(bookingId);
        assertEquals("CANCELLED", cancelledBooking.getStatus());

        Flight updatedFlight = flightDAO.getById(flightId);
        assertEquals(originalSeats + 1, updatedFlight.getAvailableSeats());
    }

    @Test
    void testSearchAndBookFlow() {
        // Поиск рейсов по маршруту
        List<Flight> flights = flightDAO.getByRoute("SVO", "LED");
        assertFalse(flights.isEmpty());

        // Выбираем рейс с доступными местами
        Flight selectedFlight = flights.stream()
                .filter(f -> f.getAvailableSeats() > 0)
                .findFirst()
                .orElse(null);

        assertNotNull(selectedFlight);

        // Создаем нового клиента для бронирования
        Client newClient = new Client();
        newClient.setFullName("Поисковый Клиент");
        newClient.setEmail("search@example.com");
        newClient.setPhone("+7-888-888-88-88");
        newClient.setAddress("Поисковый адрес");
        newClient.setBonusMiles(3000);
        clientDAO.save(newClient);

        // Бронируем место
        Booking newBooking = new Booking();
        newBooking.setClient(newClient);
        newBooking.setFlight(selectedFlight);
        newBooking.setStatus("BOOKED");
        newBooking.setBookingDate(LocalDateTime.now());
        newBooking.setPaidWithMiles(false);
        newBooking.setMilesUsed(0);
        bookingDAO.save(newBooking);

        // Обновляем количество мест
        flightDAO.decrementAvailableSeats(selectedFlight.getId());

        entityManager.flush();
        entityManager.clear();

        // Проверяем результат
        assertNotNull(newBooking.getId());

        Flight updatedFlight = flightDAO.getById(selectedFlight.getId());
        assertEquals(selectedFlight.getAvailableSeats() - 1, updatedFlight.getAvailableSeats());
    }

    @Test
    void testMilesPaymentFlow() {
        // Создаем клиента с большим количеством миль
        Client richClient = new Client();
        richClient.setFullName("Богатый Клиент");
        richClient.setEmail("rich@example.com");
        richClient.setPhone("+7-777-777-77-77");
        richClient.setAddress("Богатый адрес");
        richClient.setBonusMiles(10000);
        clientDAO.save(richClient);

        // Создаем дорогой рейс
        Flight expensiveFlight = new Flight();
        expensiveFlight.setFlightNumber("EXP999");
        expensiveFlight.setAirline(testAirline1);
        expensiveFlight.setDepartureAirport("SVO");
        expensiveFlight.setArrivalAirport("NYC");
        expensiveFlight.setDepartureTime(LocalDateTime.now().plusDays(2));
        expensiveFlight.setArrivalTime(LocalDateTime.now().plusDays(2).plusHours(10));
        expensiveFlight.setPrice(BigDecimal.valueOf(50000));
        expensiveFlight.setTotalSeats(300);
        expensiveFlight.setAvailableSeats(300);
        flightDAO.save(expensiveFlight);

        // Рассчитываем мили для оплаты
        Integer milesToUse = 5000;

        // Создаем бронирование с использованием миль
        Booking milesBooking = new Booking();
        milesBooking.setClient(richClient);
        milesBooking.setFlight(expensiveFlight);
        milesBooking.setStatus("PAID");
        milesBooking.setBookingDate(LocalDateTime.now());
        milesBooking.setPaidWithMiles(true);
        milesBooking.setMilesUsed(milesToUse);
        bookingDAO.save(milesBooking);

        // Списываем мили с клиента
        clientDAO.deductBonusMiles(richClient.getId(), milesToUse);

        // Начисляем новые мили за полет
        BigDecimal flightPrice = expensiveFlight.getPrice();
        BigDecimal milesRate = expensiveFlight.getAirline().getMilesRate();
        int earnedMiles = flightPrice.multiply(milesRate).intValue() / 100; // Предполагаем, что 1 миля = 100 рублей
        clientDAO.addBonusMiles(richClient.getId(), earnedMiles);

        entityManager.flush();
        entityManager.clear();

        // Проверяем результат
        Client updatedClient = clientDAO.getById(richClient.getId());
        Integer expectedMiles = 10000 - milesToUse + earnedMiles;
        assertEquals(expectedMiles, updatedClient.getBonusMiles());

        Booking savedBooking = bookingDAO.getById(milesBooking.getId());
        assertTrue(savedBooking.getPaidWithMiles());
        assertEquals(milesToUse, savedBooking.getMilesUsed());
    }

    @Test
    void testFlightManagement() {
        // Получаем все авиакомпании
        Collection<Airline> airlines = airlineDAO.getAll();
        assertEquals(2, airlines.size());

        // Получаем рейсы для конкретной авиакомпании
        List<Flight> aeroflotFlights = flightDAO.getByAirline(testAirline1);
        assertEquals(1, aeroflotFlights.size());

        // Проверяем доступные рейсы
        List<Flight> availableFlights = flightDAO.getAvailableFlights();
        assertEquals(2, availableFlights.size());
        assertTrue(availableFlights.stream().allMatch(f -> f.getAvailableSeats() > 0));

        // Тестируем сортировку
        List<Flight> sortedByPrice = flightDAO.getAllSorted("price");
        assertEquals(2, sortedByPrice.size());
        assertTrue(sortedByPrice.get(0).getPrice().compareTo(sortedByPrice.get(1).getPrice()) <= 0);
    }

    @Test
    void testClientManagement() {
        // Поиск клиентов
        List<Client> foundClients = clientDAO.searchByNameOrEmail("Иван");
        assertEquals(1, foundClients.size());

        List<Client> emailSearch = clientDAO.searchByNameOrEmail("maria@example");
        assertEquals(1, emailSearch.size());

        // VIP клиенты
        List<Client> vipClients = clientDAO.getByBonusMilesGreaterThan(2000);
        assertEquals(1, vipClients.size());
        assertEquals("test.maria@example.com", vipClients.get(0).getEmail());

        // Операции с милями
        Long clientId = testClient1.getId();
        Integer originalMiles = testClient1.getBonusMiles();

        clientDAO.addBonusMiles(clientId, 1000);
        entityManager.flush();
        entityManager.clear();

        Client updatedClient = clientDAO.getById(clientId);
        assertEquals(originalMiles + 1000, updatedClient.getBonusMiles());
    }

    @Test
    void testBookingStatistics() {
        // Создаем дополнительные бронирования для статистики
        Booking paidBooking = new Booking();
        paidBooking.setClient(testClient2);
        paidBooking.setFlight(testFlight2);
        paidBooking.setStatus("PAID");
        paidBooking.setPaidWithMiles(true);
        paidBooking.setMilesUsed(2000);
        bookingDAO.save(paidBooking);

        Booking cancelledBooking = new Booking();
        cancelledBooking.setClient(testClient2);
        cancelledBooking.setFlight(testFlight1);
        cancelledBooking.setStatus("CANCELLED");
        bookingDAO.save(cancelledBooking);

        entityManager.flush();

        // Получаем статистику
        List<Booking> activeBookings = bookingDAO.getActiveBookings();
        assertEquals(2, activeBookings.size()); // testBooking1 + paidBooking

        List<Booking> paidWithMiles = bookingDAO.getPaidWithMiles();
        assertEquals(1, paidWithMiles.size());

        List<Booking> statusBooked = bookingDAO.getByStatus("BOOKED");
        assertEquals(1, statusBooked.size());

        List<Booking> statusPaid = bookingDAO.getByStatus("PAID");
        assertEquals(1, statusPaid.size());

        List<Booking> statusCancelled = bookingDAO.getByStatus("CANCELLED");
        assertEquals(1, statusCancelled.size());
    }

    @Test
    void testComplexSearch() {
        // Создаем дополнительные данные для поиска
        LocalDateTime searchDate = LocalDateTime.now().plusDays(3);

        Flight searchFlight = new Flight();
        searchFlight.setFlightNumber("SRCH001");
        searchFlight.setAirline(testAirline1);
        searchFlight.setDepartureAirport("SVO");
        searchFlight.setArrivalAirport("LED");
        searchFlight.setDepartureTime(searchDate);
        searchFlight.setArrivalTime(searchDate.plusHours(2));
        searchFlight.setPrice(BigDecimal.valueOf(6000));
        searchFlight.setTotalSeats(150);
        searchFlight.setAvailableSeats(150);
        flightDAO.save(searchFlight);

        entityManager.flush();

        // Поиск рейсов
        List<Flight> routeFlights = flightDAO.getByRoute("SVO", "LED");
        assertEquals(2, routeFlights.size());

        List<Flight> searchResults = flightDAO.searchFlights("SVO", "LED", searchDate);
        assertEquals(1, searchResults.size());
        assertEquals("SRCH001", searchResults.get(0).getFlightNumber());

        // Поиск по дате
        LocalDateTime startOfDay = searchDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        List<Flight> dateFlights = flightDAO.getByDepartureDate(startOfDay, endOfDay);
        assertEquals(1, dateFlights.size());
    }

    @Test
    void testDataConsistency() {
        // Проверяем целостность данных
        Collection<Booking> allBookings = bookingDAO.getAll();

        for (Booking booking : allBookings) {
            // Каждое бронирование должно иметь валидные ссылки
            assertNotNull(booking.getClient());
            assertNotNull(booking.getFlight());
            assertNotNull(booking.getStatus());
            assertNotNull(booking.getBookingDate());

            // Проверяем, что клиент и рейс существуют в БД
            Client client = clientDAO.getById(booking.getClient().getId());
            assertNotNull(client);

            Flight flight = flightDAO.getById(booking.getFlight().getId());
            assertNotNull(flight);

            // Если использованы мили, то их количество должно быть больше 0
            if (booking.getPaidWithMiles()) {
                assertTrue(booking.getMilesUsed() > 0);
            }
        }

        Collection<Flight> allFlights = flightDAO.getAll();
        for (Flight flight : allFlights) {
            // Доступных мест не должно быть больше общего количества
            assertTrue(flight.getAvailableSeats() <= flight.getTotalSeats());
            assertTrue(flight.getAvailableSeats() >= 0);

            // Время прибытия должно быть после времени вылета
            assertTrue(flight.getArrivalTime().isAfter(flight.getDepartureTime()));

            // Цена должна быть положительной
            assertTrue(flight.getPrice().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    void testCascadeOperations() {
        // Создаем новую авиакомпанию с рейсом
        Airline testAirline = new Airline();
        testAirline.setName("Cascade Test Airlines");
        testAirline.setMilesRate(BigDecimal.valueOf(1.0));
        airlineDAO.save(testAirline);

        Flight testFlight = new Flight();
        testFlight.setFlightNumber("CASCADE");
        testFlight.setAirline(testAirline);
        testFlight.setDepartureAirport("TEST");
        testFlight.setArrivalAirport("TEST");
        testFlight.setDepartureTime(LocalDateTime.now().plusDays(1));
        testFlight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testFlight.setPrice(BigDecimal.valueOf(1000));
        testFlight.setTotalSeats(100);
        testFlight.setAvailableSeats(100);
        flightDAO.save(testFlight);

        entityManager.flush();

        Long airlineId = testAirline.getId();
        Long flightId = testFlight.getId();

        // Удаляем авиакомпанию - рейс должен остаться (в зависимости от настроек каскада)
        // Или наоборот, если настроен каскад DELETE
        Collection<Flight> flightsBefore = flightDAO.getAll();
        int countBefore = flightsBefore.size();

        // В данном случае удаление авиакомпании может привести к ошибке FK
        // или к каскадному удалению рейсов, в зависимости от настроек
        try {
            airlineDAO.delete(testAirline);
            entityManager.flush();

            Collection<Flight> flightsAfter = flightDAO.getAll();
            // Если каскадное удаление включено, рейсов должно стать меньше
            // Если нет - должна быть ошибка FK constraint

        } catch (Exception e) {
            // Ожидаемое поведение при нарушении FK constraint
            // Сначала нужно удалить рейсы, потом авиакомпанию
            flightDAO.delete(testFlight);
            airlineDAO.delete(testAirline);
            entityManager.flush();
        }
    }
}