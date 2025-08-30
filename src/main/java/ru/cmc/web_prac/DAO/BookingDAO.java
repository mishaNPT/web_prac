package ru.cmc.web_prac.DAO;

import ru.cmc.web_prac.classes.Booking;
import ru.cmc.web_prac.classes.Client;
import ru.cmc.web_prac.classes.Flight;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingDAO extends CommonDAO<Booking, Long> {
    List<Booking> getByClient(Client client);

    List<Booking> getByFlight(Flight flight);

    List<Booking> getByStatus(String status);

    List<Booking> getClientBookingHistory(Long clientId);

    List<Booking> getByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Booking> getPaidWithMiles();

    List<Booking> getActiveBookings();

    void updateStatus(Long bookingId, String newStatus);

    void cancelBooking(Long bookingId);

    void confirmBooking(Long bookingId);
}