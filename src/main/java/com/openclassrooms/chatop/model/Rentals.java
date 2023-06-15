package com.openclassrooms.chatop.model;

import java.sql.Timestamp;
import java.util.List;

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
public class Rentals {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Getter @Setter private Integer id;

  @Column(nullable = false)
  @Getter @Setter private String name;

  @Column(nullable = false)
  @Getter @Setter private Integer surface; 

  @Column(nullable = false)
  @Getter @Setter private Integer price; 

  @Column(nullable = false)
  @Getter @Setter private String picture;

  @Column(nullable = false)
  @Getter @Setter private String description;

  @Column(nullable = false)
  @Getter @Setter private Integer owner_id;

  @Column(nullable = false)
  @Getter @Setter private Timestamp created_at;

  @Column(nullable = false)
  @Getter @Setter private Timestamp updated_at;
}