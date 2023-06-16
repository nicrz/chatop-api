package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.model.Messages;
import com.openclassrooms.chatop.repository.MessagesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;

    @Transactional
    public Optional<Messages> loadMessageById(Integer id) {
        return messagesRepository.findById(id);
    }

}
