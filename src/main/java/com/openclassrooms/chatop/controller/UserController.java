package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.service.UserService;
import com.openclassrooms.chatop.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // This means that this class is a Controller
@RequestMapping(path="/") // This means URL's start with /demo (after Application path)
public class UserController {
  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private AuthenticationProvider authenticationProvider;
  
  @Autowired
  private JwtTokenProvider jwtTokenProvider;
  
  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping(path="/auth/register") // Map ONLY POST Requests
  public @ResponseBody String addNewUser (@RequestParam String name
      , @RequestParam String email , @RequestParam String password) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request

    User u = new User();
    u.setName(name);
    u.setEmail(email);
    u.setPassword(passwordEncoder.encode(password));
    Timestamp now = Timestamp.from(Instant.now());
    u.setCreated_at(now);
    u.setUpdated_at(now);
    userRepository.save(u);
    return "Enregistré";
  }

  @PostMapping("/auth/login")
  public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
      // Vérification des informations d'identification de l'utilisateur
      Authentication authentication = authenticationProvider.authenticate(
              new UsernamePasswordAuthenticationToken(email, password)
      );
  
      // Génération du token
      String token = jwtTokenProvider.generateToken(authentication);
  
      // Retourne le token JWT dans la réponse
      return ResponseEntity.ok(token);
  }

  @GetMapping("/auth/me")
  public ResponseEntity<User> getUserInfo() {
      // Récupére l'objet Authentication à partir du contexte de sécurité
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      // Récupére le nom d'utilisateur de l'objet Authentication
      String email = authentication.getName();

      // Recherche l'utilisateur dans votre repository par son nom d'utilisateur
      User user = userRepository.findByEmail(email);

      if (user != null) {
          // Renvoie les informations de l'utilisateur dans la réponse
          return ResponseEntity.ok(user);
      } else {
          // Gére le cas où l'utilisateur n'est pas trouvé
          return ResponseEntity.noContent().build();
      }

  }

  @GetMapping(path="/all")
  public @ResponseBody Iterable<User> getAllUsers() {
    // This returns a JSON or XML with the users
    return userRepository.findAll();
  }
}
