package com.openclassrooms.chatop.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.openclassrooms.dto.RegistrationRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

import com.openclassrooms.chatop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import com.openclassrooms.chatop.model.User;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email); 

        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé : " + email);
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User addNewUser(RegistrationRequest registrationRequest) {
        String name = registrationRequest.getName();
        String email = registrationRequest.getEmail();
        String password = registrationRequest.getPassword();

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        Timestamp now = Timestamp.from(Instant.now());
        user.setCreated_at(now);
        user.setUpdated_at(now);
        return userRepository.save(user);
    }

    public Authentication login(String email, String password) {
        // Vérification des informations d'identification de l'utilisateur
        Authentication authentication = authenticationProvider.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }


}
