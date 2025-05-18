package com.example.educoline.controller;

import com.example.educoline.entity.Cours;
import com.example.educoline.entity.Enseignant;
import com.example.educoline.entity.Etudiant;
import com.example.educoline.entity.Note;
import com.example.educoline.repository.CoursRepository;
import com.example.educoline.repository.EtudiantRepository;
import com.example.educoline.repository.NoteRepository;
import com.example.educoline.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteService noteService;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private CoursRepository coursRepository;
    @Autowired
    private EtudiantRepository etudiantRepository;

    // Ajouter une note
    @PostMapping
    public ResponseEntity<?> addNote(@RequestBody Note note) {
        if (note.getAbsences() > 3) {
            return ResponseEntity
                    .badRequest()
                    .body("Erreur : Vous êtes éliminé(e) à cause de plus de 3 absences.");
        }
        Note saved = noteService.saveNote(note);
        return ResponseEntity.status(201).body(saved);
    }

    // Récupérer toutes les notes
    @GetMapping
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    // Récupérer une note par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Optional<Note> note = noteService.getNoteById(id);
        return note.isPresent() ? ResponseEntity.ok(note.get()) : ResponseEntity.notFound().build();
    }

    // Mettre à jour une note
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody Note note) {
        if (note.getAbsences() > 3) {
            return ResponseEntity
                    .badRequest()
                    .body("Erreur : Vous êtes éliminé(e) à cause de plus de 3 absences.");
        }
        Note updatedNote = noteService.updateNote(id, note);
        return updatedNote != null ? ResponseEntity.ok(updatedNote) : ResponseEntity.notFound().build();
    }

    // Supprimer une note
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

}
