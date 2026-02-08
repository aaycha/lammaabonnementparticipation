package com.gestion.controllers;

import com.gestion.criteria.RecommandationCriteria;
import com.gestion.entities.Recommandation;
import com.gestion.interfaces.RecommandationService;
import com.gestion.services.RecommandationServiceImpl;

import java.util.List;

/**
 * Contrôleur SCRUD pour les recommandations.
 * Create, Read, Update, Delete, Search, Tri.
 * API : GET /api/recommandations/user/:userId?contexte=couple (top 5 par Score DESC)
 */
public class RecommandationController {
    private final RecommandationService recommandationService = new RecommandationServiceImpl();

    public Recommandation create(Recommandation recommandation) {
        return recommandationService.create(recommandation);
    }

    public Recommandation getById(Long id) {
        return recommandationService.findById(id).orElse(null);
    }

    public List<Recommandation> getAll() {
        return recommandationService.findAll();
    }

    public List<Recommandation> getAll(String sortBy, String sortOrder) {
        return recommandationService.findAll(sortBy, sortOrder);
    }

    /** Top N par utilisateur, optionnellement filtré par contexte (couple, amis, famille). Score > 0.5 pour push. */
    public List<Recommandation> getTopByUser(Long userId, String contexte, int limite) {
        return recommandationService.findTopByUserId(userId, contexte, limite > 0 ? limite : 5);
    }

    public List<Recommandation> getByUserId(Long userId) {
        return recommandationService.findByUserId(userId);
    }

    /** Recherche avec critères (score min, algorithme, valides seulement, limite, tri) */
    public List<Recommandation> search(RecommandationCriteria criteria) {
        return recommandationService.search(criteria);
    }

    public List<Recommandation> search(Long userId, String contexte, Double scoreMin, String sortBy, String sortOrder, Integer limite) {
        RecommandationCriteria criteria = new RecommandationCriteria();
        criteria.setUserId(userId);
        criteria.setContexte(contexte);
        criteria.setScoreMinimum(scoreMin != null ? scoreMin : 0.5);
        criteria.setSortBy(sortBy);
        criteria.setSortOrder(sortOrder);
        criteria.setLimite(limite != null ? limite : 10);
        criteria.setValidesSeulement(true);
        return recommandationService.search(criteria);
    }

    public Recommandation update(Recommandation recommandation) {
        return recommandationService.update(recommandation);
    }

    /** Suppression si reco obsolète (ex: événement annulé). Clean-up périodique. */
    public boolean delete(Long id) {
        return recommandationService.delete(id);
    }
}
