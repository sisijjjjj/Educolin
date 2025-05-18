package com.example.educoline.entity;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletResponse;

@Entity
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId; // ID de l'étudiant qui est absent

    private Long courseId;  // ID du cours auquel l'absence est liée

    private int numberOfAbsences;  // Nombre d'absences pour cet étudiant dans le cours

    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;  // L'enseignant responsable de ce cours

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public int getNumberOfAbsences() {
        return numberOfAbsences;
    }
    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;



    public void setNumberOfAbsences(int numberOfAbsences) {
        this.numberOfAbsences = numberOfAbsences;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public HttpServletResponse getEtudiant() {
        return null;
    }
}
