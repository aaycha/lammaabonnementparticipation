package com.gestion.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entité représentant une recommandation personnalisée
 * Générée par IA pour suggérer des événements et équipements adaptés
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Recommandation {
    private Long id;
    private Long userId;
    private Long evenementSuggereId;
    private double score;
    private String raison;
    private AlgorithmeReco algorithmeUsed;
    private Map<String, Object> equipementBundle;
    private String sourceScraped;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateGeneration;
    private LocalDateTime dateExpiration;
    private boolean estUtilisee;

    /**
     * Algorithmes de recommandation utilisés
     */
    public enum AlgorithmeReco {
        COLLABORATIVE("Collaborative Filtering"),
        CONTENT_BASED("Content-Based Filtering"),
        NLP("Natural Language Processing"),
        HYBRIDE("Hybrid Approach"),
        ML_TENSORFLOW("TensorFlow ML"),
        CLUSTERING("User Clustering");

        private final String label;

        AlgorithmeReco(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // Constructeurs
    public Recommandation() {}

    public Recommandation(Long userId, Long evenementSuggereId, double score, 
                         String raison, AlgorithmeReco algorithmeUsed) {
        this.userId = userId;
        this.evenementSuggereId = evenementSuggereId;
        this.score = score;
        this.raison = raison;
        this.algorithmeUsed = algorithmeUsed;
        this.dateGeneration = LocalDateTime.now();
        this.dateExpiration = dateGeneration.plusDays(7); // Expiration après 7 jours
        this.estUtilisee = false;
        
        // Génération automatique du bundle d'équipements
        this.equipementBundle = genererEquipementBundle(algorithmeUsed);
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

    public Long getEvenementSuggereId() {
        return evenementSuggereId;
    }

    public void setEvenementSuggereId(Long evenementSuggereId) {
        this.evenementSuggereId = evenementSuggereId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public AlgorithmeReco getAlgorithmeUsed() {
        return algorithmeUsed;
    }

    public void setAlgorithmeUsed(AlgorithmeReco algorithmeUsed) {
        this.algorithmeUsed = algorithmeUsed;
    }

    public Map<String, Object> getEquipementBundle() {
        return equipementBundle;
    }

    public void setEquipementBundle(Map<String, Object> equipementBundle) {
        this.equipementBundle = equipementBundle;
    }

    public String getSourceScraped() {
        return sourceScraped;
    }

    public void setSourceScraped(String sourceScraped) {
        this.sourceScraped = sourceScraped;
    }

    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(LocalDateTime dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public boolean isEstUtilisee() {
        return estUtilisee;
    }

    public void setEstUtilisee(boolean estUtilisee) {
        this.estUtilisee = estUtilisee;
    }

    // Méthodes utilitaires
    private Map<String, Object> genererEquipementBundle(AlgorithmeReco algorithme) {
        return switch (algorithme) {
            case COLLABORATIVE -> Map.of(
                "pour_couple", Map.of(
                    "tente", "2p_confort_queen",
                    "matelas", "epais_luxe",
                    "loisirs", "spa_portable"
                ),
                "pour_amis", Map.of(
                    "tente", "5p_spacieuse",
                    "barbecue", "professionnel_grill",
                    "boissons", "glaciere_grande"
                )
            );
            case CONTENT_BASED -> Map.of(
                "analyse_preferences", Map.of(
                    "tente", "adaptee_climat",
                    "equipement", "base_historique",
                    "activites", "similaires_precedentes"
                )
            );
            case NLP -> Map.of(
                "analyse_texte", Map.of(
                    "contexte", "extraction_description",
                    "emotion", "ton_adapte",
                    "suggestion", "personnalisee_ia"
                )
            );
            case HYBRIDE -> Map.of(
                "combinaison", Map.of(
                    "collaboratif", "poids_0.4",
                    "content", "poids_0.3",
                    "nlp", "poids_0.3"
                )
            );
            case ML_TENSORFLOW -> Map.of(
                "prediction", Map.of(
                    "precision", "modele_neuronal",
                    "confiance", "score_calculé",
                    "optimisation", "tensorflow"
                )
            );
            case CLUSTERING -> Map.of(
                "segmentation", Map.of(
                    "groupe", "similaire_profil",
                    "preferences", "communes_cluster",
                    "recommandation", "groupe_specifique"
                )
            );
        };
    }

    public boolean estValide() {
        return score > 0.5 && 
               dateExpiration != null && 
               dateExpiration.isAfter(LocalDateTime.now()) && 
               !estUtilisee;
    }

    public boolean estPrioritaire() {
        return score > 0.8;
    }

    public void marquerCommeUtilisee() {
        this.estUtilisee = true;
    }

    public void prolongerExpiration(int jours) {
        if (dateExpiration != null) {
            this.dateExpiration = dateExpiration.plusDays(jours);
        }
    }

    public String getDescriptionDetaillee() {
        StringBuilder desc = new StringBuilder();
        desc.append("Recommandation IA (Score: ").append(String.format("%.2f", score)).append("/1.0)\n");
        desc.append("Algorithme: ").append(algorithmeUsed.getLabel()).append("\n");
        desc.append("Raison: ").append(raison).append("\n");
        
        if (equipementBundle != null && !equipementBundle.isEmpty()) {
            desc.append("Équipements suggérés:\n");
            equipementBundle.forEach((key, value) -> 
                desc.append("  - ").append(key).append(": ").append(value).append("\n"));
        }
        
        if (sourceScraped != null) {
            desc.append("Source: ").append(sourceScraped).append("\n");
        }
        
        return desc.toString();
    }

    @Override
    public String toString() {
        return String.format("Recommandation{id=%d, userId=%d, evenementId=%d, score=%.2f, algo=%s}", 
                           id, userId, evenementSuggereId, score, algorithmeUsed);
    }
}
