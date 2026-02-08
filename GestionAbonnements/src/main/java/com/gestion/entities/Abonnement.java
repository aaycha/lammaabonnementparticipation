package com.gestion.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Entité représentant un abonnement utilisateur
 * Gère les memberships récurrents pour accès privilégié à des événements de loisirs
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Abonnement {
    private Long id;
    private Long userId;
    private TypeAbonnement type;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dateDebut;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dateFin;
    private BigDecimal prix;
    private StatutAbonnement statut;
    private Map<String, Object> avantages;
    private boolean autoRenew;
    private int pointsAccumules;
    private double churnScore;

    /**
     * Types d'abonnement disponibles
     */
    public enum TypeAbonnement {
        MENSUEL("Mensuel"),
        ANNUEL("Annuel"),
        PREMIUM("Premium");

        private final String label;

        TypeAbonnement(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Statuts possibles pour un abonnement
     */
    public enum StatutAbonnement {
        ACTIF("Actif"),
        EXPIRE("Expiré"),
        SUSPENDU("Suspendu"),
        EN_ATTENTE("En attente");

        private final String label;

        StatutAbonnement(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // Constructeurs
    public Abonnement() {}

    public Abonnement(Long userId, TypeAbonnement type, LocalDate dateDebut, 
                     BigDecimal prix, boolean autoRenew) {
        this.userId = userId;
        this.type = type;
        this.dateDebut = dateDebut;
        this.prix = prix;
        this.autoRenew = autoRenew;
        this.statut = StatutAbonnement.ACTIF;
        this.pointsAccumules = 0;
        this.churnScore = 0.0;
        
        // Calcul automatique de la date de fin selon le type
        switch (type) {
            case MENSUEL:
                this.dateFin = dateDebut.plusMonths(1);
                break;
            case ANNUEL:
                this.dateFin = dateDebut.plusYears(1);
                break;
            case PREMIUM:
                this.dateFin = dateDebut.plusYears(1);
                break;
        }
        
        // Configuration des avantages par défaut
        this.avantages = Map.of(
            "discounts", type == TypeAbonnement.PREMIUM ? 20 : 10,
            "prioriteWaiting", type == TypeAbonnement.PREMIUM,
            "accesEvenementsExclusifs", type == TypeAbonnement.PREMIUM
        );
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TypeAbonnement getType() {
        return type;
    }

    public void setType(TypeAbonnement type) {
        this.type = type;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public StatutAbonnement getStatut() {
        return statut;
    }

    public void setStatut(StatutAbonnement statut) {
        this.statut = statut;
    }

    public Map<String, Object> getAvantages() {
        return avantages;
    }

    public void setAvantages(Map<String, Object> avantages) {
        this.avantages = avantages;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public int getPointsAccumules() {
        return pointsAccumules;
    }

    public void setPointsAccumules(int pointsAccumules) {
        this.pointsAccumules = pointsAccumules;
    }

    public double getChurnScore() {
        return churnScore;
    }

    public void setChurnScore(double churnScore) {
        this.churnScore = churnScore;
    }

    // Méthodes utilitaires
    public boolean estActif() {
        return statut == StatutAbonnement.ACTIF && 
               dateFin != null && 
               dateFin.isAfter(LocalDate.now());
    }

    public boolean estProcheExpiration(int jours) {
        return dateFin != null && 
               dateFin.minusDays(jours).isBefore(LocalDate.now()) || 
               dateFin.minusDays(jours).isEqual(LocalDate.now());
    }

    public void ajouterPoints(int points) {
        this.pointsAccumules += points;
    }

    public void utiliserPoints(int points) {
        if (pointsAccumules >= points) {
            this.pointsAccumules -= points;
        } else {
            throw new IllegalArgumentException("Points insuffisants");
        }
    }

    @Override
    public String toString() {
        return String.format("Abonnement{id=%d, userId=%d, type=%s, statut=%s, prix=%.2f, points=%d}", 
                           id, userId, type, statut, prix, pointsAccumules);
    }
}
