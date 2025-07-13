package com.example.educoline.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@Entity
@Table(name = "reunions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"enseignant", "createdAt", "updatedAt"})
@JsonIdentityInfo(  // Solution pour éviter les références circulaires
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 100, message = "Le sujet ne doit pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String sujet;

    @NotNull(message = "La date et heure sont obligatoires")
    @Future(message = "La date doit être dans le futur")
    @Column(nullable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateHeure;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 100, message = "Le lieu ne doit pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String lieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;  // Pas de @JsonBackReference

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Méthodes métiers
    public boolean isPassee() {
        return LocalDateTime.now().isAfter(this.dateHeure);
    }

    public boolean isAVenir() {
        return LocalDateTime.now().isBefore(this.dateHeure);
    }

    // Validation avant persistance
    @PrePersist
    @PreUpdate
    private void validate() {
        if (this.dateHeure != null && this.dateHeure.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("La réunion doit être programmée dans le futur");
        }
    }

    // Builder personnalisé pour plus de flexibilité
    public static ReunionBuilder builder() {
        return new CustomReunionBuilder();
    }

    private static class CustomReunionBuilder extends ReunionBuilder {
        @Override
        public Reunion build() {
            Reunion reunion = super.build();
            if (reunion.getDateHeure() == null) {
                throw new IllegalStateException("La date/heure est obligatoire");
            }
            return reunion;
        }
    }
}