package com.example.educoline.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "etudiants")
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String status;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private Set<Note> notes = new HashSet<>();

    @ManyToMany(mappedBy = "etudiants")
    private Set<Cours> cours = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    // Constructeurs
    public Etudiant() {}

    public Etudiant(String nom, String prenom, String email, String status) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.status = status;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }

    public Set<Note> getNotes() { return notes; }
    public void setNotes(Set<Note> notes) { this.notes = notes; }

    // Important : on expose cours comme une List pour faciliter le JSON,
    // mais en interne on garde un Set
    public List<Cours> getCours() {
        return new ArrayList<>(cours);
    }
    public void setCours(Set<Cours> cours) {
        this.cours = cours;
    }

    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }

    // Méthodes utilitaires
    public void addNote(Note note) {
        notes.add(note);
        note.setEtudiant(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setEtudiant(null);
    }

    public void addCours(Cours cours) {
        this.cours.add(cours);
        cours.getEtudiants().add(this);
    }

    public void removeCours(Cours cours) {
        this.cours.remove(cours);
        cours.getEtudiants().remove(this);
    }
}
