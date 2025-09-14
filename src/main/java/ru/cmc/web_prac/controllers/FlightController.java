package ru.cmc.web_prac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.web_prac.DAO.AirlineDAO;
import ru.cmc.web_prac.DAO.FlightDAO;
import ru.cmc.web_prac.classes.Airline;
import ru.cmc.web_prac.classes.Flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class FlightController {

    @Autowired
    private FlightDAO flightDAO;

    @Autowired
    private AirlineDAO airlineDAO;

    @GetMapping("/flights")
    public String flightsList(@RequestParam(name = "sortBy", defaultValue = "date") String sortBy,
                              Model model) {
        List<Flight> flights = flightDAO.getAllSorted(sortBy);
        model.addAttribute("flights", flights);
        model.addAttribute("currentSort", sortBy);
        return "flights";
    }

    @GetMapping("/flight")
    public String flightDetails(@RequestParam("flightId") Long flightId, Model model) {
        Flight flight = flightDAO.getById(flightId);

        if (flight == null) {
            model.addAttribute("error_msg", "В базе нет рейса с ID = " + flightId);
            return "errorPage";
        }

        model.addAttribute("flight", flight);
        return "flight";
    }

    @GetMapping("/addFlight")
    public String addFlightForm(Model model) {
        model.addAttribute("airlines", airlineDAO.getAll());
        model.addAttribute("flight", new Flight());
        model.addAttribute("isEdit", false);
        return "editFlight";
    }

    @GetMapping("/editFlight")
    public String editFlightForm(@RequestParam("flightId") Long flightId, Model model) {
        Flight flight = flightDAO.getById(flightId);

        if (flight == null) {
            model.addAttribute("error_msg", "В базе нет рейса с ID = " + flightId);
            return "errorPage";
        }

        model.addAttribute("airlines", airlineDAO.getAll());
        model.addAttribute("flight", flight);
        model.addAttribute("isEdit", true);
        return "editFlight";
    }

    @PostMapping("/saveFlight")
    public String saveFlight(@RequestParam(name = "flightId", required = false) Long flightId,
                             @RequestParam("flightNumber") String flightNumber,
                             @RequestParam("airlineId") Long airlineId,
                             @RequestParam("departureAirport") String departureAirport,
                             @RequestParam("arrivalAirport") String arrivalAirport,
                             @RequestParam("departureTime") String departureTimeStr,
                             @RequestParam("arrivalTime") String arrivalTimeStr,
                             @RequestParam("price") BigDecimal price,
                             @RequestParam("totalSeats") Integer totalSeats,
                             @RequestParam("availableSeats") Integer availableSeats,
                             Model model) {
        try {
            Airline airline = airlineDAO.getById(airlineId);
            if (airline == null) {
                model.addAttribute("error_msg", "Авиакомпания не найдена");
                return "errorPage";
            }

            LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
            LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr);

            Flight flight;
            if (flightId != null) {
                // Редактирование существующего рейса
                flight = flightDAO.getById(flightId);
                if (flight == null) {
                    model.addAttribute("error_msg", "Рейс не найден");
                    return "errorPage";
                }
            } else {
                // Создание нового рейса
                flight = new Flight();
            }

            flight.setFlightNumber(flightNumber);
            flight.setAirline(airline);
            flight.setDepartureAirport(departureAirport);
            flight.setArrivalAirport(arrivalAirport);
            flight.setDepartureTime(departureTime);
            flight.setArrivalTime(arrivalTime);
            flight.setPrice(price);
            flight.setTotalSeats(totalSeats);
            flight.setAvailableSeats(availableSeats);

            if (flightId != null) {
                flightDAO.update(flight);
            } else {
                flightDAO.save(flight);
            }

            return "redirect:/flight?flightId=" + flight.getId();

        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при сохранении рейса: " + e.getMessage());
            return "errorPage";
        }
    }

    @PostMapping("/deleteFlight")
    public String deleteFlight(@RequestParam("flightId") Long flightId) {
        Flight flight = flightDAO.getById(flightId);
        if (flight != null) {
            flightDAO.delete(flight);
        }
        return "redirect:/flights";
    }

    @GetMapping("/availableFlights")
    public String availableFlights(Model model) {
        List<Flight> flights = flightDAO.getAvailableFlights();
        model.addAttribute("flights", flights);
        model.addAttribute("title", "Доступные рейсы");
        return "flights";
    }
}
