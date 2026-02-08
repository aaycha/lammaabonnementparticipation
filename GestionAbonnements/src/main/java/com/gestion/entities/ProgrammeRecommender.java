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

import java.time.LocalTime;

public class ProgrammeRecommender {

    private Long id;
    private Long participationId;

    private String activite;          // Barbecue, Vin, Jeux, Randonnée…
    private LocalTime heureDebut;
    private LocalTime heureFin;

    private Ambiance ambiance;        // CALME, FESTIVE, SOCIALE
    private String justification;     // Pourquoi cette activité
    private boolean recommande;       // validée ou non

    public enum Ambiance {
        CALME,
        FESTIVE,
        SOCIALE
    }

    public ProgrammeRecommender() {}

    public ProgrammeRecommender(Long participationId,
                                String activite,
                                LocalTime heureDebut,
                                LocalTime heureFin,
                                Ambiance ambiance,
                                String justification) {

        this.participationId = participationId;
        this.activite = activite;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ambiance = ambiance;
        this.justification = justification;
        this.recommande = true;
    }

    public boolean estValide() {
        return heureDebut != null && heureFin != null && heureFin.isAfter(heureDebut);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipationId() { return participationId; }
    public String getActivite() { return activite; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public Ambiance getAmbiance() { return ambiance; }
    public String getJustification() { return justification; }
    public boolean isRecommande() { return recommande; }
}
