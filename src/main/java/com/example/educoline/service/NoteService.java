package com.example.educoline.service;

import com.example.educoline.entity.Note;
import com.example.educoline.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    // Ajouter une note
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    // Récupérer toutes les notes
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    // Récupérer une note par son ID
    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    // Mettre à jour une note
    public Note updateNote(Long id, Note note) {
        if (noteRepository.existsById(id)) {
            note.setId(id);
            return noteRepository.save(note);
        }
        return null; // Ou tu pourrais lancer une exception personnalisée
    }

    // Supprimer une note
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
    // Récupérer la note d'un étudiant pour un cours spécifique, par un enseignant spécifique
    public Optional<Note> getNoteByEnseignantCoursEtudiant(Long enseignantId, Long coursId, Long etudiantId) {
        return noteRepository.findByEnseignantIdAndCoursIdAndEtudiantId(enseignantId, coursId, etudiantId);
    }

    public Note addNoteForEtudiant(Long id, Note note) {
        return note;
    }

    public List<Note> getNotesByEtudiantId(Long id) {
        return List.of();
    }
}

