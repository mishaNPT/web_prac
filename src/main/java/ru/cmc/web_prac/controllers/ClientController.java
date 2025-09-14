package ru.cmc.web_prac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.web_prac.DAO.BookingDAO;
import ru.cmc.web_prac.DAO.ClientDAO;
import ru.cmc.web_prac.classes.Booking;
import ru.cmc.web_prac.classes.Client;

import java.util.List;

@Controller
public class ClientController {

    @Autowired
    private ClientDAO clientDAO;

    @Autowired
    private BookingDAO bookingDAO;

    @GetMapping("/clients")
    public String clientsList(@RequestParam(name = "search", required = false) String searchTerm,
                              Model model) {
        List<Client> clients;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            clients = clientDAO.searchByNameOrEmail(searchTerm.trim());
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("searchPerformed", true);
        } else {
            clients = (List<Client>) clientDAO.getAll();
        }

        model.addAttribute("clients", clients);
        return "clients";
    }

    @GetMapping("/client")
    public String clientDetails(@RequestParam("clientId") Long clientId, Model model) {
        Client client = clientDAO.getById(clientId);

        if (client == null) {
            model.addAttribute("error_msg", "В базе нет клиента с ID = " + clientId);
            return "errorPage";
        }

        // Получаем историю бронирований клиента
        List<Booking> bookings = bookingDAO.getClientBookingHistory(clientId);

        model.addAttribute("client", client);
        model.addAttribute("bookings", bookings);
        return "client";
    }

    @GetMapping("/addClient")
    public String addClientForm(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("isEdit", false);
        return "editClient";
    }

    @GetMapping("/editClient")
    public String editClientForm(@RequestParam("clientId") Long clientId, Model model) {
        Client client = clientDAO.getById(clientId);

        if (client == null) {
            model.addAttribute("error_msg", "В базе нет клиента с ID = " + clientId);
            return "errorPage";
        }

        model.addAttribute("client", client);
        model.addAttribute("isEdit", true);
        return "editClient";
    }

    @PostMapping("/saveClient")
    public String saveClient(@RequestParam(name = "clientId", required = false) Long clientId,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("email") String email,
                             @RequestParam("phone") String phone,
                             @RequestParam("address") String address,
                             @RequestParam("bonusMiles") Integer bonusMiles,
                             Model model) {
        try {
            Client client;
            if (clientId != null) {
                // Редактирование существующего клиента
                client = clientDAO.getById(clientId);
                if (client == null) {
                    model.addAttribute("error_msg", "Клиент не найден");
                    return "errorPage";
                }
            } else {
                // Создание нового клиента
                client = new Client();
            }

            client.setFullName(fullName);
            client.setEmail(email);
            client.setPhone(phone);
            client.setAddress(address);
            client.setBonusMiles(bonusMiles);

            if (clientId != null) {
                clientDAO.update(client);
            } else {
                clientDAO.save(client);
            }

            return "redirect:/client?clientId=" + client.getId();

        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при сохранении клиента: " + e.getMessage());
            return "errorPage";
        }
    }

    @PostMapping("/deleteClient")
    public String deleteClient(@RequestParam("clientId") Long clientId) {
        Client client = clientDAO.getById(clientId);
        if (client != null) {
            clientDAO.delete(client);
        }
        return "redirect:/clients";
    }

    @PostMapping("/addMiles")
    public String addMiles(@RequestParam("clientId") Long clientId,
                           @RequestParam("miles") Integer miles,
                           Model model) {
        try {
            clientDAO.addBonusMiles(clientId, miles);
            return "redirect:/client?clientId=" + clientId;
        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при добавлении миль: " + e.getMessage());
            return "errorPage";
        }
    }

    @PostMapping("/deductMiles")
    public String deductMiles(@RequestParam("clientId") Long clientId,
                              @RequestParam("miles") Integer miles,
                              Model model) {
        try {
            clientDAO.deductBonusMiles(clientId, miles);
            return "redirect:/client?clientId=" + clientId;
        } catch (Exception e) {
            model.addAttribute("error_msg", "Ошибка при списании миль: " + e.getMessage());
            return "errorPage";
        }
    }
}
