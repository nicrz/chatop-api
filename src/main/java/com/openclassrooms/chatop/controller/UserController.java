package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.LoginRequest;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.service.UserService;
import com.openclassrooms.chatop.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller 
@RequestMapping(path="/") 
public class UserController {
  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private AuthenticationProvider authenticationProvider;
  
  @Autowired
  private JwtTokenProvider jwtTokenProvider;
  
  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping(path = "/auth/register")
  public ResponseEntity<String> addNewUser(@RequestParam String name,
                                           @RequestParam String email,
                                           @RequestParam String password) {
      try {
          User u = new User();
          u.setName(name);
          u.setEmail(email);
          u.setPassword(passwordEncoder.encode(password));
          Timestamp now = Timestamp.from(Instant.now());
          u.setCreated_at(now);
          u.setUpdated_at(now);
          userRepository.save(u);
          return ResponseEntity.ok("Compte enregistré avec succès");
      } catch (Exception e) {
          // Retourne une réponse 400 s'il y a une erreur lors de l'ajout
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
  }

  @PostMapping("/auth/login")
  public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
      try {
          String email = loginRequest.getEmail();
          String password = loginRequest.getPassword();
  
          // Vérification des informations d'identification de l'utilisateur
          Authentication authentication = authenticationProvider.authenticate(
                  new UsernamePasswordAuthenticationToken(email, password)
          );
  
          // Génération du token
          String token = jwtTokenProvider.generateToken(authentication);
  
          // Retourne le token JWT dans la réponse
          return ResponseEntity.ok(token);
      } catch (AuthenticationException e) {
          // Retourne une réponse 401 Unauthorized en cas d'erreur
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
  }

  @GetMapping("/auth/me")
  public ResponseEntity<User> getUserInfo() {
      // Récupère l'objet Authentication
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  
      if (authentication != null) {
          // Récupère l'identifiant' de l'objet Authentication
          String email = authentication.getName();
  
          // Recherche l'utilisateur par son e-mail
          User user = userRepository.findByEmail(email);
  
          if (user != null) {
              // Renvoie les informations de l'utilisateur dans la réponse
              return ResponseEntity.ok(user);
          }
      }
  
      // Renvoie une erreur 401 Unauthorized si l'authentification n'est pas fournie
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<User> getUserById(@PathVariable Integer id) {
      // Récupère l'objet Authentication
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  
      if (authentication != null && authentication.isAuthenticated()) {
          // Recherche l'utilisateur par son ID
          Optional<User> optionalUser = userRepository.findById(id);
  
          if (optionalUser.isPresent()) {
              // Si l'utilisateur existe, on renvoie les informations de l'utilisateur dans la réponse
              return ResponseEntity.ok(optionalUser.get());
          }
      }
  
      // Renvoie une réponse 401 Unauthorized si l'authentificaton n'est pas fournie
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @GetMapping(path="/all")
  public @ResponseBody Iterable<User> getAllUsers() {
    // Retourne la liste de tous les utilisateurs enregistrés
    return userRepository.findAll();
  }
}
