package com.openclassrooms.chatop.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.openclassrooms.chatop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import com.openclassrooms.chatop.model.User;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email); 

        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé : " + email);
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }


}
