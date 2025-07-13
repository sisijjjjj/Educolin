package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final EnseignantRepository enseignantRepository;

    public NoteService(NoteRepository noteRepository,
                       EtudiantRepository etudiantRepository,
                       CoursRepository coursRepository,
                       EnseignantRepository enseignantRepository) {
        this.noteRepository = noteRepository;
        this.etudiantRepository = etudiantRepository;
        this.coursRepository = coursRepository;
        this.enseignantRepository = enseignantRepository;
    }

    // Créer une nouvelle note
    public Note createNote(Note note) {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");
        note.calculateMoyenne();
        return noteRepository.save(note);
    }

    // Ajouter une note à un étudiant pour un cours spécifique
    public Note addNoteToEtudiant(Long etudiantId, Long coursId, Note note) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        note.setEtudiant(etudiant);
        note.setCours(cours);
        note.calculateMoyenne();

        return noteRepository.save(note);
    }



    // Récupérer une note par son ID
    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    // Récupérer les notes d'un enseignant
    public List<Note> getNotesByTeacher(Long enseignantId) {
        return noteRepository.findByEnseignantId(enseignantId);
    }

    // Récupérer les notes d'un étudiant
    public List<Note> getNotesByStudent(Long etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId);
    }

    // Récupérer les notes d'un étudiant (alias)
    public List<Note> getNotesByEtudiantId(Long etudiantId) {
        return getNotesByStudent(etudiantId);
    }

    // Récupérer les notes pour un cours
    public List<Note> getNotesByCourse(Long coursId) {
        return noteRepository.findByCoursId(coursId);
    }

    // Récupérer les notes pour un cours (alias)
    public List<Note> getNotesByCoursId(Long coursId) {
        return getNotesByCourse(coursId);
    }

    // Mettre à jour une note
    public Optional<Note> updateNote(Long id, Note noteDetails) {
        return noteRepository.findById(id).map(existingNote -> {
            if (noteDetails.getTp() != null) existingNote.setTp(noteDetails.getTp());
            if (noteDetails.getExam() != null) existingNote.setExam(noteDetails.getExam());
            existingNote.calculateMoyenne();
            return noteRepository.save(existingNote);
        });
    }
    public List<Note> getAllNotes() {
        return noteRepository.findAll(); // Bonne utilisation comme méthode d'instance
    }

    // Mettre à jour la note d'un étudiant
    public Note updateEtudiantNote(Long etudiantId, Long noteId, Note note) {
        Note existingNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note non trouvée"));

        if (!existingNote.getEtudiant().getId().equals(etudiantId)) {
            throw new RuntimeException("Cette note n'appartient pas à l'étudiant spécifié");
        }

        if (note.getTp() != null) existingNote.setTp(note.getTp());
        if (note.getExam() != null) existingNote.setExam(note.getExam());
        existingNote.calculateMoyenne();

        return noteRepository.save(existingNote);
    }

    // Supprimer une note
    public boolean deleteNote(Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Supprimer la note d'un étudiant
    public void deleteEtudiantNote(Long etudiantId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note non trouvée"));

        if (!note.getEtudiant().getId().equals(etudiantId)) {
            throw new RuntimeException("Cette note n'appartient pas à l'étudiant spécifié");
        }

        noteRepository.delete(note);
    }

    // Calculer la moyenne d'un étudiant
    public Double calculateStudentAverage(Long etudiantId) {
        List<Note> notes = noteRepository.findByEtudiantId(etudiantId);
        return notes.stream()
                .mapToDouble(n -> n.getMoyenne() != null ? n.getMoyenne() : 0.0)
                .average()
                .orElse(0.0);
    }

    // Calculer la moyenne d'un cours
    public Double calculateCourseAverage(Long coursId) {
        List<Note> notes = noteRepository.findByCoursId(coursId);
        return notes.stream()
                .mapToDouble(n -> n.getMoyenne() != null ? n.getMoyenne() : 0.0)
                .average()
                .orElse(0.0);
    }

    // Récupérer la note d'un étudiant pour un cours spécifique
    public Optional<Note> getStudentCourseNote(Long etudiantId, Long coursId) {
        return noteRepository.findByEtudiantIdAndCoursId(etudiantId, coursId);
    }

    // Récupérer la note d'un étudiant pour un cours (alias)
    public Optional<Note> getNotesByEtudiantAndCours(Long etudiantId, Long coursId) {
        return getStudentCourseNote(etudiantId, coursId);
    }

    // Mettre à jour les statistiques d'un enseignant
    public void updateTeacherStatistics(Long enseignantId) {
        // Implémentation pour mettre à jour les statistiques de l'enseignant
        // Par exemple, calculer la moyenne des notes pour ses cours
        List<Note> notes = noteRepository.findByEnseignantId(enseignantId);
        double average = notes.stream()
                .mapToDouble(n -> n.getMoyenne() != null ? n.getMoyenne() : 0.0)
                .average()
                .orElse(0.0);

        // Ici vous pourriez sauvegarder cette statistique quelque part
        // Par exemple dans une entité EnseignantStatistics
    }
}