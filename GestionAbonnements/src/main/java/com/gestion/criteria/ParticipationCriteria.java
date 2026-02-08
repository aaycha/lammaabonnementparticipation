package com.gestion.criteria;

import com.gestion.entities.Participation;

import java.time.LocalDateTime;

/**
 * Critères de recherche et tri pour les participations.
 * API: GET /api/participations/event/:eventId, filtrage par ContexteSocial
 */
public class ParticipationCriteria {
    private Long userId;
    private Long evenementId;
    private Participation.StatutParticipation statut;
    private Participation.TypeParticipation type;
    private Participation.ContexteSocial contexteSocial;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String sortBy;   // dateInscription, statut, type, hebergementNuits
    private String sortOrder; // ASC, DESC

    public ParticipationCriteria() {
        this.sortBy = "dateInscription";
        this.sortOrder = "DESC";
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getEvenementId() { return evenementId; }
    public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }
    public Participation.StatutParticipation getStatut() { return statut; }
    public void setStatut(Participation.StatutParticipation statut) { this.statut = statut; }
    public Participation.TypeParticipation getType() { return type; }
    public void setType(Participation.TypeParticipation type) { this.type = type; }
    public Participation.ContexteSocial getContexteSocial() { return contexteSocial; }
    public void setContexteSocial(Participation.ContexteSocial contexteSocial) { this.contexteSocial = contexteSocial; }
    public LocalDateTime getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }
    public LocalDateTime getDateTo() { return dateTo; }
    public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy != null ? sortBy : "dateInscription"; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = "DESC".equalsIgnoreCase(sortOrder != null ? sortOrder : "") ? "DESC" : "ASC"; }
}
