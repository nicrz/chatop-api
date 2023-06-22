package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.model.Rentals;
import com.openclassrooms.chatop.repository.RentalsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RentalsService {

    @Autowired
    private final RentalsRepository rentalsRepository;

    @Transactional
    public Optional<Rentals> loadRentalById(Integer id) {
        return rentalsRepository.findById(id);
    }

}
