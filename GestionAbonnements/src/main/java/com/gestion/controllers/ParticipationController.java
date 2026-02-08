/*package com.gestion.controllers;

import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.Participation;
import com.gestion.interfaces.ParticipationService;
import com.gestion.services.ParticipationServiceImpl;

import java.util.List;

/**
 * Contrôleur SCRUD pour les participations.
 * Expose : Create, Read, Update, Delete, Search, Tri.
 * API : GET /api/participations/event/:eventId (pour organisateur), filtrage par ContexteSocial
 */
/*public class ParticipationController {
    private final ParticipationService participationService = new ParticipationServiceImpl();

    public Participation create(Participation participation) {
        return participationService.create(participation);
    }

    public Participation getById(Long id) {
        return participationService.findById(id).orElse(null);
    }

    public List<Participation> getAll() {
        return participationService.findAll();
    }

    public List<Participation> getAll(String sortBy, String sortOrder) {
        return participationService.findAll(sortBy, sortOrder);
    }

    public List<Participation> getByEvenementId(Long evenementId) {
        return participationService.findByEvenementId(evenementId);
    }

    public List<Participation> getByUserId(Long userId) {
        return participationService.findByUserId(userId);
    }

    /** Recherche et tri : statut, type, contexte (ex: couple), événement */
    /*public List<Participation> search(String statut, String type, String contexte, Long evenementId, Long userId, String sortBy, String sortOrder) {
        ParticipationCriteria criteria = new ParticipationCriteria();
        if (statut != null && !statut.isBlank()) {
            try {
                criteria.setStatut(Participation.StatutParticipation.valueOf(statut.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (type != null && !type.isBlank()) {
            try {
                criteria.setType(Participation.TypeParticipation.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (contexte != null && !contexte.isBlank()) {
            try {
                criteria.setContexteSocial(Participation.ContexteSocial.valueOf(contexte.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (evenementId != null) criteria.setEvenementId(evenementId);
        if (userId != null) criteria.setUserId(userId);
        if (sortBy != null) criteria.setSortBy(sortBy);
        if (sortOrder != null) criteria.setSortOrder(sortOrder);
        return participationService.search(criteria);
    }

    public List<Participation> search(ParticipationCriteria criteria) {
        return participationService.search(criteria);
    }

    public Participation update(Participation participation) {
        return participationService.update(participation);
    }

    public boolean delete(Long id) {
        return participationService.delete(id);
    }

    public Participation confirmer(Long id) {
        return participationService.confirmerParticipation(id);
    }

    public Participation annuler(Long id, String raison) {
        return participationService.annulerParticipation(id, raison);
    }

    public int getPlacesDisponibles(Long evenementId) {
        return participationService.getPlacesDisponibles(evenementId);
    }

    public <T> ScopedValue<T> findById(Long participationId) {
    }
}*/


package com.gestion.controllers;

import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.Participation;
import com.gestion.interfaces.ParticipationService;
import com.gestion.services.ParticipationServiceImpl;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur SCRUD pour les participations.
 * Expose : Create, Read, Update, Delete, Search, Tri.
 * API : GET /api/participations/event/:eventId (pour organisateur), filtrage par ContexteSocial
 */
public class ParticipationController {

    private final ParticipationService participationService = new ParticipationServiceImpl();

    // ===== CREATE =====
    public Participation create(Participation participation) {
        return participationService.create(participation);
    }

    // ===== READ =====
    public Participation getById(Long id) {
        return participationService.findById(id).orElse(null);
    }

    public List<Participation> getAll() {
        return participationService.findAll();
    }

    public List<Participation> getAll(String sortBy, String sortOrder) {
        return participationService.findAll(sortBy, sortOrder);
    }

    public List<Participation> getByEvenementId(Long evenementId) {
        return participationService.findByEvenementId(evenementId);
    }

    public List<Participation> getByUserId(Long userId) {
        return participationService.findByUserId(userId);
    }

    // ===== SEARCH / FILTRAGE =====
    public List<Participation> search(String statut, String type, String contexte,
                                      Long evenementId, Long userId,
                                      String sortBy, String sortOrder) {
        ParticipationCriteria criteria = new ParticipationCriteria();

        if (statut != null && !statut.isBlank()) {
            try {
                criteria.setStatut(Participation.StatutParticipation.valueOf(statut.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (type != null && !type.isBlank()) {
            try {
                criteria.setType(Participation.TypeParticipation.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (contexte != null && !contexte.isBlank()) {
            try {
                criteria.setContexteSocial(Participation.ContexteSocial.valueOf(contexte.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (evenementId != null) criteria.setEvenementId(evenementId);
        if (userId != null) criteria.setUserId(userId);
        if (sortBy != null) criteria.setSortBy(sortBy);
        if (sortOrder != null) criteria.setSortOrder(sortOrder);

        return participationService.search(criteria);
    }

    public List<Participation> search(ParticipationCriteria criteria) {
        return participationService.search(criteria);
    }

    // ===== UPDATE =====
    public Participation update(Participation participation) {
        return participationService.update(participation);
    }

    // ===== DELETE =====
    public boolean delete(Long id) {
        return participationService.delete(id);
    }

    // ===== CONFIRMER / ANNULER =====
    public Participation confirmer(Long id) {
        return participationService.confirmerParticipation(id);
    }

    public Participation annuler(Long id, String raison) {
        return participationService.annulerParticipation(id, raison);
    }

    // ===== PLACES DISPONIBLES =====
    public int getPlacesDisponibles(Long evenementId) {
        return participationService.getPlacesDisponibles(evenementId);
    }

    // ===== FIND BY ID SÉCURISÉ =====
    public Optional<Participation> findByIdSafe(Long participationId) {
        return participationService.findById(participationId);
    }
}

