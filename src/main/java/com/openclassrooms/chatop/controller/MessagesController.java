package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.exception.NotFoundException;
import com.openclassrooms.chatop.model.Messages;
import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.MessagesRepository;
import com.openclassrooms.chatop.repository.RentalsRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.responses.MessageResponse;
import com.openclassrooms.chatop.service.MessagesService;
import com.openclassrooms.dto.MessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequestMapping(path="/")
@SecurityRequirement(name = "bearerAuth")
public class MessagesController {

  private final MessagesService messagesService;

  public MessagesController(MessagesService messagesService) {
      this.messagesService = messagesService;
  }

  @Autowired
  private MessagesRepository messagesRepository;

  @Autowired
  private RentalsRepository rentalsRepository;

  @Autowired
  private UserRepository userRepository;

  @PostMapping(path = "/messages")
  @Operation(summary = "Create a new message")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message sent with success"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageRequest messageRequest) {
      // Vérifie si le rental existe
      Optional<Rentals> optionalRental = rentalsRepository.findById(messageRequest.getRental_id());
      if (optionalRental.isEmpty()) {
          throw new NotFoundException("Rental not found");
      }
  
      // Récupère l'ID de l'utilisateur authentifié
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String email = authentication.getName();
      User user = userRepository.findByEmail(email);
      Integer userId = user.getId();
  
      // Crée un nouvel objet Messages avec les valeurs du DTO
      Messages newMessage = new Messages();
      newMessage.setRental_id(messageRequest.getRental_id());
      newMessage.setUser_id(userId);
      newMessage.setMessage(messageRequest.getMessage());
      Timestamp now = Timestamp.from(Instant.now());
      newMessage.setCreated_at(now);
      newMessage.setUpdated_at(now);
  
      // Enregistre le message dans la base de données
      Messages savedMessage = messagesRepository.save(newMessage);
  
      // Crée un objet MessageResponse avec le message approprié
      MessageResponse messageResponse = new MessageResponse("Message sent with success");
  
      // Retourne l'objet MessageResponse dans la réponse
      return ResponseEntity.ok(messageResponse);
  }

}

