package com.openclassrooms.chatop.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data // Génère les getters, les setters et d'autres méthodes utiles avec Lombok
@NoArgsConstructor
public class Messages {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Getter @Setter private Integer id;

  @Column(nullable = false)
  @Getter @Setter private Integer rental_id;

  @Column(nullable = false)
  @Getter @Setter private Integer user_id;

  @Column(nullable = false)
  @Getter @Setter private String message;

  @Column(nullable = false)
  @Getter @Setter private Timestamp created_at;

  @Column(nullable = false)
  @Getter @Setter private Timestamp updated_at;
}
