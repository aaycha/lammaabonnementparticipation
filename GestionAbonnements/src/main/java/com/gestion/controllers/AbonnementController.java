package com.gestion.controllers;

import com.gestion.criteria.AbonnementCriteria;
import com.gestion.entities.Abonnement;
import com.gestion.interfaces.AbonnementService;
import com.gestion.services.AbonnementServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur SCRUD pour les abonnements.
 * Expose : Create, Read, Update, Delete, Search, Tri.
 * API équivalente : GET /api/abonnements?statut=actif&sort=dateDebut DESC
 */
public class AbonnementController {
    private final AbonnementService abonnementService = new AbonnementServiceImpl();

    public Abonnement create(Abonnement abonnement) {
        return abonnementService.create(abonnement);
    }

    public Abonnement getById(Long id) {
        return abonnementService.findById(id).orElse(null);
    }

    public List<Abonnement> getAll() {
        return abonnementService.findAll();
    }

    public List<Abonnement> getAll(String sortBy, String sortOrder) {
        return abonnementService.findAll(sortBy, sortOrder);
    }

    /** Recherche et tri : équivalent GET /api/abonnements?statut=actif&sort=dateDebut DESC */
    public List<Abonnement> search(String statut, String type, Long userId, String sortBy, String sortOrder) {
        AbonnementCriteria criteria = new AbonnementCriteria();
        if (statut != null && !statut.isBlank()) {
            try {
                criteria.setStatut(Abonnement.StatutAbonnement.valueOf(statut.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (type != null && !type.isBlank()) {
            try {
                criteria.setType(Abonnement.TypeAbonnement.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (userId != null) criteria.setUserId(userId);
        if (sortBy != null) criteria.setSortBy(sortBy);
        if (sortOrder != null) criteria.setSortOrder(sortOrder);
        return abonnementService.search(criteria);
    }

    public List<Abonnement> search(AbonnementCriteria criteria) {
        return abonnementService.search(criteria);
    }

    public List<Abonnement> findProchesExpiration(int jours) {
        return abonnementService.findAbonnementsProchesExpiration(jours);
    }

    public BigDecimal getTotalPointsAccumules() {
        return abonnementService.getTotalPointsAccumules();
    }

    public BigDecimal getTotalPointsAccumulesByUser(Long userId) {
        return abonnementService.getTotalPointsAccumulesByUserId(userId);
    }

    public Abonnement update(Abonnement abonnement) {
        return abonnementService.update(abonnement);
    }

    public boolean delete(Long id) {
        return abonnementService.delete(id);
    }

    public boolean toggleAutoRenew(Long id, boolean autoRenew) {
        return abonnementService.toggleAutoRenew(id, autoRenew);
    }
}
