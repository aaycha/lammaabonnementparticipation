/*package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;
import com.gestion.interfaces.ProgrammeService;
import com.gestion.services.ProgrammerecomanderServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur SCRUD pour les programmes.
 * Expose : Create, Read, Update, Delete, Search, Tri.
 */
/*public class ProgrammerecomanderController {
    private final ProgrammeService programmeService = new ProgrammerecomanderServiceImpl();

    public ProgrammeRecommender create(ProgrammeRecommender programme) {
        return programmeService.create(programme);
    }

    public ProgrammeRecommender getById(Long id) {
        return programmeService.findById(id).orElse(null);
    }

    public List<ProgrammeRecommender> getAll() {
        return programmeService.findAll();
    }

    public List<ProgrammeRecommender> getAll(String sortBy, String sortOrder) {
        return programmeService.findAll(sortBy, sortOrder);
    }

    public ProgrammeRecommender update(ProgrammeRecommender programme) {
        return programmeService.update(programme);
    }

    public boolean delete(Long id) {
        return programmeService.delete(id);
    }

    public List<ProgrammeRecommender> getByEventId(Long eventId) {
        return programmeService.findByEventId(eventId);
    }

    public List<ProgrammeRecommender> getByTitre(String titre) {
        return programmeService.findByTitre(titre);
    }

    public List<ProgrammeRecommender> getProgrammesEnCours() {
        return programmeService.findProgrammesEnCours();
    }

    public List<ProgrammeRecommender> getProgrammesTermines() {
        return programmeService.findProgrammesTermines();
    }

    public List<ProgrammeRecommender> getProgrammesAVenir() {
        return programmeService.findProgrammesAVenir();
    }

    public List<ProgrammeRecommender> getByDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return programmeService.findByDateBetween(debut, fin);
    }
}*/



package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;
import com.gestion.entities.ProgrammeRecommender;
import com.gestion.interfaces.ProgrammeService;
import com.gestion.services.ProgrammerecomanderServiceImpl;

import java.util.List;

public class ProgrammerecomanderController {

    private final ProgrammeService service =
            new ProgrammerecomanderServiceImpl();

    public ProgrammeRecommender create(Long userId, Long programmeId, double score) {
        return service.create(
                new ProgrammeRecommender(
                        userId,
                        programmeId,
                        score,
                        "Recommandé selon vos choix",
                        ProgrammeRecommender.AlgorithmeReco.CHOIX_UTILISATEUR
                )
        );
    }

    public List<ProgrammeRecommender> top(Long userId) {
        return service.topRecommandations(userId, 5);
    }

    public void utiliser(Long recoId) {
        service.marquerCommeUtilisee(recoId);
    }
}
