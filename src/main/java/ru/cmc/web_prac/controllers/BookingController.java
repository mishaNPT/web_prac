package ru.cmc.web_prac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.web_prac.DAO.BookingDAO;
import ru.cmc.web_prac.DAO.ClientDAO;
import ru.cmc.web_prac.DAO.FlightDAO;
import ru.cmc.web_prac.classes.Booking;
import ru.cmc.web_prac.classes.Client;
import ru.cmc.web_prac.classes.Flight;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private FlightDAO flightDAO;

    @Autowired
    private ClientDAO clientDAO;

    @GetMapping("/bookings")
    public String bookingsList(@RequestParam(name = "status", required = false) String status,
                               Model model) {
        List<Booking> bookings;

        if (status != null && !status.isEmpty()) {
            bookings = bookingDAO.getByStatus(status);
            model.addAttribute("filterStatus", status);
        } else {
            bookings = (List<Booking>) bookingDAO.getAll();
        }

        model.addAttribute("bookings", bookings);
        return "bookings";
    }

    @GetMapping("/booking")
    public String bookingDetails(@RequestParam("bookingId") Long bookingId, Model model) {
        Booking booking = bookingDAO.getById(bookingId);

        if (booking == null) {
            model.addAttribute("error_msg", "В базе нет бронирования с ID = " + bookingId);
            return "errorPage";
        }

        model.addAttribute("booking", booking);
        return "booking";
    }

    @GetMapping("/createBooking")
    public String createBookingForm(@RequestParam("flightId") Long flightId, Model model) {
        Flight flight = flightDAO.getById(flightId);

        if (flight == null) {
            model.addAttribute("error_msg", "Рейс не найден");
            return "errorPage";
        }

        if (flight.getAvailableSeats() <= 0) {
            model.addAttribute("error_msg", "На данном рейсе нет свободных мест");
            return "errorPage";
        }

        List<Client> clients = (List<Client>) clientDAO.getAll();

        model.addAttribute("flight", flight);
        model.addAttribute("clients", clients);
        return "createBooking";
    }

    @PostMapping("/saveBooking")
    public String saveBooking(@RequestParam("flightId") Long flightId,
                              @RequestParam("clientId") Long clientId,
                              @RequestParam(name = "paidWithMiles", defaultValue = "false") boolean paidWithMiles,
                              @RequestParam(name = "milesUsed", defaultValue = "0") Integer milesUsed,
                              Model model) {
        try {
            Flight flight = flightDAO.getById(flightId);
            Client client = clientDAO.getById(clientId);

            if (flight == null || client == null) {
                model.addAttribute("error_msg", "Рейс или клиент не найдены");
                return "errorPage";
            }

            if (flight.getAvailableSeats() <= 0) {
                model.addAttribute("error_msg", "На данном рейсе нет свободных мест");
                return "errorPage";
            }

            // Проверяем мили клиента, если используется оплата милями
            if (paidWithMiles && milesUsed > 0) {
                if (client.getBonusMiles() < milesUsed) {
                    model.addAttribute("error_msg", "У клиента недостаточно бонусных миль");
                    return "errorPage";
                }
            }

            // Создаем бронирование
            Booking booking = new Booking();
            booking.setFlight(flight);
            booking.setClient(client);
            booking.setBookingDate(LocalDateTime.now());
            booking.setStatus("BOOKED");
            booking.setPaidWithMiles(paidWithMiles);
            booking.setMilesUsed(milesUsed);

            bookingDAO.save(booking);

            // Уменьшаем количество доступных мест
            flightDAO.decrementAvailableSeats(flightId);

            // Списываем мили, если используется оплата милями
            if (paidWithMiles && milesUsed > 0) {
                clientDAO.deductBonusMiles(clientId, milesUsed);
            }

            return "redirect:/booking?bookingId=" + booking.getId();

        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при создании бронирования: " + e.getMessage());
            return "errorPage";
        }
    }

    @PostMapping("/confirmBooking")
    public String confirmBooking(@RequestParam("bookingId") Long bookingId, Model model) {
        try {
            Booking booking = bookingDAO.getById(bookingId);
            if (booking == null) {
                model.addAttribute("error_msg", "Бронирование не найдено");
                return "errorPage";
            }

            bookingDAO.confirmBooking(bookingId);

            // Начисляем мили за полет
            Flight flight = booking.getFlight();
            if (flight != null && flight.getAirline() != null) {
                int earnedMiles = flight.getPrice().multiply(flight.getAirline().getMilesRate()).intValue() / 100;
                clientDAO.addBonusMiles(booking.getClient().getId(), earnedMiles);
            }

            return "redirect:/booking?bookingId=" + bookingId;

        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при подтверждении бронирования: " + e.getMessage());
            return "errorPage";
        }
    }

    @PostMapping("/cancelBooking")
    public String cancelBooking(@RequestParam("bookingId") Long bookingId, Model model) {
        try {
            Booking booking = bookingDAO.getById(bookingId);
            if (booking == null) {
                model.addAttribute("error_msg", "Бронирование не найдено");
                return "errorPage";
            }

            bookingDAO.cancelBooking(bookingId);

            // Возвращаем место в самолет
            flightDAO.incrementAvailableSeats(booking.getFlight().getId());

            // Возвращаем мили, если они использовались
            if (booking.getPaidWithMiles() && booking.getMilesUsed() > 0) {
                clientDAO.addBonusMiles(booking.getClient().getId(), booking.getMilesUsed());
            }

            return "redirect:/booking?bookingId=" + bookingId;

        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при отмене бронирования: " + e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("/activeBookings")
    public String activeBookings(Model model) {
        List<Booking> bookings = bookingDAO.getActiveBookings();
        model.addAttribute("bookings", bookings);
        model.addAttribute("title", "Активные бронирования");
        return "bookings";
    }

    @GetMapping("/milesBookings")
    public String milesBookings(Model model) {
        List<Booking> bookings = bookingDAO.getPaidWithMiles();
        model.addAttribute("bookings", bookings);
        model.addAttribute("title", "Бронирования, оплаченные милями");
        return "bookings";
    }
}
