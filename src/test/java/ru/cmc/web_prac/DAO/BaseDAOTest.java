package ru.cmc.web_prac.DAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public abstract class BaseDAOTest {

    @PersistenceContext
    protected EntityManager entityManager;

    @BeforeEach
    void initializeTestData() {
        clearDatabase();
        fillTestData();
    }

    private void clearDatabase() {
        entityManager.createNativeQuery("DELETE FROM bookings").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM flights").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM clients").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM airlines").executeUpdate();

        // Сброс последовательностей
        entityManager.createNativeQuery("ALTER SEQUENCE airlines_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE clients_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE flights_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE bookings_id_seq RESTART WITH 1").executeUpdate();

        entityManager.flush();
    }

    /**
     * Заполнение БД тестовыми данными
     */
    private void fillTestData() {
        // Заполнение авиакомпаний
        entityManager.createNativeQuery(
                "INSERT INTO airlines (name, miles_rate) VALUES " +
                        "('Aeroflot', 1.0), ('S7 Airlines', 1.2), ('Ural Airlines', 0.9), " +
                        "('Pobeda', 0.8), ('Nordwind', 1.1)").executeUpdate();

        // Заполнение клиентов
        entityManager.createNativeQuery(
                "INSERT INTO clients (full_name, email, phone, address, bonus_miles) VALUES " +
                        "('Иванов Иван Иванович', 'ivanov@email.com', '+7-903-123-4567', 'Москва, ул. Ленина, 10', 15000), " +
                        "('Петрова Анна Сергеевна', 'petrova@email.com', '+7-905-234-5678', 'СПб, Невский пр., 25', 8500), " +
                        "('Сидоров Петр Николаевич', 'sidorov@email.com', '+7-916-345-6789', 'Екатеринбург, ул. Мира, 5', 2300), " +
                        "('Козлова Мария Александровна', 'kozlova@email.com', '+7-921-456-7890', 'Новосибирск, пр. Ленина, 15', 0), " +
                        "('Смирнов Алексей Викторович', 'smirnov@email.com', '+7-812-567-8901', 'Казань, ул. Баумана, 30', 12000)").executeUpdate();

        // Заполнение рейсов
        entityManager.createNativeQuery(
                "INSERT INTO flights (flight_number, airline_id, departure_airport, arrival_airport, departure_time, arrival_time, price, total_seats, available_seats) VALUES " +
                        "('SU123', 1, 'SVO', 'LED', '2025-08-20 10:00:00', '2025-08-20 11:30:00', 8500.00, 180, 45), " +
                        "('S7456', 2, 'DME', 'KZN', '2025-08-20 14:30:00', '2025-08-20 16:00:00', 6200.00, 150, 78), " +
                        "('U6789', 3, 'VKO', 'SVX', '2025-08-21 08:15:00', '2025-08-21 11:45:00', 12400.00, 200, 120), " +
                        "('DP234', 4, 'SVO', 'ROV', '2025-08-21 16:20:00', '2025-08-21 18:30:00', 5800.00, 189, 156), " +
                        "('N4567', 5, 'LED', 'SVO', '2025-08-22 12:45:00', '2025-08-22 14:15:00', 7900.00, 160, 89), " +
                        "('SU890', 1, 'SVO', 'OVB', '2025-08-22 20:00:00', '2025-08-23 02:30:00', 18500.00, 220, 67), " +
                        "('S7321', 2, 'KZN', 'DME', '2025-08-23 09:30:00', '2025-08-23 11:00:00', 6100.00, 150, 92)").executeUpdate();

        // Заполнение бронирований
        entityManager.createNativeQuery(
                "INSERT INTO bookings (client_id, flight_id, status, paid_with_miles, miles_used) VALUES " +
                        "(1, 1, 'PAID', FALSE, 0), (1, 5, 'BOOKED', FALSE, 0), " +
                        "(2, 2, 'PAID', TRUE, 6200), (3, 3, 'PAID', FALSE, 0), " +
                        "(4, 4, 'BOOKED', FALSE, 0), (5, 6, 'PAID', TRUE, 12000), " +
                        "(2, 7, 'CANCELLED', FALSE, 0), (1, 3, 'BOOKED', FALSE, 0)").executeUpdate();

        // Финальные обновления
        entityManager.createNativeQuery(
                "UPDATE flights SET available_seats = available_seats - 1 WHERE id IN (1, 2, 3, 4, 5, 6)").executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE flights SET available_seats = available_seats + 1 WHERE id = 7").executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE clients SET bonus_miles = 8300 WHERE id = 2").executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE clients SET bonus_miles = 0 WHERE id = 5").executeUpdate();

        entityManager.flush();
    }
}