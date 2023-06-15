package com.openclassrooms.chatop.model;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data // Génère les getters, les setters et d'autres méthodes utiles avec Lombok
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Getter @Setter private Integer id;

  @Column(unique = true, nullable = false)
  @Getter @Setter private String email;

  @Size(min = 4, max = 255, message = "Minimum username length: 4 characters")
  @Column(unique = true, nullable = false)
  @Getter @Setter private String name;

  @Size(min = 8, message = "Minimum password length: 8 characters")
  @JsonIgnore
  @Getter @Setter private String password;

  @Column(nullable = false)
  @Getter @Setter private Timestamp created_at;

  @Column(nullable = false)
  @Getter @Setter private Timestamp updated_at;
}