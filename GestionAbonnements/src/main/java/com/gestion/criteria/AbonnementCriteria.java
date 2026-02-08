package com.gestion.criteria;

import com.gestion.entities.Abonnement;

import java.time.LocalDate;

/**
 * Critères de recherche et tri pour les abonnements.
 * Utilisé par l'API GET /api/abonnements?statut=actif&sort=dateDebut DESC
 */
public class AbonnementCriteria {
    private Abonnement.StatutAbonnement statut;
    private Abonnement.TypeAbonnement type;
    private Long userId;
    private LocalDate dateFinAvant;
    private LocalDate dateFinApres;
    private Boolean autoRenew;
    private Integer pointsMinimum;
    private String sortBy;   // dateDebut, dateFin, prix, statut, pointsAccumules, churnScore
    private String sortOrder; // ASC, DESC

    public AbonnementCriteria() {
        this.sortBy = "dateDebut";
        this.sortOrder = "DESC";
    }

    public Abonnement.StatutAbonnement getStatut() { return statut; }
    public void setStatut(Abonnement.StatutAbonnement statut) { this.statut = statut; }
    public Abonnement.TypeAbonnement getType() { return type; }
    public void setType(Abonnement.TypeAbonnement type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getDateFinAvant() { return dateFinAvant; }
    public void setDateFinAvant(LocalDate dateFinAvant) { this.dateFinAvant = dateFinAvant; }
    public LocalDate getDateFinApres() { return dateFinApres; }
    public void setDateFinApres(LocalDate dateFinApres) { this.dateFinApres = dateFinApres; }
    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }
    public Integer getPointsMinimum() { return pointsMinimum; }
    public void setPointsMinimum(Integer pointsMinimum) { this.pointsMinimum = pointsMinimum; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy != null ? sortBy : "dateDebut"; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = "DESC".equalsIgnoreCase(sortOrder != null ? sortOrder : "") ? "DESC" : "ASC"; }
}
