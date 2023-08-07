package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.responses.AuthSuccess;
import com.openclassrooms.chatop.security.JwtTokenProvider;
import com.openclassrooms.chatop.service.UserService;
import com.openclassrooms.dto.LoginRequest;
import com.openclassrooms.dto.RegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping(path = "/")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Autowired
  private AuthenticationProvider authenticationProvider;
  
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @PostMapping(path = "/auth/register")
  @Operation(summary = "User registration")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "JWT Token"),
      @ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<AuthSuccess> addNewUser(@RequestBody RegistrationRequest registrationRequest) {
    try {
        // Appelle la méthode du service pour ajouter un nouvel utilisateur
        User newUser = userService.addNewUser(registrationRequest);

        // Authentifie l'utilisateur après son enregistrement
        Authentication authentication = authenticationProvider.authenticate(
            new UsernamePasswordAuthenticationToken(newUser.getEmail(), registrationRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Génère le token JWT
        String token = jwtTokenProvider.generateToken(authentication);

        // Crée l'objet AuthSuccess avec le token
        AuthSuccess authSuccess = new AuthSuccess(token);

        // Retourne l'objet AuthSuccess dans la réponse
        return ResponseEntity.ok(authSuccess);
    } catch (Exception e) {
        // Retourne une réponse 400 s'il y a une erreur lors de l'ajout
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
  

  @PostMapping("/auth/login")
  @Operation(summary = "User login")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "JWT Token"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<AuthSuccess> login(@RequestBody LoginRequest loginRequest) {
    try {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Appelle la méthode du service pour vérifier les informations d'identification
        Authentication authentication = userService.login(email, password);

        // Génère le token JWT
        String token = jwtTokenProvider.generateToken(authentication);

        // Crée l'objet AuthSuccess avec le token
        AuthSuccess authSuccess = new AuthSuccess(token);

        // Retourne l'objet AuthSuccess dans la réponse
        return ResponseEntity.ok(authSuccess);
    } catch (AuthenticationException e) {
        // Retourne une réponse 401 Unauthorized en cas d'erreur
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

@GetMapping("/auth/me")
@Operation(summary = "Return information about the logged user")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
  public ResponseEntity<User> getUserInfo() {
    // Récupère l'objet Authentication
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null) {
        // Récupère l'identifiant de l'objet Authentication
        String email = authentication.getName();

        // Appelle la méthode du service pour récupérer les informations de l'utilisateur
        User user = userService.getUserByEmail(email);

        if (user != null) {
            // Renvoie les informations de l'utilisateur dans la réponse
            return ResponseEntity.ok(user);
        }
    }

    // Renvoie une erreur 401 Unauthorized si l'authentification n'est pas fournie
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}

@GetMapping("/user/{id}")
@Operation(summary = "Get user information by ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "User not found")
})
  public ResponseEntity<User> getUserById(@PathVariable Integer id) {
      // Récupère l'objet Authentication
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  
      if (authentication != null && authentication.isAuthenticated()) {
          // Recherche l'utilisateur par son ID
          Optional<User> optionalUser = userService.getUserById(id);
  
          if (optionalUser.isPresent()) {
              // Si l'utilisateur existe, on renvoie les informations de l'utilisateur dans la réponse
              return ResponseEntity.ok(optionalUser.get());
          }
      }
      // Renvoie une réponse 401 Unauthorized si l'authentificaton n'est pas fournie
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }


}
