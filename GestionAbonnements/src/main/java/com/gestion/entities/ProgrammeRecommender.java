
/*package com.gestion.entities;

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
}*/

package com.gestion.entities;

import java.time.LocalTime;
import java.util.Objects;

public class ProgrammeRecommender {

    private Long id;
    private Long participationId;  // ← ON UNIFIE : camelCase en Java

    private String activite;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    private Ambiance ambiance;
    private String justification;
    private boolean recommande = true;

    public enum Ambiance {
        CALME, FESTIVE, SOCIALE, AVENTURE, CULTURELLE
    }

    public ProgrammeRecommender() {
    }

    public ProgrammeRecommender(Long participationId, String activite, LocalTime heureDebut,
                                LocalTime heureFin, Ambiance ambiance, String justification) {
        this.participationId = participationId;
        this.activite = activite;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ambiance = ambiance;
        this.justification = justification;
    }

    public boolean estValide() {
        return heureDebut != null && heureFin != null &&
                !heureFin.isBefore(heureDebut) && !heureDebut.equals(heureFin);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long participationId) { this.participationId = participationId; }

    public String getActivite() { return activite; }
    public void setActivite(String activite) { this.activite = activite; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public Ambiance getAmbiance() { return ambiance; }
    public void setAmbiance(Ambiance ambiance) { this.ambiance = ambiance; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public boolean isRecommande() { return recommande; }
    public void setRecommande(boolean recommande) { this.recommande = recommande; }

    @Override
    public String toString() {
        return String.format(
                "Programme: %-25s | %s → %s | Ambiance: %-10s | %s",
                activite, heureDebut, heureFin, ambiance, justification
        );
    }
}