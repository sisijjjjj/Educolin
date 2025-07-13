package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "absences")
@SQLDelete(sql = "UPDATE absences SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")


@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler"})
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_absence_etudiant",
                    foreignKeyDefinition = "FOREIGN KEY (etudiant_id) REFERENCES etudiants(id) ON DELETE CASCADE"
            ))
    private Etudiant etudiant;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Enseignant enseignant;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Cours cours;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean justifiee = false;

    @Column(length = 500)
    private String motif;

    @Column(nullable = false)
    private boolean deleted = false;

    // Constructeurs
    public Absence() {
    }

    public Absence(Etudiant etudiant, Enseignant enseignant, Cours cours, LocalDate date) {
        this.etudiant = etudiant;
        this.enseignant = enseignant;
        this.cours = cours;
        this.date = date;
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isJustifiee() {
        return justifiee;
    }

    public void setJustifiee(boolean justifiee) {
        this.justifiee = justifiee;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Méthodes métier
    public void justifier(String motif) {
        this.justifiee = true;
        this.motif = motif;
    }

    public void annulerJustification() {
        this.justifiee = false;
        this.motif = null;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Absence absence = (Absence) o;
        return Objects.equals(id, absence.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Absence{" +
                "id=" + id +
                ", etudiantId=" + (etudiant != null ? etudiant.getId() : null) +
                ", enseignantId=" + (enseignant != null ? enseignant.getId() : null) +
                ", coursId=" + (cours != null ? cours.getId() : null) +
                ", date=" + date +
                ", justifiee=" + justifiee +
                ", motif='" + motif + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}