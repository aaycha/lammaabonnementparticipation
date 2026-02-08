/*package com.gestion.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * Entité représentant un programme d'événement
 */
/*@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgrammeRecommender {
    private Long idProg;
    private Long eventId;
    private String titre;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime debut;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime fin;

    // Constructeurs
    public ProgrammeRecommender() {}

    public ProgrammeRecommender(Long eventId, String titre, LocalDateTime debut, LocalDateTime fin) {
        this.eventId = eventId;
        this.titre = titre;
        this.debut = debut;
        this.fin = fin;
    }

    // Getters et Setters
    public Long getIdProg() {
        return idProg;
    }

    public void setIdProg(Long idProg) {
        this.idProg = idProg;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    // Méthodes utilitaires
    public boolean estEnCours() {
        LocalDateTime now = LocalDateTime.now();
        return debut != null && fin != null &&
               !now.isBefore(debut) && !now.isAfter(fin);
    }

    public boolean estTermine() {
        return fin != null && fin.isBefore(LocalDateTime.now());
    }

    public boolean estAVenir() {
        return debut != null && debut.isAfter(LocalDateTime.now());
    }

    public long getDureeMinutes() {
        if (debut != null && fin != null) {
            return java.time.Duration.between(debut, fin).toMinutes();
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Programme{idProg=%d, eventId=%d, titre='%s', debut=%s, fin=%s}",
                           idProg, eventId, titre, debut, fin);
    }
}*/
package com.gestion.entities;

import java.time.LocalDateTime;

public class ProgrammeRecommender {

    private Long id;
    private Long userId;
    private Long programmeId;
    private double score;
    private String raison;
    private AlgorithmeReco algorithme;
    private LocalDateTime dateCreation;
    private boolean utilise;

    // Enum
    public enum AlgorithmeReco {
        CHOIX_UTILISATEUR,
        COLLABORATIF,
        CONTENU
    }

    // Constructeur vide (OBLIGATOIRE JDBC)
    public ProgrammeRecommender() {}

    // Constructeur principal
    public ProgrammeRecommender(Long userId,
                                Long programmeId,
                                double score,
                                String raison,
                                AlgorithmeReco algorithme) {
        this.userId = userId;
        this.programmeId = programmeId;
        this.score = score;
        this.raison = raison;
        this.algorithme = algorithme;
        this.dateCreation = LocalDateTime.now();
        this.utilise = false;
    }

    // Logique métier simple
    public boolean estValide() {
        return score >= 0.5 && !utilise;
    }

    public boolean estPrioritaire() {
        return score >= 0.8;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public Long getProgrammeId() { return programmeId; }
    public double getScore() { return score; }
    public String getRaison() { return raison; }
    public AlgorithmeReco getAlgorithme() { return algorithme; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public boolean isUtilise() { return utilise; }

    public void setUtilise(boolean utilise) {
        this.utilise = utilise;
    }

    // ✅ TO STRING (OBLIGATOIRE POUR LA CONSOLE)
    @Override
    public String toString() {
        return "ProgrammeRecommande {" +
                "programmeId=" + programmeId +
                ", score=" + String.format("%.2f", score) +
                ", algorithme=" + algorithme +
                ", prioritaire=" + estPrioritaire() +
                ", raison='" + raison + '\'' +
                '}';
    }
}
