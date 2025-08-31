package ru.cmc.web_prac.DAO;

import org.junit.jupiter.api.Test;
import ru.cmc.web_prac.classes.Airline;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AirlineDAOTest extends BaseDAOTest {
    @Test
    void testSave() {
        Airline airline = new Airline();
        airline.setName("Test New Airlines");
        airline.setMilesRate(BigDecimal.valueOf(1.5));

        airlineDAO.save(airline);
        entityManager.flush();

        assertNotNull(airline.getId());
        assertTrue(airline.getId() > 0);
    }

    @Test
    void testGetById() {
        Airline foundAirline = airlineDAO.getById(testAirline1.getId());

        assertNotNull(foundAirline);
        assertEquals(testAirline1.getId(), foundAirline.getId());
        assertEquals("Test Aeroflot", foundAirline.getName());
        assertEquals(BigDecimal.valueOf(1.0), foundAirline.getMilesRate());
    }

    @Test
    void testGetByIdNonExistent() {
        Airline foundAirline = airlineDAO.getById(999L);
        assertNull(foundAirline);
    }

    @Test
    void testGetAll() {
        Collection<Airline> allAirlines = airlineDAO.getAll();

        assertNotNull(allAirlines);
        assertEquals(2, allAirlines.size());

        boolean found1 = allAirlines.stream().anyMatch(a -> a.getName().equals("Test Aeroflot"));
        boolean found2 = allAirlines.stream().anyMatch(a -> a.getName().equals("Test S7"));
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    void testUpdate() {
        testAirline1.setName("Test Updated Aeroflot");
        testAirline1.setMilesRate(BigDecimal.valueOf(1.1));

        airlineDAO.update(testAirline1);
        entityManager.flush();
        entityManager.clear(); // Очищаем кэш для проверки обновления в БД

        Airline updatedAirline = airlineDAO.getById(testAirline1.getId());
        assertEquals("Test Updated Aeroflot", updatedAirline.getName());
        assertEquals(1.1, updatedAirline.getMilesRate().doubleValue());
    }

    @Test
    void testDelete() {
        Long airlineId = testAirline1.getId();

        // Сначала удаляем связанные рейсы и бронирования
        bookingDAO.delete(testBooking1);
        flightDAO.delete(testFlight1);

        // Теперь можем удалить авиакомпанию
        airlineDAO.delete(testAirline1);

        Airline deletedAirline = airlineDAO.getById(airlineId);
        assertNull(deletedAirline);

        Collection<Airline> allAirlines = airlineDAO.getAll();
        assertEquals(1, allAirlines.size());
        assertEquals("Test S7", allAirlines.iterator().next().getName());
    }

    @Test
    void testGetByName() {
        Airline foundAirline = airlineDAO.getByName("Test Aeroflot");

        assertNotNull(foundAirline);
        assertEquals("Test Aeroflot", foundAirline.getName());
        assertEquals(testAirline1.getId(), foundAirline.getId());
    }

    @Test
    void testGetByNameNotFound() {
        Airline foundAirline = airlineDAO.getByName("NonExistent Airlines");
        assertNull(foundAirline);
    }

    @Test
    void testGetByNameCaseInsensitive() {
        Airline foundAirline = airlineDAO.getByName("test aeroflot");
        // Зависит от реализации - в текущей реализации чувствителен к регистру
        assertNull(foundAirline);
    }

    @Test
    void testGetAllSortedByName() {
        List<Airline> sortedAirlines = airlineDAO.getAllSortedByName();

        assertNotNull(sortedAirlines);
        assertEquals(2, sortedAirlines.size());

        // Проверяем сортировку по алфавиту
        assertEquals("Test Aeroflot", sortedAirlines.get(0).getName());
        assertEquals("Test S7", sortedAirlines.get(1).getName());
    }

    @Test
    void testSaveCollection() {
        Airline airline1 = new Airline();
        airline1.setName("Test Collection 1");
        airline1.setMilesRate(BigDecimal.valueOf(0.8));

        Airline airline2 = new Airline();
        airline2.setName("Test Collection 2");
        airline2.setMilesRate(BigDecimal.valueOf(0.9));

        List<Airline> airlinesToSave = List.of(airline1, airline2);
        airlineDAO.saveCollection(airlinesToSave);
        entityManager.flush();

        assertNotNull(airline1.getId());
        assertNotNull(airline2.getId());

        Collection<Airline> allAirlines = airlineDAO.getAll();
        assertEquals(4, allAirlines.size());
    }

    @Test
    void testUniqueNameConstraint() {
        Airline airline = new Airline();
        airline.setName("Test Aeroflot"); // Дублируем имя
        airline.setMilesRate(BigDecimal.valueOf(2.0));

        // Этот тест проверяет уникальность имени на уровне БД
        assertThrows(Exception.class, () -> {
            airlineDAO.save(airline);
            entityManager.flush();
        });
    }

    @Test
    void testMilesRateValidation() {
        Airline airline = new Airline();
        airline.setName("Test Miles Rate");
        airline.setMilesRate(BigDecimal.valueOf(3.5)); // Может быть ограничение в БД

        airlineDAO.save(airline);
        entityManager.flush();

        assertNotNull(airline.getId());
        assertEquals(BigDecimal.valueOf(3.5), airline.getMilesRate());
    }

    @Test
    void testDefaultMilesRate() {
        Airline airline = new Airline();
        airline.setName("Test Default Miles");
        // Не устанавливаем milesRate

        airlineDAO.save(airline);
        entityManager.flush();

        assertNotNull(airline.getId());
        // Проверяем, что установлено значение по умолчанию
        assertNotNull(airline.getMilesRate());
    }

    @Test
    void testNameLengthValidation() {
        Airline airline = new Airline();
        // Имя длиннее 50 символов
        airline.setName("This is a very long airline name that exceeds fifty characters limit");
        airline.setMilesRate(BigDecimal.valueOf(1.0));

        // Проверяем ограничение длины имени
        assertThrows(Exception.class, () -> {
            airlineDAO.save(airline);
            entityManager.flush();
        });
    }
}