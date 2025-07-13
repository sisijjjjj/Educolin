package com.example.educoline.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "etudiants")

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "date_naissance")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;
    @JsonIgnore
    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("etudiant-absence")
    private List<Absence> absences = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "deleted=false")
    @JsonManagedReference("etudiant-note")
    private Set<Note> notes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private StatusEtudiant status = StatusEtudiant.ACTIF;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id")
    @JsonBackReference("classe-etudiant")
    private Classe classe;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    @JsonBackReference("enseignant-etudiant")
    private Enseignant enseignant;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "cours_etudiant",
            joinColumns = @JoinColumn(name = "etudiant_id"),
            inverseJoinColumns = @JoinColumn(name = "cours_id"),
            foreignKey = @ForeignKey(
                    name = "FK_etudiant_courses",
                    foreignKeyDefinition = "FOREIGN KEY (etudiant_id) REFERENCES etudiants(id) ON DELETE CASCADE"
            )
    )
    @JsonIgnore
    @JsonIgnoreProperties("etudiants")
    private Set<Cours> cours = new HashSet<>();

    @Column(nullable = false)
    private boolean eliminated = false;

    @ElementCollection
    @CollectionTable(name = "etudiant_notes_tp", joinColumns = @JoinColumn(name = "etudiant_id"))
    @MapKeyJoinColumn(name = "cours_id")
    @Column(name = "note")
    @JsonIgnore
    private Map<Cours, Double> notesTP = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "etudiant_notes_examen", joinColumns = @JoinColumn(name = "etudiant_id"))
    @MapKeyJoinColumn(name = "cours_id")
    @Column(name = "note")
    @JsonIgnore
    private Map<Cours, Double> notesExamen = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "etudiant_moyennes", joinColumns = @JoinColumn(name = "etudiant_id"))
    @MapKeyJoinColumn(name = "cours_id")
    @Column(name = "moyenne")
    @JsonIgnore
    private Map<Cours, Double> moyennes = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "etudiant_absences", joinColumns = @JoinColumn(name = "etudiant_id"))
    @MapKeyJoinColumn(name = "cours_id")
    @Column(name = "nombre")
    @JsonIgnore
    private Map<Cours, Integer> absencesParMatiere = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "etudiant_elimination", joinColumns = @JoinColumn(name = "etudiant_id"))
    @MapKeyJoinColumn(name = "cours_id")
    @Column(name = "est_elimine")
    @JsonIgnore
    private Map<Cours, Boolean> eliminationParMatiere = new HashMap<>();

    @Column(nullable = false)
    private boolean deleted = false;

    // Constructeurs
    public Etudiant() {
    }

    public Etudiant(String nom, String prenom, String email, LocalDate dateNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.dateNaissance = dateNaissance;
    }

    // Getters et Setters
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public StatusEtudiant getStatus() {
        return status;
    }

    public void setStatus(StatusEtudiant status) {
        this.status = status;
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

    public List<Absence> getAbsences() {
        return absences;
    }

    public void setAbsences(List<Absence> absences) {
        this.absences = absences;
    }

    public Set<Cours> getCours() {
        return cours;
    }

    public void setCours(Set<Cours> cours) {
        this.cours = cours;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public Map<Cours, Double> getNotesTP() {
        return notesTP;
    }

    public void setNotesTP(Map<Cours, Double> notesTP) {
        this.notesTP = notesTP;
    }

    public Map<Cours, Double> getNotesExamen() {
        return notesExamen;
    }

    public void setNotesExamen(Map<Cours, Double> notesExamen) {
        this.notesExamen = notesExamen;
    }

    public Map<Cours, Double> getMoyennes() {
        return moyennes;
    }

    public void setMoyennes(Map<Cours, Double> moyennes) {
        this.moyennes = moyennes;
    }

    public Map<Cours, Integer> getAbsencesParMatiere() {
        return absencesParMatiere;
    }

    public void setAbsencesParMatiere(Map<Cours, Integer> absencesParMatiere) {
        this.absencesParMatiere = absencesParMatiere;
    }

    public Map<Cours, Boolean> getEliminationParMatiere() {
        return eliminationParMatiere;
    }

    public void setEliminationParMatiere(Map<Cours, Boolean> eliminationParMatiere) {
        this.eliminationParMatiere = eliminationParMatiere;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Set<Note> getNotes() {
        return notes;
    }

    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    // Méthodes utilitaires
    @JsonProperty("nomComplet")
    public String getNomComplet() {
        return (prenom != null ? prenom + " " : "") + (nom != null ? nom : "");
    }

    @JsonProperty("moyenneGenerale")
    public Double getMoyenneGenerale() {
        if (moyennes.isEmpty()) return null;
        return moyennes.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    @JsonProperty("resultats")
    public List<Map<String, Object>> getResultatsPourAffichage() {
        List<Map<String, Object>> resultats = new ArrayList<>();
        for (Cours c : cours) {
            Map<String, Object> resultat = new HashMap<>();
            resultat.put("coursId", c.getId());
            resultat.put("coursNom", c.getNom());
            resultat.put("noteTP", notesTP.getOrDefault(c, 0.0));
            resultat.put("noteExamen", notesExamen.getOrDefault(c, 0.0));
            resultat.put("moyenne", moyennes.getOrDefault(c, 0.0));
            resultat.put("absences", absencesParMatiere.getOrDefault(c, 0));
            resultat.put("elimine", eliminationParMatiere.getOrDefault(c, false));
            resultats.add(resultat);
        }
        return resultats;
    }

    // Méthodes pour gérer les relations
    public void addNote(Note note) {
        notes.add(note);
        note.setEtudiant(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setEtudiant(null);
    }

    public void addAbsence(Absence absence) {
        absences.add(absence);
        absence.setEtudiant(this);
        incrementerAbsence(absence.getCours());
    }

    public void removeAbsence(Absence absence) {
        absences.remove(absence);
        absence.setEtudiant(null);
        decrementerAbsence(absence.getCours());
    }

    public void addCours(Cours cours) {
        this.cours.add(cours);
        cours.getEtudiants().add(this);
        initializeMapsForCours(cours);
    }

    public void removeCours(Cours cours) {
        this.cours.remove(cours);
        cours.getEtudiants().remove(this);
        cleanMapsForCours(cours);
    }

    @PreRemove
    private void preRemove() {
        for (Cours c : new HashSet<>(cours)) {
            removeCours(c);
        }
        notesTP.clear();
        notesExamen.clear();
        moyennes.clear();
        absencesParMatiere.clear();
        eliminationParMatiere.clear();
    }

    private void initializeMapsForCours(Cours cours) {
        notesTP.putIfAbsent(cours, 0.0);
        notesExamen.putIfAbsent(cours, 0.0);
        moyennes.putIfAbsent(cours, 0.0);
        absencesParMatiere.putIfAbsent(cours, 0);
        eliminationParMatiere.putIfAbsent(cours, false);
    }

    private void cleanMapsForCours(Cours cours) {
        notesTP.remove(cours);
        notesExamen.remove(cours);
        moyennes.remove(cours);
        absencesParMatiere.remove(cours);
        eliminationParMatiere.remove(cours);
    }

    // Méthodes pour gérer les notes et absences
    public void ajouterNoteTP(Cours cours, double note) {
        validateNote(note);
        notesTP.put(cours, note);
        calculerMoyenne(cours);
        verifierElimination(cours);
    }

    public void ajouterNoteExamen(Cours cours, double note) {
        validateNote(note);
        notesExamen.put(cours, note);
        calculerMoyenne(cours);
        verifierElimination(cours);
    }

    private void validateNote(double note) {
        if (note < 0 || note > 20) {
            throw new IllegalArgumentException("La note doit être entre 0 et 20");
        }
    }

    private void calculerMoyenne(Cours cours) {
        Double tp = notesTP.get(cours);
        Double exam = notesExamen.get(cours);

        if (tp != null && exam != null) {
            double moyenne = (tp * 0.4) + (exam * 0.6);
            moyennes.put(cours, moyenne);
        }
    }

    public void incrementerAbsence(Cours cours) {
        int nbAbsences = absencesParMatiere.getOrDefault(cours, 0) + 1;
        absencesParMatiere.put(cours, nbAbsences);
        verifierElimination(cours);
    }

    public void decrementerAbsence(Cours cours) {
        int nbAbsences = Math.max(absencesParMatiere.getOrDefault(cours, 0) - 1, 0);
        absencesParMatiere.put(cours, nbAbsences);
        verifierElimination(cours);
    }

    private void verifierElimination(Cours cours) {
        boolean elimine = (moyennes.getOrDefault(cours, 0.0) < 10) ||
                (absencesParMatiere.getOrDefault(cours, 0) > 3);
        eliminationParMatiere.put(cours, elimine);
        updateGlobalEliminationStatus();
    }

    private void updateGlobalEliminationStatus() {
        this.eliminated = eliminationParMatiere.values().stream()
                .anyMatch(Boolean::booleanValue);
    }

    // Méthodes pour la sérialisation JSON des relations
    @JsonProperty("classeInfo")
    public Map<String, Object> getClasseInfo() {
        if (classe == null) return null;
        Map<String, Object> info = new HashMap<>();
        info.put("id", classe.getId());
        info.put("nom", classe.getName());
        return info;
    }

    @JsonProperty("enseignantInfo")
    public Map<String, Object> getEnseignantInfo() {
        if (enseignant == null) return null;
        Map<String, Object> info = new HashMap<>();
        info.put("id", enseignant.getId());
        info.put("nomComplet", enseignant.getNom() + " " + enseignant.getPrenom());
        return info;
    }

    // Méthodes utilitaires JSON
    @JsonIgnore
    public boolean estElimineDansMatiere(Cours cours) {
        return eliminationParMatiere.getOrDefault(cours, false);
    }

    @JsonIgnore
    public Double getMoyennePourMatiere(Cours cours) {
        return moyennes.get(cours);
    }

    @JsonIgnore
    public Integer getNombreAbsencesPourMatiere(Cours cours) {
        return absencesParMatiere.getOrDefault(cours, 0);
    }

    @JsonIgnore
    public Double getNoteTPPourMatiere(Cours cours) {
        return notesTP.get(cours);
    }

    @JsonIgnore
    public Double getNoteExamenPourMatiere(Cours cours) {
        return notesExamen.get(cours);
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Etudiant)) return false;
        Etudiant etudiant = (Etudiant) o;
        return id != null && id.equals(etudiant.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Etudiant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }

    public Object getAdmins() {
        return null;
    }

    // Enum StatusEtudiant
    public enum StatusEtudiant {
        ACTIF, INACTIF, DIPLOME;

        public static StatusEtudiant fromString(String key) {
            if (key == null) return null;
            switch (key.trim().toLowerCase()) {
                case "actif": case "active": return ACTIF;
                case "inactif": case "inactive": return INACTIF;
                case "diplome": case "graduated": return DIPLOME;
                default: throw new IllegalArgumentException("Invalid status: " + key);
            }
        }
    }
}