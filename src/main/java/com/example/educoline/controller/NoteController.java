package com.example.educoline.controller;

import com.example.educoline.entity.Note;
import com.example.educoline.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        try {
            Note createdNote = noteService.createNote(note);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Erreur lors de la création de la note: " + e.getMessage()
            );
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note non trouvée avec l'ID: " + id
                ));
    }

    @GetMapping(
            value = "/teacher/{teacherId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Note>> getNotesByTeacher(@PathVariable Long teacherId) {
        List<Note> notes = noteService.getNotesByTeacher(teacherId);
        if (notes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Aucune note trouvée pour l'enseignant ID: " + teacherId
            );
        }
        return ResponseEntity.ok(notes);
    }

    @GetMapping(
            value = "/student/{studentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Note>> getNotesByStudent(@PathVariable Long studentId) {
        List<Note> notes = noteService.getNotesByStudent(studentId);
        if (notes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Aucune note trouvée pour l'étudiant ID: " + studentId
            );
        }
        return ResponseEntity.ok(notes);
    }

    @GetMapping(
            value = "/course/{courseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Note>> getNotesByCourse(@PathVariable Long courseId) {
        List<Note> notes = noteService.getNotesByCourse(courseId);
        if (notes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Aucune note trouvée pour le cours ID: " + courseId
            );
        }
        return ResponseEntity.ok(notes);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody Note noteDetails) {
        try {
            return noteService.updateNote(id, noteDetails)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Note non trouvée avec l'ID: " + id
                    ));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        boolean isDeleted = noteService.deleteNote(id);
        if (!isDeleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Note non trouvée avec l'ID: " + id
            );
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping(
            value = "/student/{studentId}/average",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Double> getStudentAverage(@PathVariable Long studentId) {
        Double average = noteService.calculateStudentAverage(studentId);
        if (average == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Aucune note trouvée pour l'étudiant ID: " + studentId
            );
        }
        return ResponseEntity.ok(average);
    }

    @GetMapping(
            value = "/course/{courseId}/average",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Double> getCourseAverage(@PathVariable Long courseId) {
        Double average = noteService.calculateCourseAverage(courseId);
        if (average == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Aucune note trouvée pour le cours ID: " + courseId
            );
        }
        return ResponseEntity.ok(average);
    }

    @GetMapping(
            value = "/student/{studentId}/course/{courseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Note> getStudentCourseNote(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return noteService.getStudentCourseNote(studentId, courseId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note non trouvée pour l'étudiant ID: " + studentId +
                                " et le cours ID: " + courseId
                ));
    }
}