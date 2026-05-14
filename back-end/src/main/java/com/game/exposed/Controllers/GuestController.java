package com.game.exposed.Controllers;


import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import com.game.exposed.dto.NameDTO;
import com.game.exposed.Exceptions.MissingSessionDataException;

@RestController
@RequestMapping("/guest")
public class GuestController {

    @PostMapping
    public String saveGuest(@RequestBody NameDTO request, HttpSession session) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (session == null) {
            throw new MissingSessionDataException("Session is invalid");
        }
        
        System.out.println(request.getName());
        session.setAttribute("name", request.getName());
        return "saved " + request.getName();
    }

    @GetMapping
    public String getGuest(HttpSession session) {
        if (session == null) {
            throw new MissingSessionDataException("Session is invalid");
        }
        
        String name = (String) session.getAttribute("name");
        return name != null ? name : "";
    }
}