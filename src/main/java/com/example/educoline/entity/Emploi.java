package com.example.educoline.entity;

import jakarta.persistence.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emplois")
public class Emploi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", referencedColumnName = "id")
    private Classe classe;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", referencedColumnName = "id")
    private Enseignant enseignant;

    @OneToMany(mappedBy = "emploi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cours> cours = new ArrayList<>(); // Initialisation de la liste

    // Constructeurs
    public Emploi() {
    }

    public Emploi(Classe classe, Enseignant enseignant) {
        this.classe = classe;
        this.enseignant = enseignant;
    }



    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public List<Cours> getCours() {
        return cours;
    }

    public void setCours(List<Cours> cours) {
        if (cours != null) {
            this.cours.clear();
            this.cours.addAll(cours);
        } else {
            this.cours.clear();
        }
    }

    public boolean isEmpty() {
        return false;
    }


}