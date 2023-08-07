package com.openclassrooms.chatop;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SecurityRequirement(name = "bearerAuth")
@SpringBootApplication
public class ChatopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatopApplication.class, args);
	}

}


