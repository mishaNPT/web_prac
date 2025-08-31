package ru.cmc.web_prac.DAO;

import org.junit.jupiter.api.Test;
import ru.cmc.web_prac.classes.Booking;
import ru.cmc.web_prac.classes.Client;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingDAOTest extends BaseDAOTest {
    @Test
    void testSave() {
        Booking booking = new Booking();
        booking.setClient(testClient2);
        booking.setFlight(testFlight2);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("BOOKED");
        booking.setPaidWithMiles(false);
        booking.setMilesUsed(0);

        bookingDAO.save(booking);
        entityManager.flush();

        assertNotNull(booking.getId());
        assertTrue(booking.getId() > 0);
    }

    @Test
    void testGetById() {
        Booking foundBooking = bookingDAO.getById(testBooking1.getId());

        assertNotNull(foundBooking);
        assertEquals(testBooking1.getId(), foundBooking.getId());
        assertEquals("BOOKED", foundBooking.getStatus());
        assertEquals(testClient1.getId(), foundBooking.getClient().getId());
        assertEquals(testFlight1.getId(), foundBooking.getFlight().getId());
        assertEquals(0, foundBooking.getMilesUsed());
    }

    @Test
    void testGetByIdNonExistent() {
        Booking foundBooking = bookingDAO.getById(999L);
        assertNull(foundBooking);
    }

    @Test
    void testGetAll() {
        // Создаем дополнительное бронирование
        Booking booking2 = new Booking();
        booking2.setClient(testClient2);
        booking2.setFlight(testFlight2);
        booking2.setStatus("PAID");
        booking2.setPaidWithMiles(true);
        booking2.setMilesUsed(1000);
        bookingDAO.save(booking2);
        entityManager.flush();

        Collection<Booking> allBookings = bookingDAO.getAll();

        assertNotNull(allBookings);
        assertEquals(2, allBookings.size());
    }

    @Test
    void testUpdate() {
        testBooking1.setStatus("PAID");
        testBooking1.setPaidWithMiles(true);
        testBooking1.setMilesUsed(200);

        bookingDAO.update(testBooking1);
        entityManager.flush();
        entityManager.clear();

        Booking updatedBooking = bookingDAO.getById(testBooking1.getId());
        assertEquals("PAID", updatedBooking.getStatus());
        assertTrue(updatedBooking.getPaidWithMiles());
        assertEquals(200, updatedBooking.getMilesUsed());
    }

    @Test
    void testDelete() {
        Long bookingId = testBooking1.getId();

        bookingDAO.delete(testBooking1);
        entityManager.flush();

        Booking deletedBooking = bookingDAO.getById(bookingId);
        assertNull(deletedBooking);

        Collection<Booking> allBookings = bookingDAO.getAll();
        assertEquals(0, allBookings.size());
    }

    @Test
    void testGetByClient() {
        // Создаем еще одно бронирование для того же клиента
        Booking booking2 = new Booking();
        booking2.setClient(testClient1);
        booking2.setFlight(testFlight2);
        booking2.setStatus("CANCELLED");
        booking2.setBookingDate(LocalDateTime.now().minusDays(1));
        bookingDAO.save(booking2);
        entityManager.flush();

        List<Booking> clientBookings = bookingDAO.getByClient(testClient1);

        assertEquals(2, clientBookings.size());
        assertTrue(clientBookings.stream().allMatch(b -> b.getClient().getId().equals(testClient1.getId())));

        // Проверяем сортировку по дате (новые сверху)
        assertTrue(clientBookings.get(0).getBookingDate().isAfter(clientBookings.get(1).getBookingDate()) ||
                clientBookings.get(0).getBookingDate().equals(clientBookings.get(1).getBookingDate()));
    }

    @Test
    void testGetByClientEmpty() {
        List<Booking> clientBookings = bookingDAO.getByClient(testClient2);
        assertTrue(clientBookings.isEmpty());
    }

    @Test
    void testGetByFlight() {
        // Создаем еще одно бронирование для того же рейса
        Booking booking2 = new Booking();
        booking2.setClient(testClient2);
        booking2.setFlight(testFlight1);
        booking2.setStatus("BOOKED");
        booking2.setBookingDate(LocalDateTime.now().plusMinutes(10));
        bookingDAO.save(booking2);
        entityManager.flush();

        List<Booking> flightBookings = bookingDAO.getByFlight(testFlight1);

        assertEquals(2, flightBookings.size());
        assertTrue(flightBookings.stream().allMatch(b -> b.getFlight().getId().equals(testFlight1.getId())));

        // Проверяем сортировку по дате бронирования
        assertTrue(flightBookings.get(0).getBookingDate().isBefore(flightBookings.get(1).getBookingDate()) ||
                flightBookings.get(0).getBookingDate().equals(flightBookings.get(1).getBookingDate()));
    }

    @Test
    void testGetByFlightEmpty() {
        List<Booking> flightBookings = bookingDAO.getByFlight(testFlight2);
        assertTrue(flightBookings.isEmpty());
    }

    @Test
    void testGetByStatus() {
        // Создаем бронирования с разными статусами
        Booking paidBooking = new Booking();
        paidBooking.setClient(testClient2);
        paidBooking.setFlight(testFlight2);
        paidBooking.setStatus("PAID");
        bookingDAO.save(paidBooking);

        Booking cancelledBooking = new Booking();
        cancelledBooking.setClient(testClient2);
        cancelledBooking.setFlight(testFlight2);
        cancelledBooking.setStatus("CANCELLED");
        bookingDAO.save(cancelledBooking);
        entityManager.flush();

        List<Booking> bookedBookings = bookingDAO.getByStatus("BOOKED");
        List<Booking> paidBookings = bookingDAO.getByStatus("PAID");
        List<Booking> cancelledBookings = bookingDAO.getByStatus("CANCELLED");

        assertEquals(1, bookedBookings.size());
        assertEquals(1, paidBookings.size());
        assertEquals(1, cancelledBookings.size());

        assertEquals("BOOKED", bookedBookings.get(0).getStatus());
        assertEquals("PAID", paidBookings.get(0).getStatus());
        assertEquals("CANCELLED", cancelledBookings.get(0).getStatus());
    }

    @Test
    void testGetByStatusEmpty() {
        List<Booking> bookings = bookingDAO.getByStatus("NONEXISTENT");
        assertTrue(bookings.isEmpty());
    }

    @Test
    void testGetClientBookingHistory() {
        // Создаем историю бронирований для клиента
        Booking booking1 = new Booking();
        booking1.setClient(testClient1);
        booking1.setFlight(testFlight2);
        booking1.setStatus("PAID");
        booking1.setBookingDate(LocalDateTime.now().minusDays(1));
        bookingDAO.save(booking1);
        entityManager.flush();

        List<Booking> history = bookingDAO.getClientBookingHistory(testClient1.getId());

        assertEquals(2, history.size());

        // Проверяем, что все бронирования принадлежат клиенту
        assertTrue(history.stream().allMatch(b -> b.getClient().getId().equals(testClient1.getId())));

        // Проверяем наличие связанных данных (FETCH)
        assertNotNull(history.get(0).getFlight());
        assertNotNull(history.get(0).getFlight().getAirline());
    }

    @Test
    void testGetClientBookingHistoryEmpty() {
        List<Booking> history = bookingDAO.getClientBookingHistory(999L);
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetByBookingDateBetween() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusHours(1);
        LocalDateTime endDate = now.plusHours(1);

        List<Booking> bookings = bookingDAO.getByBookingDateBetween(startDate, endDate);

        assertEquals(1, bookings.size());
        assertTrue(bookings.get(0).getBookingDate().isAfter(startDate.minusSeconds(1)));
        assertTrue(bookings.get(0).getBookingDate().isBefore(endDate.plusSeconds(1)));
    }

    @Test
    void testGetByBookingDateBetweenEmpty() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        List<Booking> bookings = bookingDAO.getByBookingDateBetween(startDate, endDate);
        assertTrue(bookings.isEmpty());
    }

    @Test
    void testGetPaidWithMiles() {
        // Создаем бронирование, оплаченное милями
        Booking milesBooking = new Booking();
        milesBooking.setClient(testClient2);
        milesBooking.setFlight(testFlight2);
        milesBooking.setStatus("PAID");
        milesBooking.setPaidWithMiles(true);
        milesBooking.setMilesUsed(1500);
        bookingDAO.save(milesBooking);
        entityManager.flush();

        List<Booking> milesBookings = bookingDAO.getPaidWithMiles();

        assertEquals(1, milesBookings.size());
        assertTrue(milesBookings.get(0).getPaidWithMiles());
        assertTrue(milesBookings.get(0).getMilesUsed() > 0);
    }

    @Test
    void testGetPaidWithMilesEmpty() {
        List<Booking> milesBookings = bookingDAO.getPaidWithMiles();
        assertTrue(milesBookings.isEmpty());
    }

    @Test
    void testGetActiveBookings() {
        // Создаем активные и неактивные бронирования
        Booking activeBooking = new Booking();
        activeBooking.setClient(testClient2);
        activeBooking.setFlight(testFlight2);
        activeBooking.setStatus("PAID");
        bookingDAO.save(activeBooking);

        Booking cancelledBooking = new Booking();
        cancelledBooking.setClient(testClient2);
        cancelledBooking.setFlight(testFlight2);
        cancelledBooking.setStatus("CANCELLED");
        bookingDAO.save(cancelledBooking);
        entityManager.flush();

        List<Booking> activeBookings = bookingDAO.getActiveBookings();

        assertEquals(2, activeBookings.size()); // testBooking1 (BOOKED) + activeBooking (PAID)
        assertTrue(activeBookings.stream().noneMatch(b -> "CANCELLED".equals(b.getStatus())));
    }

    @Test
    void testUpdateStatus() {
        Long bookingId = testBooking1.getId();
        String newStatus = "PAID";

        bookingDAO.updateStatus(bookingId, newStatus);
        entityManager.flush();
        entityManager.clear();

        Booking updatedBooking = bookingDAO.getById(bookingId);
        assertEquals(newStatus, updatedBooking.getStatus());
    }

    @Test
    void testCancelBooking() {
        Long bookingId = testBooking1.getId();

        bookingDAO.cancelBooking(bookingId);
        entityManager.flush();
        entityManager.clear();

        Booking cancelledBooking = bookingDAO.getById(bookingId);
        assertEquals("CANCELLED", cancelledBooking.getStatus());
    }

    @Test
    void testConfirmBooking() {
        Long bookingId = testBooking1.getId();

        bookingDAO.confirmBooking(bookingId);
        entityManager.flush();
        entityManager.clear();

        Booking confirmedBooking = bookingDAO.getById(bookingId);
        assertEquals("PAID", confirmedBooking.getStatus());
    }

    @Test
    void testSaveCollection() {
        Booking booking1 = new Booking();
        booking1.setClient(testClient1);
        booking1.setFlight(testFlight2);
        booking1.setStatus("BOOKED");
        booking1.setMilesUsed(100);

        Booking booking2 = new Booking();
        booking2.setClient(testClient2);
        booking2.setFlight(testFlight1);
        booking2.setStatus("PAID");
        booking2.setPaidWithMiles(true);
        booking2.setMilesUsed(300);

        List<Booking> bookingsToSave = List.of(booking1, booking2);
        bookingDAO.saveCollection(bookingsToSave);
        entityManager.flush();

        assertNotNull(booking1.getId());
        assertNotNull(booking2.getId());

        Collection<Booking> allBookings = bookingDAO.getAll();
        assertEquals(3, allBookings.size());
    }

    @Test
    void testDefaultValues() {
        Booking booking = new Booking();
        booking.setClient(testClient1);
        booking.setFlight(testFlight1);
        // Не устанавливаем остальные поля, проверяем значения по умолчанию

        bookingDAO.save(booking);
        entityManager.flush();
        entityManager.clear();

        Booking savedBooking = bookingDAO.getById(booking.getId());
        assertEquals("BOOKED", savedBooking.getStatus());
        assertFalse(savedBooking.getPaidWithMiles());
        assertEquals(0, savedBooking.getMilesUsed());
        assertNotNull(savedBooking.getBookingDate()); // Должна быть установлена автоматически
    }

    @Test
    void testForeignKeyConstraints() {
        // Попытка сохранить бронирование с несуществующим клиентом должна завершиться ошибкой
        Client nonExistentClient = new Client();
        nonExistentClient.setId(999L);

        Booking booking = new Booking();
        booking.setClient(nonExistentClient);
        booking.setFlight(testFlight1);

        assertThrows(Exception.class, () -> {
            bookingDAO.save(booking);
            entityManager.flush();
        });
    }

    @Test
    void testBookingDateOrdering() {
        // Создаем несколько бронирований с разными датами
        Booking oldBooking = new Booking();
        oldBooking.setClient(testClient2);
        oldBooking.setFlight(testFlight2);
        oldBooking.setBookingDate(LocalDateTime.now().minusDays(5));
        bookingDAO.save(oldBooking);

        Booking recentBooking = new Booking();
        recentBooking.setClient(testClient2);
        recentBooking.setFlight(testFlight2);
        recentBooking.setBookingDate(LocalDateTime.now().minusHours(1));
        bookingDAO.save(recentBooking);
        entityManager.flush();

        // Проверяем сортировку в различных методах
        List<Booking> client2Bookings = bookingDAO.getByClient(testClient2);
        assertEquals(2, client2Bookings.size());
        // Должны быть отсортированы по убыванию даты (новые сверху)
        assertTrue(client2Bookings.get(0).getBookingDate().isAfter(client2Bookings.get(1).getBookingDate()));
    }
}