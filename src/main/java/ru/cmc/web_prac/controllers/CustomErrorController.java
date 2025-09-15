package ru.cmc.web_prac.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        String errorMessage = "Произошла неизвестная ошибка";

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            switch(statusCode) {
                case 404:
                    errorMessage = "Запрашиваемая страница не найдена";
                    break;
                case 500:
                    errorMessage = "Внутренняя ошибка сервера";
                    break;
                case 403:
                    errorMessage = "Доступ к ресурсу запрещен";
                    break;
                default:
                    errorMessage = "Произошла ошибка " + statusCode;
                    break;
            }
        }

        model.addAttribute("error_msg", errorMessage);
        return "errorPage";
    }
}