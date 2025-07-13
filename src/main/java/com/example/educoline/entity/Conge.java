package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "conges")
public class Conge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeConge type;

    @NotBlank(message = "Le motif est obligatoire")
    @Size(max = 2000, message = "Le motif ne doit pas dépasser 2000 caractères")
    @Column(nullable = false, length = 2000)
    private String motif;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutConge statut = StatutConge.EN_ATTENTE;

    @Size(max = 500, message = "Le motif de rejet ne doit pas dépasser 500 caractères")
    private String motifRejet;

    @Size(max = 500, message = "Le motif d'annulation ne doit pas dépasser 500 caractères")
    private String motifAnnulation;

    @Size(max = 500, message = "Le commentaire admin ne doit pas dépasser 500 caractères")
    private String commentaireAdmin;


    public Conge orElseThrow(Object leaveNotFound) {
        return null;
    }


    public enum TypeConge {
        ANNUEL, MALADIE, MATERNITE, PATERNITE, FORMATION
    }

    public enum StatutConge {
        EN_ATTENTE, APPROUVE, REJETE, ANNULE;

        @JsonCreator
        public static StatutConge fromString(String value) {
            if (value == null) return null;
            try {
                return StatutConge.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Statut de congé invalide: " + value);
            }
        }
    }

    public Conge() {}

    public Conge(TypeConge type, String motif, LocalDate dateDebut, LocalDate dateFin, Enseignant enseignant) {
        this.type = type;
        this.motif = motif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.enseignant = enseignant;
    }

    public void approuver() {
        this.statut = StatutConge.APPROUVE;
        this.motifRejet = null;
    }

    public void rejeter(String raison) {
        if (raison == null || raison.trim().isEmpty()) {
            throw new IllegalArgumentException("Un motif de rejet est requis");
        }
        this.motifRejet = raison;
        this.statut = StatutConge.REJETE;
    }

    public void annuler(String motif) {
        if (motif == null || motif.trim().isEmpty()) {
            throw new IllegalArgumentException("Un motif d'annulation est requis");
        }
        this.motifAnnulation = motif;
        this.statut = StatutConge.ANNULE;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeConge getType() {
        return type;
    }

    public void setType(TypeConge type) {
        this.type = type;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        if (dateFin != null && dateDebut.isAfter(dateFin)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        if (dateDebut != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        this.dateFin = dateFin;
    }

    public StatutConge getStatut() {
        return statut;
    }

    public void setStatut(StatutConge statut) {
        this.statut = statut;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public void setMotifAnnulation(String motifAnnulation) {
        this.motifAnnulation = motifAnnulation;
    }

    public String getCommentaireAdmin() {
        return commentaireAdmin;
    }

    public void setCommentaireAdmin(String commentaireAdmin) {
        this.commentaireAdmin = commentaireAdmin;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public static TypeConge convertirType(String type) {
        try {
            return TypeConge.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type de congé invalide: " + type);
        }
    }
    // In Conge.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    @JsonIgnore  // Add this instead of @JsonBackReference
    private Enseignant enseignant;

    @Override
    public String toString() {
        return "Conge{" +
                "id=" + id +
                ", type=" + type +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut=" + statut +
                '}';
    }
}