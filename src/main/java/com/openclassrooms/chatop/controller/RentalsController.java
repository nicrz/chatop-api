package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.RentalsRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.service.RentalsService;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.openclassrooms.chatop.exception.UnauthorizedException;
import com.openclassrooms.chatop.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
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
import org.springframework.web.multipart.MultipartFile;

@Controller 
@RequestMapping(path="/") 
public class RentalsController {
  @Autowired
  private RentalsRepository rentalsRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ResourceLoader resourceLoader;

  @Value("${file.upload-dir}")
  private String uploadDir;

  @GetMapping(path = "/rentals")
  public @ResponseBody Iterable<Rentals> getAllRentals(Authentication authentication) {

    // Vérifie si l'utilisateur est authentifié
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new UnauthorizedException("Unauthorized");
    }

    // Renvoie toutes les locations
    return rentalsRepository.findAll();
  }

  @GetMapping(path="/rentals/{id}")
  public @ResponseBody Optional<Rentals> getRental(@PathVariable Integer id, Authentication authentication) {
    // Vérifie si l'utilisateur est authentifié
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new UnauthorizedException("Unauthorized");
    }

    // Récup la location par son ID
    Optional<Rentals> rental = rentalsRepository.findById(id);

    if (rental.isPresent()) {
        return rental;
    } else {
        throw new NotFoundException("Rental not found");
    }
  }

  @PostMapping(path="/rentals")
  public @ResponseBody ResponseEntity<String> createRental(@RequestParam String name,
                                        @RequestParam Integer surface,
                                        @RequestParam Integer price,
                                        @RequestParam MultipartFile picture,
                                        @RequestParam String description,
                                        Authentication authentication) {
      // Vérifie si l'utilisateur est authentifié
      if (authentication == null || !authentication.isAuthenticated()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
      }
  
      if (picture != null && !picture.isEmpty()) {
          try {
              // Enregistre le fichier sur le serveur
              String fileName = UUID.randomUUID().toString() + "_" + picture.getOriginalFilename();
              Path targetPath = Path.of(uploadDir, fileName);
              Files.copy(picture.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
  
              // Enregistre l'URL de l'image dans la base de données
              String pictureUrl = "http://localhost:3000/files/media/" + fileName;
  
              // Crée un nouvel objet Rentals avec les autres propriétés
              Rentals newRental = new Rentals();
              newRental.setName(name);
              newRental.setSurface(surface);
              newRental.setPrice(price);
              newRental.setPicture(pictureUrl);
              newRental.setDescription(description);
  
              // Récup l'ID de l'utilisateur authentifié
              String email = authentication.getName();
              User user = userRepository.findByEmail(email);
              Integer owner_id = user.getId();
              newRental.setOwner_id(owner_id);
              newRental.setCreated_at(new Timestamp(System.currentTimeMillis()));
              newRental.setUpdated_at(new Timestamp(System.currentTimeMillis()));
  
              // Enregistre l'objet Rentals dans la base de données
              rentalsRepository.save(newRental);
  
              return ResponseEntity.ok("Rental created !");
          } catch (IOException e) {
              // Gère l'erreur de téléchargement du fichier
              e.printStackTrace();
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading picture");
          }
      } else {
          return ResponseEntity.badRequest().body("No picture uploaded");
      }
  }

  @PutMapping(path="/rental/{id}")
  public ResponseEntity<String> updateRental(@PathVariable Integer id,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer surface,
                                            @RequestParam(required = false) Integer price,
                                            @RequestParam(required = false) String description,
                                            Authentication authentication) {
      // Vérifie si l'utilisateur est authentifié
      if (authentication == null || !authentication.isAuthenticated()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
      }
  
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
  
      // Met à jour les champs modifiables de la location
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
  
      // Modif la date de mise à jour
      Timestamp now = Timestamp.from(Instant.now());
      rental.setUpdated_at(now);
  
      // Enregistre les modifications
      rentalsRepository.save(rental);
  
      return ResponseEntity.ok("Rental updated !");
  }

}
