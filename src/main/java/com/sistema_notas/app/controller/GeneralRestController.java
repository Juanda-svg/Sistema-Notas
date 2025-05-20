package com.sistema_notas.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.UserRepository;

@RestController
@RequestMapping("/api")
public class GeneralRestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    @ResponseBody
    public List<User> getUsers() {
        System.out.println("Obteniendo usuarios para API: " + userRepository.findAll().size()); 
        return userRepository.findAll();
    }
}