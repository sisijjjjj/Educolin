package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classes")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la classe est obligatoire")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Le niveau est obligatoire")
    @Column(nullable = false)
    private String niveau = "Non spécifié";

    @NotNull
    @Column(nullable = false)
    private Double moyenneGenerale = 0.0;

    @NotNull
    @Column(nullable = false)
    private Integer nombreAbsences = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "classe_enseignant",
            joinColumns = @JoinColumn(name = "classe_id"),
            inverseJoinColumns = @JoinColumn(name = "enseignant_id")
    )
    @JsonIgnoreProperties("classes") // Évite la récursion infinie
    private Set<Enseignant> enseignants = new HashSet<>();

    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // On ignore les étudiants dans la réponse par défaut
    private Set<Etudiant> etudiants = new HashSet<>();

    @Version
    @JsonIgnore
    private Long version;

    // Constructeurs
    public Classe() {
        this.moyenneGenerale = 0.0;
        this.nombreAbsences = 0;
    }

    public Classe(String name, String niveau) {
        this();
        this.name = name;
        this.niveau = niveau;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = (niveau == null || niveau.trim().isEmpty()) ? "Non spécifié" : niveau.trim();
    }

    public Double getMoyenneGenerale() {
        return moyenneGenerale;
    }

    public void setMoyenneGenerale(Double moyenneGenerale) {
        this.moyenneGenerale = moyenneGenerale != null ? moyenneGenerale : 0.0;
    }

    public Integer getNombreAbsences() {
        return nombreAbsences;
    }

    public void setNombreAbsences(Integer nombreAbsences) {
        this.nombreAbsences = nombreAbsences != null ? nombreAbsences : 0;
    }

    public Set<Enseignant> getEnseignants() {
        return enseignants;
    }

    public void setEnseignants(Set<Enseignant> enseignants) {
        this.enseignants = enseignants != null ? enseignants : new HashSet<>();
    }

    public Set<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setEtudiants(Set<Etudiant> etudiants) {
        this.etudiants = etudiants != null ? etudiants : new HashSet<>();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Méthodes utilitaires pour gérer les relations
    public void addEnseignant(Enseignant enseignant) {
        this.enseignants.add(enseignant);
        enseignant.getClasses().add(this);
    }

    public void removeEnseignant(Enseignant enseignant) {
        this.enseignants.remove(enseignant);
        enseignant.getClasses().remove(this);
    }

    public void addEtudiant(Etudiant etudiant) {
        this.etudiants.add(etudiant);
        etudiant.setClasse(this);
    }

    public void removeEtudiant(Etudiant etudiant) {
        this.etudiants.remove(etudiant);
        etudiant.setClasse(null);
    }

    // Méthodes métier
    public int getSeuilAbsences() {
        // Implémentez votre logique métier ici
        return 5; // Exemple
    }

    public double getSeuilMoyenne() {
        // Implémentez votre logique métier ici
        return 10.0; // Exemple
    }

    @Override
    public String toString() {
        return "Classe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", niveau='" + niveau + '\'' +
                ", moyenneGenerale=" + moyenneGenerale +
                ", nombreAbsences=" + nombreAbsences +
                '}';
    }

    // Equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classe)) return false;
        return id != null && id.equals(((Classe) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Etudiant getMatiere() {
        return null;
    }
}