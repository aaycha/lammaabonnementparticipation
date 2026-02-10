/*package com.gestion.criteria;

import com.gestion.entities.Participation;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Critères de recherche et tri pour les participations.
 * API: GET /api/participations/event/:eventId, filtrage par ContexteSocial
 */
/*public class ParticipationCriteria {
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
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy != null ? sortBy : "dateInscription"; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = "DESC".equalsIgnoreCase(sortOrder != null ? sortOrder : "") ? "DESC" : "ASC"; }
}
*/

package com.gestion.criteria;

import com.gestion.entities.Participation;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Critères de recherche et de tri pour les participations.
 *
 * Utilisé pour filtrer les participations via :
 * - GET /api/participations
 * - GET /api/participations/event/{eventId}
 * - GET /api/participations/user/{userId}
 *
 * Supporte le filtrage par :
 * - utilisateur
 * - événement
 * - statut
 * - type de participation
 * - contexte social
 * - plage de dates d'inscription
 *
 * Supporte le tri par :
 * - dateInscription (par défaut)
 * - statut
 * - type
 * - hebergementNuits
 */
public class ParticipationCriteria {

    private Long userId;
    private Long evenementId;
    private Participation.StatutParticipation statut;
    private Participation.TypeParticipation type;
    private Participation.ContexteSocial contexteSocial;
    private LocalDateTime dateInscriptionFrom;
    private LocalDateTime dateInscriptionTo;
    private String sortBy;     // dateInscription, statut, type, hebergementNuits
    private String sortOrder;  // ASC, DESC

    public ParticipationCriteria() {
        this.sortBy = "dateInscription";
        this.sortOrder = "DESC";
    }

    // ────────────────────────────────────────────────
    // Getters
    // ────────────────────────────────────────────────
    public Long getUserId() {
        return userId;
    }

    public Long getEvenementId() {
        return evenementId;
    }

    public Participation.StatutParticipation getStatut() {
        return statut;
    }

    public Participation.TypeParticipation getType() {
        return type;
    }

    public Participation.ContexteSocial getContexteSocial() {
        return contexteSocial;
    }

    public LocalDateTime getDateInscriptionFrom() {
        return dateInscriptionFrom;
    }

    public LocalDateTime getDateInscriptionTo() {
        return dateInscriptionTo;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    // ────────────────────────────────────────────────
    // Setters avec validation de base
    // ────────────────────────────────────────────────
    public void setUserId(Long userId) {
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("userId doit être positif ou null");
        }
        this.userId = userId;
    }

    public void setEvenementId(Long evenementId) {
        if (evenementId != null && evenementId <= 0) {
            throw new IllegalArgumentException("evenementId doit être positif ou null");
        }
        this.evenementId = evenementId;
    }

    public void setStatut(Participation.StatutParticipation statut) {
        this.statut = statut;
    }

    public void setType(Participation.TypeParticipation type) {
        this.type = type;
    }

    public void setContexteSocial(Participation.ContexteSocial contexteSocial) {
        this.contexteSocial = contexteSocial;
    }

    public void setDateInscriptionFrom(LocalDateTime dateInscriptionFrom) {
        this.dateInscriptionFrom = dateInscriptionFrom;
        // Optionnel : vérifier que dateFrom <= dateTo
        validateDateRange();
    }

    public void setDateInscriptionTo(LocalDateTime dateInscriptionTo) {
        this.dateInscriptionTo = dateInscriptionTo;
        // Optionnel : vérifier que dateFrom <= dateTo
        validateDateRange();
    }

    public void setSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            this.sortBy = "dateInscription";
            return;
        }

        String cleaned = sortBy.trim().toLowerCase();
        if (cleaned.equals("dateinscription") ||
                cleaned.equals("statut") ||
                cleaned.equals("type") ||
                cleaned.equals("hebergementnuits")) {
            this.sortBy = cleaned;
        } else {
            throw new IllegalArgumentException(
                    "sortBy invalide. Valeurs possibles : dateInscription, statut, type, hebergementNuits"
            );
        }
    }

    public void setSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            this.sortOrder = "DESC";
            return;
        }

        String cleaned = sortOrder.trim().toUpperCase();
        if (cleaned.equals("ASC") || cleaned.equals("DESC")) {
            this.sortOrder = cleaned;
        } else {
            throw new IllegalArgumentException("sortOrder invalide. Valeurs possibles : ASC, DESC");
        }
    }

    // ────────────────────────────────────────────────
    // Validation de la plage de dates
    // ────────────────────────────────────────────────
    private void validateDateRange() {
        if (dateInscriptionFrom != null && dateInscriptionTo != null) {
            if (dateInscriptionFrom.isAfter(dateInscriptionTo)) {
                throw new IllegalArgumentException(
                        "dateInscriptionFrom ne peut pas être postérieure à dateInscriptionTo"
                );
            }
        }
    }

    // ────────────────────────────────────────────────
    // Utilitaire : savoir si au moins un critère de filtre est présent
    // ────────────────────────────────────────────────
    public boolean hasAnyFilter() {
        return userId != null ||
                evenementId != null ||
                statut != null ||
                type != null ||
                contexteSocial != null ||
                dateInscriptionFrom != null ||
                dateInscriptionTo != null;
    }

    @Override
    public String toString() {
        return "ParticipationCriteria{" +
                "userId=" + userId +
                ", evenementId=" + evenementId +
                ", statut=" + statut +
                ", type=" + type +
                ", contexteSocial=" + contexteSocial +
                ", dateInscriptionFrom=" + dateInscriptionFrom +
                ", dateInscriptionTo=" + dateInscriptionTo +
                ", sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipationCriteria that = (ParticipationCriteria) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(evenementId, that.evenementId) &&
                statut == that.statut &&
                type == that.type &&
                contexteSocial == that.contexteSocial &&
                Objects.equals(dateInscriptionFrom, that.dateInscriptionFrom) &&
                Objects.equals(dateInscriptionTo, that.dateInscriptionTo) &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(sortOrder, that.sortOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, evenementId, statut, type, contexteSocial, dateInscriptionFrom, dateInscriptionTo, sortBy, sortOrder);
    }
}