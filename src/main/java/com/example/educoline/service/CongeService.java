package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.CongeRepository;
import com.example.educoline.repository.EnseignantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CongeService {

    private final CongeRepository congeRepository;
    private final EnseignantRepository enseignantRepository;
    private final NoteService noteService;
    private final EmailRequest emailRequest;

    public CongeService(CongeRepository congeRepository,
                        EnseignantRepository enseignantRepository,
                        NoteService noteService,
                        EmailRequest emailRequest) {
        this.congeRepository = congeRepository;
        this.enseignantRepository = enseignantRepository;
        this.noteService = noteService;
        this.emailRequest = emailRequest;
    }

    public List<Conge> getAllConges() {
        return congeRepository.findAll();
    }

    public List<Conge> getCongesByStatut(Conge.StatutConge statut) {
        return congeRepository.findByStatut(statut);
    }

    public Conge getCongeById(Long id) {
        return congeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé avec l'ID: " + id));
    }

    public Conge saveConge(Conge conge) {
        return congeRepository.save(conge);
    }

    public List<Conge> getCongesByEnseignantId(Long id) {
        return congeRepository.findByEnseignantId(id);
    }

    public Conge approveConge(Long congeId) {
        Conge conge = getCongeById(congeId);

        if (conge.getStatut() != Conge.StatutConge.EN_ATTENTE) {
            throw new RuntimeException("Seuls les congés en attente peuvent être approuvés");
        }

        // Mise à jour du statut du congé
        conge.setStatut(Conge.StatutConge.APPROUVE);

        // Mise à jour du statut de l'enseignant
        Enseignant enseignant = conge.getEnseignant();
        enseignant.setStatus(Enseignant.StatusEnseignant.EN_CONGE);
        enseignantRepository.save(enseignant);

        Conge updatedConge = congeRepository.save(conge);
        saveHistorique(updatedConge, "Congé approuvé");
        sendApprovalEmail(updatedConge);
        noteService.updateTeacherStatistics(enseignant.getId());

        return updatedConge;
    }

    public Conge rejectConge(Long congeId, String raison) {
        if (raison == null || raison.trim().isEmpty()) {
            throw new RuntimeException("Un motif de rejet est requis");
        }

        Conge conge = getCongeById(congeId);
        conge.setStatut(Conge.StatutConge.REJETE);
        conge.setMotifRejet(raison);

        Conge updatedConge = congeRepository.save(conge);
        saveHistorique(updatedConge, "Congé rejeté: " + raison);
        sendRejectionEmail(updatedConge, raison);

        return updatedConge;
    }

    public void annulerConge(Long congeId, String motifAnnulation) {
        if (motifAnnulation == null || motifAnnulation.trim().isEmpty()) {
            throw new RuntimeException("Un motif d'annulation est requis");
        }

        Conge conge = getCongeById(congeId);
        conge.setStatut(Conge.StatutConge.ANNULE);
        conge.setMotifAnnulation(motifAnnulation);
        congeRepository.save(conge);

        saveHistorique(conge, "Congé annulé: " + motifAnnulation);
        sendCancellationEmail(conge, motifAnnulation);
    }

    private void saveHistorique(Conge conge, String action) {
        // Implémentation de la sauvegarde d'historique
    }

    private void sendApprovalEmail(Conge conge) {
        Enseignant enseignant = conge.getEnseignant();
        String sujet = "Demande de congé approuvée";
        String contenu = String.format(
                "Bonjour %s %s,%n%nVotre demande de congé a été approuvée.%nType: %s%nPériode: du %s au %s%n%nCordialement,%nL'administration",
                enseignant.getPrenom(),
                enseignant.getNom(),
                conge.getType(),
                conge.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                conge.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        emailRequest.envoyerEmail(enseignant.getEmail(), sujet, contenu);
    }

    private void sendRejectionEmail(Conge conge, String raison) {
        Enseignant enseignant = conge.getEnseignant();
        String sujet = "Demande de congé rejetée";
        String contenu = String.format(
                "Bonjour %s %s,%n%nVotre demande de congé a été rejetée.%nMotif: %s%nType: %s%nPériode demandée: du %s au %s%n%nCordialement,%nL'administration",
                enseignant.getPrenom(),
                enseignant.getNom(),
                raison,
                conge.getType(),
                conge.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                conge.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        emailRequest.envoyerEmail(enseignant.getEmail(), sujet, contenu);
    }

    private void sendCancellationEmail(Conge conge, String motif) {
        Enseignant enseignant = conge.getEnseignant();
        String sujet = "Congé annulé";
        String contenu = String.format(
                "Bonjour %s %s,%n%nVotre congé a été annulé.%nMotif: %s%nType: %s%nPériode: du %s au %s%n%nCordialement,%nL'administration",
                enseignant.getPrenom(),
                enseignant.getNom(),
                motif,
                conge.getType(),
                conge.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                conge.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        emailRequest.envoyerEmail(enseignant.getEmail(), sujet, contenu);
    }
}