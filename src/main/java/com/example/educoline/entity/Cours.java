package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "cours")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String niveau;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime heureDebut;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime heureFin;

    @Column
    private String emploi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "classes", "cours", "matieresEnseignees", "notes", "absences"})
    private Enseignant enseignant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "enseignants", "etudiants", "cours"})
    private Classe classe;

    @ManyToMany
    @JoinTable(
            name = "etudiant_cours",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "etudiant_id")
    )
    @JsonIgnoreProperties({"cours", "notes", "absences", "classe"})
    private Set<Etudiant> etudiants = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Note> notes = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Absence> absences = new HashSet<>();

    @Transient
    private Integer nombreMaxAbsences = 0;

    // Constructeurs
    public Cours() {}

    public Cours(String nom, String description, String niveau, LocalTime heureDebut, LocalTime heureFin) {
        this.nom = nom;
        this.description = description;
        this.niveau = niveau;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public String getEmploi() {
        return emploi;
    }

    public void setEmploi(String emploi) {
        this.emploi = emploi;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Set<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setEtudiants(Set<Etudiant> etudiants) {
        this.etudiants = etudiants;
    }

    public Set<Note> getNotes() {
        return notes;
    }

    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    public Set<Absence> getAbsences() {
        return absences;
    }

    public void setAbsences(Set<Absence> absences) {
        this.absences = absences;
    }

    public Integer getNombreMaxAbsences() {
        return nombreMaxAbsences;
    }

    public void setNombreMaxAbsences(Integer nombreMaxAbsences) {
        this.nombreMaxAbsences = nombreMaxAbsences;
    }

    // Méthodes utilitaires
    public void addEtudiant(Etudiant etudiant) {
        this.etudiants.add(etudiant);
        etudiant.getCours().add(this);
    }

    public void removeEtudiant(Etudiant etudiant) {
        this.etudiants.remove(etudiant);
        etudiant.getCours().remove(this);
    }

    public void addNote(Note note) {
        this.notes.add(note);
        note.setCours(this);
    }

    public void removeNote(Note note) {
        this.notes.remove(note);
        note.setCours(null);
    }

    public void addAbsence(Absence absence) {
        this.absences.add(absence);
        absence.setCours(this);
    }

    public void removeAbsence(Absence absence) {
        this.absences.remove(absence);
        absence.setCours(null);
    }

    // equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cours cours = (Cours) o;
        return Objects.equals(id, cours.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", niveau='" + niveau + '\'' +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                '}';
    }

    public Object getMatiere() {
        return null;
    }
}
