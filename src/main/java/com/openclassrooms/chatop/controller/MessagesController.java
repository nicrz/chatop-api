package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.Messages;
import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.MessagesRepository;
import com.openclassrooms.chatop.repository.RentalsRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.service.RentalsService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // This means that this class is a Controller
@RequestMapping(path="/") // This means URL's start with /demo (after Application path)
public class MessagesController {
  @Autowired
  private MessagesRepository messagesRepository;

  @Autowired
  private RentalsRepository rentalsRepository;

  @Autowired
  private UserRepository userRepository;

  @GetMapping(path="/messages")
  public @ResponseBody Iterable<Messages> getAllMessages() {
    // This returns a JSON or XML with the rentals
    return messagesRepository.findAll();
  }

  @GetMapping(path="/message/{id}")
  public @ResponseBody Optional<Messages> getMessage(@PathVariable Integer id) {
    // This returns a JSON or XML with the rentals
    return messagesRepository.findById(id);
  }

  @PostMapping(path="/messages")
  public ResponseEntity<String> createMessage(@RequestParam Integer rentalId, 
                                                @RequestParam String message,
                                                Authentication authentication) {
    // Vérifie si le rental existe
    Optional<Rentals> optionalRental = rentalsRepository.findById(rentalId);
    if (optionalRental.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rental not found");
    }

    // Crée un nouvel objet Messages
    Messages newMessage = new Messages();
    newMessage.setRental_id(rentalId);
    // Récup l'ID de l'utilisateur authentifié
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);
    Integer userId = user.getId();
    newMessage.setUser_id(userId);
    newMessage.setMessage(message);
    Timestamp now = Timestamp.from(Instant.now());
    newMessage.setCreated_at(now);
    newMessage.setUpdated_at(now);

    // Sauvegarde le message dans la base de données
    messagesRepository.save(newMessage);

    return ResponseEntity.ok("Message created successfully");
  }

}
