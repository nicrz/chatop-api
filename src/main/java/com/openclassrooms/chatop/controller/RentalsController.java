package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.RentalsRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.responses.RentalResponse;
import com.openclassrooms.chatop.responses.RentalsResponse;
import com.openclassrooms.chatop.service.RentalsService;
import com.openclassrooms.dto.RentalRequest;

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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Controller 
@RequestMapping(path="/") 
public class RentalsController {

  private final RentalsService rentalsService;

  public RentalsController(RentalsService rentalsService) {
      this.rentalsService = rentalsService;
  }

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
    Iterable<Rentals> rentalsIterable = rentalsService.getAllRentals();

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
    Optional<Rentals> rental = rentalsService.getRentalById(id);

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
  public ResponseEntity<RentalResponse> createRental(@RequestBody RentalRequest rentalRequest, Authentication authentication) {
      // Vérifie si l'utilisateur est authentifié
      if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
      }

      MultipartFile picture = rentalRequest.getPicture();
  
      if (picture != null && !picture.isEmpty()) {
          try {
               // Enregistre la location
              rentalsService.createRental(rentalRequest, picture, authentication);

              RentalResponse rentalResponse = new RentalResponse("Rental created!");

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
                                          @RequestBody RentalRequest rentalRequest,
                                          Authentication authentication) {
      // Vérifie si l'utilisateur est authentifié
      if (authentication == null || !authentication.isAuthenticated()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
      }
  
      // Vérifie si la location existe
      Optional<Rentals> rentalOptional = rentalsService.getRentalById(id);
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
      if (rentalRequest.getName() != null) {
          rental.setName(rentalRequest.getName());
      }
      if (rentalRequest.getSurface() != null) {
          rental.setSurface(rentalRequest.getSurface());
      }
      if (rentalRequest.getPrice() != null) {
          rental.setPrice(rentalRequest.getPrice());
      }
      if (rentalRequest.getDescription() != null) {
          rental.setDescription(rentalRequest.getDescription());
      }
  
      // Modifie la date de mise à jour
      Timestamp now = Timestamp.from(Instant.now());
      rental.setUpdated_at(now);
  
      // Enregistre les modifications
      rentalsService.saveRental(rental);
  
      // Crée un objet RentalResponse avec le message approprié
      RentalResponse rentalResponse = new RentalResponse("Rental updated !");
  
      // Retourne l'objet RentalResponse dans la réponse
      return ResponseEntity.ok(rentalResponse);
  }
  

}
