package ru.cmc.web_prac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.web_prac.DAO.AirlineDAO;
import ru.cmc.web_prac.DAO.FlightDAO;
import ru.cmc.web_prac.classes.Flight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private FlightDAO flightDAO;

    @Autowired
    private AirlineDAO airlineDAO;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        // Передаем все авиакомпании для выпадающего списка
        model.addAttribute("airlines", airlineDAO.getAll());
        return "index";
    }

    @PostMapping("/search")
    public String searchFlights(@RequestParam("departureAirport") String departureAirport,
                                @RequestParam("arrivalAirport") String arrivalAirport,
                                @RequestParam("departureDate") String departureDateStr,
                                Model model) {
        try {
            // Парсим дату
            LocalDateTime departureDate = LocalDateTime.parse(departureDateStr + "T00:00:00");

            // Ищем рейсы
            List<Flight> flights = flightDAO.searchFlights(departureAirport, arrivalAirport, departureDate);

            model.addAttribute("flights", flights);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("departureAirport", departureAirport);
            model.addAttribute("arrivalAirport", arrivalAirport);
            model.addAttribute("departureDate", departureDateStr);

        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при поиске рейсов: " + e.getMessage());
            return "errorPage";
        }

        return "searchResults";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
