package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.model.Messages;
import com.openclassrooms.chatop.repository.MessagesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MessagesService {

    @Autowired
    private final MessagesRepository messagesRepository;

    @Transactional
    public Optional<Messages> loadMessageById(Integer id) {
        return messagesRepository.findById(id);
    }

    @Transactional
    public Messages createMessage(Messages message) {
        return messagesRepository.save(message);
    }

}
