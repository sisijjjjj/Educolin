package com.example.educoline.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double tp;

    private Double exam;

    private Integer absences;

    private Boolean retakeSession = false;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")

    private Etudiant etudiant;
    @ManyToOne
    @JoinColumn(name = "enseignant_id") // ou le nom réel de votre colonne
    private Enseignant enseignant;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    private Cours cours;


    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getTp() { return tp; }
    public void setTp(Double tp) { this.tp = tp; }

    public Double getExam() { return exam; }
    public void setExam(Double exam) { this.exam = exam; }

    public Integer getAbsences() { return absences; }
    public void setAbsences(Integer absences) { this.absences = absences; }

    public Boolean getRetakeSession() { return retakeSession; }
    public void setRetakeSession(Boolean retakeSession) { this.retakeSession = retakeSession; }

    public Etudiant getEtudiant() { return etudiant; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }
}
