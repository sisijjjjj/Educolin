package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "enseignants",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"),
        indexes = {
                @Index(name = "idx_enseignant_email", columnList = "email"),
                @Index(name = "idx_enseignant_nom_prenom", columnList = "nom, prenom")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "reunions", "notes"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Enseignant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Version
    @JsonIgnore
    private Integer version;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false, length = 50)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false, length = 50)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @NotBlank(message = "Le diplôme est obligatoire")
    @Column(nullable = false, length = 100)
    private String diplome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEnseignant status = StatusEnseignant.ACTIF;

    @NotNull(message = "Le nombre d'années d'expérience est obligatoire")
    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    @Column(name = "nb_annee_experience", nullable = false)
    private Integer nbAnneeExperience;

    @NotNull(message = "Le nombre de classes est obligatoire")
    @Min(value = 0, message = "Le nombre de classes ne peut pas être négatif")
    @Column(name = "nb_classe", nullable = false)
    private Integer nbClasse = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "enseignant_matieres",
            joinColumns = @JoinColumn(name = "enseignant_id"),
            foreignKey = @ForeignKey(name = "fk_enseignant_matieres"))
    @Column(name = "matiere", length = 50)
    @Fetch(FetchMode.SUBSELECT)
    private Set<String> matieresEnseignees = new HashSet<>();

    @OneToMany(mappedBy = "enseignant",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)

    @JsonIgnore
    private List<Cours> cours = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "enseignant_classe",
            joinColumns = @JoinColumn(name = "enseignant_id",
                    foreignKey = @ForeignKey(name = "fk_enseignant_classe_enseignant")),
            inverseJoinColumns = @JoinColumn(name = "classe_id",
                    foreignKey = @ForeignKey(name = "fk_enseignant_classe_classe")))
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIgnoreProperties("enseignants") // Solution clé
    private Set<Classe> classes = new HashSet<>();

    @OneToMany(mappedBy = "enseignant",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reunion> reunions = new ArrayList<>();

    @OneToMany(mappedBy = "enseignant",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Etudiant> etudiants = new HashSet<>();



    @OneToMany(mappedBy = "enseignant",
            fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Note> notes = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void validate() {
        if (this.nbAnneeExperience == null) {
            this.nbAnneeExperience = 0;
        }
        if (this.nbClasse == null) {
            this.nbClasse = 0;
        }
        if (this.status == null) {
            this.status = StatusEnseignant.ACTIF;
        }
    }

    // Méthodes utilitaires
    public void addEtudiant(Etudiant etudiant) {
        if (etudiant != null && !this.etudiants.contains(etudiant)) {
            this.etudiants.add(etudiant);
            etudiant.setEnseignant(this);
        }
    }

    public void removeEtudiant(Etudiant etudiant) {
        if (etudiant != null && this.etudiants.contains(etudiant)) {
            this.etudiants.remove(etudiant);
            etudiant.setEnseignant(null);
        }
    }

    public void addClasse(Classe classe) {
        if (classe != null && !this.classes.contains(classe)) {
            this.classes.add(classe);
            classe.getEnseignants().add(this);
            this.nbClasse = this.classes.size();
        }
    }

    public void removeClasse(Classe classe) {
        if (classe != null && this.classes.contains(classe)) {
            this.classes.remove(classe);
            classe.getEnseignants().remove(this);
            this.nbClasse = this.classes.size();
        }
    }

    public void addMatiere(String matiere) {
        if (matiere != null && !matiere.isBlank()) {
            this.matieresEnseignees.add(matiere.trim());
        }
    }

    public void addCours(Cours cours) {
        if (cours != null && !this.cours.contains(cours)) {
            this.cours.add(cours);
            cours.setEnseignant(this);
        }
    }

    public void removeCours(Cours cours) {
        if (cours != null && this.cours.contains(cours)) {
            this.cours.remove(cours);
            cours.setEnseignant(null);
        }
    }

    public void addNote(Note note) {
        if (note != null && !this.notes.contains(note)) {
            this.notes.add(note);
            note.setEnseignant(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enseignant)) return false;
        Enseignant that = (Enseignant) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    public void setStatutConge(String statut) {
        this.status = StatusEnseignant.fromString(statut);
    }

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    public Object getSpecialite() {
        return null;
    }

    public enum StatusEnseignant {
        ACTIF, INACTIF, EN_CONGE;

        @JsonCreator
        public static StatusEnseignant fromString(String key) {
            if (key == null) return null;
            switch (key.trim().toLowerCase()) {
                case "actif":
                    return ACTIF;
                case "inactif":
                    return INACTIF;
                case "en_conge":
                case "en conge":
                    return EN_CONGE;
                default:
                    throw new IllegalArgumentException("Invalid status: " + key);
            }
        }

        public StatusEnseignant getStatus() {
            return null;
        }
    }
    // In Enseignant.java
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Add this instead of @JsonManagedReference
    private List<Conge> conges = new ArrayList<>();

}


