package ru.cmc.web_prac.DAO.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.cmc.web_prac.DAO.BookingDAO;
import ru.cmc.web_prac.classes.Booking;
import ru.cmc.web_prac.classes.Client;
import ru.cmc.web_prac.classes.Flight;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class BookingDAOImpl extends CommonDAOImpl<Booking, Long> implements BookingDAO {

    public BookingDAOImpl() {
        super(Booking.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getByClient(Client client) {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.client = :client ORDER BY b.bookingDate DESC", Booking.class);
        query.setParameter("client", client);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getByFlight(Flight flight) {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.flight = :flight ORDER BY b.bookingDate", Booking.class);
        query.setParameter("flight", flight);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getByStatus(String status) {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.status = :status ORDER BY b.bookingDate DESC", Booking.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getClientBookingHistory(Long clientId) {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b JOIN FETCH b.flight f JOIN FETCH f.airline " +
                        "WHERE b.client.id = :clientId ORDER BY b.bookingDate DESC", Booking.class);
        query.setParameter("clientId", clientId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate ORDER BY b.bookingDate",
                Booking.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getPaidWithMiles() {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.paidWithMiles = true ORDER BY b.bookingDate DESC", Booking.class);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getActiveBookings() {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.status != 'CANCELLED' ORDER BY b.bookingDate DESC", Booking.class);
        return query.getResultList();
    }

    @Override
    public void updateStatus(Long bookingId, String newStatus) {
        Query query = entityManager.createQuery(
                "UPDATE Booking b SET b.status = :status WHERE b.id = :bookingId");
        query.setParameter("status", newStatus);
        query.setParameter("bookingId", bookingId);
        query.executeUpdate();
    }

    @Override
    public void cancelBooking(Long bookingId) {
        updateStatus(bookingId, "CANCELLED");
    }

    @Override
    public void confirmBooking(Long bookingId) {
        updateStatus(bookingId, "PAID");
    }
}