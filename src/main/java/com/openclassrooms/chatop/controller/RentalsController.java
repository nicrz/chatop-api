package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.RentalResponse;
import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.RentalsResponse;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.RentalsRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.service.RentalsService;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Controller 
@RequestMapping(path="/") 
public class RentalsController {
  @Autowired
  private RentalsRepository rentalsRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ResourceLoader resourceLoader;

  @GetMapping(path = "/rentals")
  @Operation(summary = "Show the list of all rentals")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Rentals.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<RentalsResponse> getAllRentals(Authentication authentication) {

    // Vérifie si l'utilisateur est authentifié
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new UnauthorizedException("Unauthorized");
    }

    // Récupère toutes les locations
    Iterable<Rentals> rentalsIterable = rentalsRepository.findAll();

    // Convertit l'itérable en liste
    List<Rentals> rentalsList = new ArrayList<>();
    rentalsIterable.forEach(rentalsList::add);

    // Crée l'objet RentalsResponse contenant les locations
    RentalsResponse rentalsResponse = new RentalsResponse(rentalsList);

    // Retourne l'objet RentalsResponse dans la réponse
    return ResponseEntity.ok(rentalsResponse);
  }

  @GetMapping(path="/rentals/{id}")
  @Operation(summary = "Show a rental by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Rentals.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
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
  @Operation(summary = "Create a new rental")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Rental created !"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public @ResponseBody ResponseEntity<RentalResponse> createRental(@RequestParam String name,
                                        @RequestParam Integer surface,
                                        @RequestParam Integer price,
                                        @RequestParam MultipartFile picture,
                                        @RequestParam String description,
                                        Authentication authentication) {
      // Vérifie si l'utilisateur est authentifié
      if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
      }
  
      if (picture != null && !picture.isEmpty()) {
          try {
              // Définit le dossier où le fichier sera stocké
              String fileDirectory = System.getProperty("user.dir") + "/src/main/resources/static/images/";

              // Crée le chemin d'accès complet pour le fichier
              String fileName = picture.getOriginalFilename();
              Path destination = Paths.get(fileDirectory + fileName);

              // Copie le fichier vers le répertoire de destination
              Files.copy(picture.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

              // Génère l'URL complet pour accéder au fichier
              String fileUrl = "http://localhost:3000/images/" + fileName;
  
              // Crée un nouvel objet Rentals avec les autres propriétés
              Rentals newRental = new Rentals();
              newRental.setName(name);
              newRental.setSurface(surface);
              newRental.setPrice(price);
              newRental.setPicture(fileUrl);
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
  
             // Crée un objet RentalResponse avec le message approprié
             RentalResponse rentalResponse = new RentalResponse("Rental created !");

             // Retourne l'objet RentalResponse dans la réponse
             return ResponseEntity.ok(rentalResponse);
          } catch (IOException e) {
              // Gère l'erreur de téléchargement du fichier
              e.printStackTrace();
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
          }
      } else {
        return ResponseEntity.badRequest().body(null);
      }
  }

  @PutMapping(path="/rentals/{id}")
  @Operation(summary = "Update rental")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Rental updated !"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<RentalResponse> updateRental(@PathVariable Integer id,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer surface,
                                            @RequestParam(required = false) Integer price,
                                            @RequestParam(required = false) String description,
                                            Authentication authentication) {
      // Vérifie si l'utilisateur est authentifié
      if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
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
  
     // Crée un objet RentalResponse avec le message approprié
     RentalResponse rentalResponse = new RentalResponse("Rental updated !");

     // Retourne l'objet RentalResponse dans la réponse
     return ResponseEntity.ok(rentalResponse);
  }

}
