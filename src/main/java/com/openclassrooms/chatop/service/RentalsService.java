package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.model.User;
import com.openclassrooms.chatop.repository.RentalsRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.dto.RentalRequest;

import java.io.File;
import java.util.Optional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
public class RentalsService {

    private final RentalsRepository rentalsRepository;
    private final UserRepository userRepository;

    public RentalsService(RentalsRepository rentalsRepository, UserRepository userRepository) {
        this.rentalsRepository = rentalsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Iterable<Rentals> getAllRentals() {
        return rentalsRepository.findAll();
    }

    @Transactional
    public Optional<Rentals> getRentalById(Integer id) {
        return rentalsRepository.findById(id);
    }

    @Transactional
    public Rentals saveRental(Rentals rental) {
        return rentalsRepository.save(rental);
    }

    @Transactional
    public Rentals createRental(RentalRequest rentalRequest, MultipartFile picture, Authentication authentication) throws IOException {
        // Définit le dossier où le fichier sera stocké
        String fileDirectory = System.getProperty("user.dir") + "/src/main/resources/static/images/";

        // Crée le chemin d'accès complet pour le fichier
        String fileName = picture.getOriginalFilename();
        Path destination = Path.of(fileDirectory + fileName);

        // Copie le fichier vers le répertoire de destination
        Files.copy(picture.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Génère l'URL complet pour accéder au fichier
        String fileUrl = "http://localhost:3000/images/" + fileName;

        // Crée un nouvel objet Rentals avec les propriétés

        Rentals newRental = new Rentals();
        newRental.setName(rentalRequest.getName());
        newRental.setSurface(rentalRequest.getSurface());
        newRental.setPrice(rentalRequest.getPrice());
        newRental.setPicture(fileUrl);
        newRental.setDescription(rentalRequest.getDescription());
  
        // Récup l'ID de l'utilisateur authentifié
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        Integer owner_id = user.getId();
        newRental.setOwner_id(owner_id);
        newRental.setCreated_at(new Timestamp(System.currentTimeMillis()));
        newRental.setUpdated_at(new Timestamp(System.currentTimeMillis()));
  
        // Enregistre l'objet Rentals dans la base de données
        rentalsRepository.save(newRental);
  
        return newRental;
}

}
