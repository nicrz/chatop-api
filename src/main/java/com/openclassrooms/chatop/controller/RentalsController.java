package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.User;
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
public class RentalsController {
  @Autowired
  private RentalsRepository rentalsRepository;

  @Autowired
  private UserRepository userRepository;

  @GetMapping(path="/rentals")
  public @ResponseBody Iterable<Rentals> getAllRentals() {
    // This returns a JSON or XML with the rentals
    return rentalsRepository.findAll();
  }

  @GetMapping(path="/rental/{id}")
  public @ResponseBody Optional<Rentals> getRental(@PathVariable Integer id) {
    // This returns a JSON or XML with the rentals
    return rentalsRepository.findById(id);
  }

  @PostMapping(path = "/rentals")
  public @ResponseBody Rentals createRental(@RequestParam String name,
                                            @RequestParam Integer surface,
                                            @RequestParam Integer price,
                                            @RequestParam String picture,
                                            @RequestParam String description,
                                            Authentication authentication) {
      Rentals newRental = new Rentals();
      newRental.setName(name);
      newRental.setSurface(surface);
      newRental.setPrice(price);
      newRental.setPicture(picture);
      newRental.setDescription(description);
      // Récup l'ID de l'utilisateur authentifié
      String email = authentication.getName();
      User user = userRepository.findByEmail(email);
      Integer ownerId = user.getId();
      newRental.setOwner_id(ownerId);

      Timestamp now = Timestamp.from(Instant.now());
      newRental.setCreated_at(now);
      newRental.setUpdated_at(now);
  
      return rentalsRepository.save(newRental);
  }

  @PutMapping(path = "/rental/{id}")
  public ResponseEntity<Rentals> updateRental(@PathVariable Integer id,
                                              @RequestParam(required = false) String name,
                                              @RequestParam(required = false) Integer surface,
                                              @RequestParam(required = false) Integer price,
                                              @RequestParam(required = false) String description,
                                              Authentication authentication) {
      // Vérifie si la location existe
      Optional<Rentals> rentalOptional = rentalsRepository.findById(id);
      if (rentalOptional.isEmpty()) {
          return ResponseEntity.notFound().build();
      }

      // Récupère l'utilisateur authentifié
      String email = authentication.getName();
      User user = userRepository.findByEmail(email);
      Integer ownerId = user.getId();

      Rentals rental = rentalOptional.get();

      // Vérifie si l'utilisateur est le propriétaire de la location
      if (!rental.getOwner_id().equals(ownerId)) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      // Met à jour les champs modifiables du rental
      if (name != null) {
          rental.setName(name);
      }
      if (surface != null) {
          rental.setSurface(surface);
      }
      if (price != null) {
          rental.setPrice(price);
      }
      if (description != null) {
          rental.setDescription(description);
      }

      // Met à jour la date de mise à jour
      Timestamp now = Timestamp.from(Instant.now());
      rental.setUpdated_at(now);

      // Enregistre les modifications
      Rentals updatedRental = rentalsRepository.save(rental);

      return ResponseEntity.ok(updatedRental);
  }

}
