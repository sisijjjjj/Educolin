package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double tp;

    @Column(nullable = false)
    private Double exam;

    @Column(nullable = false)
    private Integer absences;

    @Column(nullable = false)
    private Double moyenne;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etudiant_id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_note_to_etudiant",
                    foreignKeyDefinition = "FOREIGN KEY (etudiant_id) REFERENCES etudiants(id) ON DELETE CASCADE"
            ))
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;

    @PrePersist
    @PreUpdate
    public void calculateMoyenne() {
        if (tp != null && exam != null) {
            this.moyenne = (tp * 0.4) + (exam * 0.6);
        } else {
            throw new IllegalStateException("TP and Exam grades must be provided to calculate moyenne");
        }
    }

    public double getValeur() {
        return 0;
    }

    public String getDateCreation() {
        return "";
    }

    public void setDateCreation(LocalDate now) {
    }
}