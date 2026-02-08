package com.gestion.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * Entité représentant une participation à un événement
 * Gère les inscriptions individuelles ou groupées avec hébergements et équipements
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Participation {
    private Long id;
    private Long userId;
    private Long evenementId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateInscription;
    private TypeParticipation type;
    private StatutParticipation statut;
    private int hebergementNuits;
    private ContexteSocial contexteSocial;
    private String badgeAssocie;

    /**
     * Types de participation possibles
     */
    public enum TypeParticipation {
        SIMPLE("Simple"),
        HEBERGEMENT("Avec hébergement"),
        GROUPE("Groupe");

        private final String label;

        TypeParticipation(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Statuts possibles pour une participation
     */
    public enum StatutParticipation {
        EN_ATTENTE("En attente"),
        CONFIRME("Confirmé"),
        ANNULE("Annulé"),
        EN_LISTE_ATTENTE("En liste d'attente");

        private final String label;

        StatutParticipation(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Contextes sociaux pour les recommandations personnalisées
     */
    public enum ContexteSocial {
        COUPLE("Couple"),
        AMIS("Amis"),
        FAMILLE("Famille"),
        SOLO("Solo"),
        PROFESSIONNEL("Professionnel");

        private final String label;

        ContexteSocial(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // Constructeurs
    public Participation() {}

    public Participation(Long userId, Long evenementId, TypeParticipation type, 
                        ContexteSocial contexteSocial) {
        this.userId = userId;
        this.evenementId = evenementId;
        this.type = type;
        this.contexteSocial = contexteSocial;
        this.dateInscription = LocalDateTime.now();
        this.statut = StatutParticipation.EN_ATTENTE;
        this.hebergementNuits = type == TypeParticipation.HEBERGEMENT ? 1 : 0;
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

    public Long getEvenementId() {
        return evenementId;
    }

    public void setEvenementId(Long evenementId) {
        this.evenementId = evenementId;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public TypeParticipation getType() {
        return type;
    }

    public void setType(TypeParticipation type) {
        this.type = type;
    }

    public StatutParticipation getStatut() {
        return statut;
    }

    public void setStatut(StatutParticipation statut) {
        this.statut = statut;
    }

    public int getHebergementNuits() {
        return hebergementNuits;
    }

    public void setHebergementNuits(int hebergementNuits) {
        this.hebergementNuits = hebergementNuits;
    }

    public ContexteSocial getContexteSocial() {
        return contexteSocial;
    }

    public void setContexteSocial(ContexteSocial contexteSocial) {
        this.contexteSocial = contexteSocial;
    }

    public String getBadgeAssocie() {
        return badgeAssocie;
    }

    public void setBadgeAssocie(String badgeAssocie) {
        this.badgeAssocie = badgeAssocie;
    }

    // Méthodes utilitaires
    public boolean estConfirmee() {
        return statut == StatutParticipation.CONFIRME;
    }

    public boolean estEnAttente() {
        return statut == StatutParticipation.EN_ATTENTE || 
               statut == StatutParticipation.EN_LISTE_ATTENTE;
    }

    public boolean requiertHebergement() {
        return type == TypeParticipation.HEBERGEMENT || type == TypeParticipation.GROUPE;
    }

    public void confirmer() {
        this.statut = StatutParticipation.CONFIRME;
        // Attribution automatique d'un badge selon le contexte
        this.badgeAssocie = attribuerBadge();
    }

    public void annuler() {
        this.statut = StatutParticipation.ANNULE;
    }

    private String attribuerBadge() {
        return switch (contexteSocial) {
            case COUPLE -> "Romantique_Aventure";
            case AMIS -> "Esprit_Equipe";
            case FAMILLE -> "Famille_Unie";
            case SOLO -> "Explorateur_Solitaire";
            case PROFESSIONNEL -> "Pro_Leadership";
        };
    }

    @Override
    public String toString() {
        return String.format("Participation{id=%d, userId=%d, evenementId=%d, type=%s, statut=%s, contexte=%s}", 
                           id, userId, evenementId, type, statut, contexteSocial);
    }
}
