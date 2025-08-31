package ru.cmc.web_prac.DAO;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.cmc.web_prac.DAO.*;
import ru.cmc.web_prac.classes.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
public abstract class BaseDAOTest {

    @Autowired
    protected AirlineDAO airlineDAO;

    @Autowired
    protected FlightDAO flightDAO;

    @Autowired
    protected ClientDAO clientDAO;

    @Autowired
    protected BookingDAO bookingDAO;

    @PersistenceContext
    protected EntityManager entityManager;

    protected Airline testAirline1;
    protected Airline testAirline2;
    protected Flight testFlight1;
    protected Flight testFlight2;
    protected Client testClient1;
    protected Client testClient2;
    protected Booking testBooking1;

    @BeforeEach
    void setUp() {
        populateTestData();
    }

    // Убираем ручную очистку - пусть create-drop сам управляет

    private void populateTestData() {
        // Создаем тестовые авиакомпании
        testAirline1 = new Airline();
        testAirline1.setName("Test Aeroflot");
        testAirline1.setMilesRate(BigDecimal.valueOf(1.0));
        airlineDAO.save(testAirline1);

        testAirline2 = new Airline();
        testAirline2.setName("Test S7");
        testAirline2.setMilesRate(BigDecimal.valueOf(1.2));
        airlineDAO.save(testAirline2);

        // Создаем тестовые рейсы
        testFlight1 = new Flight();
        testFlight1.setFlightNumber("TEST123");
        testFlight1.setAirline(testAirline1);
        testFlight1.setDepartureAirport("SVO");
        testFlight1.setArrivalAirport("LED");
        testFlight1.setDepartureTime(LocalDateTime.of(2024, 12, 15, 10, 30));
        testFlight1.setArrivalTime(LocalDateTime.of(2024, 12, 15, 12, 0));
        testFlight1.setPrice(BigDecimal.valueOf(5000));
        testFlight1.setTotalSeats(150);
        testFlight1.setAvailableSeats(150);
        flightDAO.save(testFlight1);

        testFlight2 = new Flight();
        testFlight2.setFlightNumber("TEST456");
        testFlight2.setAirline(testAirline2);
        testFlight2.setDepartureAirport("LED");
        testFlight2.setArrivalAirport("SVO");
        testFlight2.setDepartureTime(LocalDateTime.of(2024, 12, 16, 14, 15));
        testFlight2.setArrivalTime(LocalDateTime.of(2024, 12, 16, 15, 45));
        testFlight2.setPrice(BigDecimal.valueOf(4500));
        testFlight2.setTotalSeats(120);
        testFlight2.setAvailableSeats(120);
        flightDAO.save(testFlight2);

        // Создаем тестовых клиентов
        testClient1 = new Client();
        testClient1.setFullName("Тестовый Иван Иванович");
        testClient1.setEmail("test.ivan@example.com");
        testClient1.setPhone("+7-900-123-45-67");
        testClient1.setAddress("Москва, Тестовая ул., д. 1");
        testClient1.setBonusMiles(1000);
        clientDAO.save(testClient1);

        testClient2 = new Client();
        testClient2.setFullName("Тестовая Мария Петровна");
        testClient2.setEmail("test.maria@example.com");
        testClient2.setPhone("+7-900-987-65-43");
        testClient2.setAddress("СПб, Тестовая наб., д. 10");
        testClient2.setBonusMiles(2500);
        clientDAO.save(testClient2);

        // Создаем тестовое бронирование
        testBooking1 = new Booking();
        testBooking1.setClient(testClient1);
        testBooking1.setFlight(testFlight1);
        testBooking1.setBookingDate(LocalDateTime.now());
        testBooking1.setStatus("BOOKED");
        testBooking1.setPaidWithMiles(false);
        testBooking1.setMilesUsed(0);
        bookingDAO.save(testBooking1);

        entityManager.flush();
    }
}