package com.sistema_notas.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeneralController {
	
	@GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model, String error) {
        if (error != null) {
            model.addAttribute("error", "ID o contraseña incorrectos");
        }
        return "login";
    }


}
