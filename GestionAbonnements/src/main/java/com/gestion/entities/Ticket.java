/*package com.gestion.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * Entité représentant un ticket ou badge pour une participation
 * Créé selon les coordonnées et choix de l'utilisateur
 */
/*@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ticket {
    private Long id;
    private Long participationId;
    private Long userId;
    private TypeTicket type;
    private String codeUnique;
    private Double latitude;
    private Double longitude;
    private String lieu;
    private StatutTicket statut;
    private FormatTicket format;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateCreation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateExpiration;
    private String qrCode;
    private String informationsSupplementaires;

    /**
     * Types de tickets possibles
     */
    /*public enum TypeTicket {
        TICKET("Ticket d'entrée"),
        BADGE("Badge d'identification"),
        PASS("Pass événement");

        private final String label;

        TypeTicket(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Statuts possibles pour un ticket
     */
    /*public enum StatutTicket {
        VALIDE("Valide"),
        UTILISE("Utilisé"),
        EXPIRE("Expiré"),
        ANNULE("Annulé");

        private final String label;

        StatutTicket(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Formats de ticket possibles
     */
    /*public enum FormatTicket {
        NUMERIQUE("Numérique"),
        PHYSIQUE("Physique"),
        HYBRIDE("Hybride");

        private final String label;

        FormatTicket(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // Constructeurs
    /*public Ticket() {}

    public Ticket(Long participationId, Long userId, TypeTicket type, 
                  Double latitude, Double longitude, String lieu, FormatTicket format) {
        this.participationId = participationId;
        this.userId = userId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lieu = lieu;
        this.format = format;
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutTicket.VALIDE;
        this.codeUnique = genererCodeUnique();
        this.qrCode = genererQRCode();
        
        // Date d'expiration par défaut : 7 jours après création
        this.dateExpiration = dateCreation.plusDays(7);
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParticipationId() {
        return participationId;
    }

    public void setParticipationId(Long participationId) {
        this.participationId = participationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TypeTicket getType() {
        return type;
    }

    public void setType(TypeTicket type) {
        this.type = type;
    }

    public String getCodeUnique() {
        return codeUnique;
    }

    public void setCodeUnique(String codeUnique) {
        this.codeUnique = codeUnique;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public StatutTicket getStatut() {
        return statut;
    }

    public void setStatut(StatutTicket statut) {
        this.statut = statut;
    }

    public FormatTicket getFormat() {
        return format;
    }

    public void setFormat(FormatTicket format) {
        this.format = format;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getInformationsSupplementaires() {
        return informationsSupplementaires;
    }

    public void setInformationsSupplementaires(String informationsSupplementaires) {
        this.informationsSupplementaires = informationsSupplementaires;
    }

    // Méthodes utilitaires
    private String genererCodeUnique() {
        return "TKT-" + System.currentTimeMillis() + "-" + 
               (participationId != null ? participationId : "0");
    }

    private String genererQRCode() {
        return "QR-" + codeUnique + "-" + 
               (latitude != null ? latitude.toString() : "0") + "-" +
               (longitude != null ? longitude.toString() : "0");
    }

    public boolean estValide() {
        return statut == StatutTicket.VALIDE && 
               dateExpiration != null && 
               dateExpiration.isAfter(LocalDateTime.now());
    }

    public void marquerCommeUtilise() {
        this.statut = StatutTicket.UTILISE;
    }

    public void annuler() {
        this.statut = StatutTicket.ANNULE;
    }

    public void prolongerExpiration(int jours) {
        if (dateExpiration != null) {
            this.dateExpiration = dateExpiration.plusDays(jours);
        }
    }

    public String getCoordonneesFormatees() {
        if (latitude != null && longitude != null) {
            return String.format("%.6f, %.6f", latitude, longitude);
        }
        return lieu != null ? lieu : "Non spécifié";
    }

    @Override
    public String toString() {
        return String.format("Ticket{id=%d, participationId=%d, userId=%d, type=%s, code=%s, statut=%s, format=%s}", 
                           id, participationId, userId, type, codeUnique, statut, format);
    }
}*/



/*package com.gestion.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ticket {
    private Long id;
    private Long participationId;
    private Long userId;
    private TypeTicket type;
    private String codeUnique;
    private Double latitude;
    private Double longitude;
    private String lieu;
    private StatutTicket statut;
    private FormatTicket format;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateExpiration;

    private String qrCode;
    private String informationsSupplementaires;

    // Types de tickets
    public enum TypeTicket { TICKET, BADGE, PASS }
    // Statuts possibles
    public enum StatutTicket { VALIDE, UTILISE, EXPIRE, ANNULE }
    // Formats possibles
    public enum FormatTicket { NUMERIQUE, PHYSIQUE, HYBRIDE }

    // Constructeurs
    public Ticket() {}

    public Ticket(Long participationId, Long userId, TypeTicket type,
                  Double latitude, Double longitude, String lieu, FormatTicket format) {
        this.participationId = participationId;
        this.userId = userId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lieu = lieu;
        this.format = format;
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutTicket.VALIDE;
        this.codeUnique = genererCodeUnique();
        this.qrCode = genererQRCode();
        this.dateExpiration = dateCreation.plusDays(7);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long participationId) { this.participationId = participationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public TypeTicket getType() { return type; }
    public void setType(TypeTicket type) { this.type = type; }
    public String getCodeUnique() { return codeUnique; }
    public void setCodeUnique(String codeUnique) { this.codeUnique = codeUnique; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public StatutTicket getStatut() { return statut; }
    public void setStatut(StatutTicket statut) { this.statut = statut; }
    public FormatTicket getFormat() { return format; }
    public void setFormat(FormatTicket format) { this.format = format; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getInformationsSupplementaires() { return informationsSupplementaires; }
    public void setInformationsSupplementaires(String informationsSupplementaires) {
        this.informationsSupplementaires = informationsSupplementaires;
    }

    // Méthodes utilitaires
    private String genererCodeUnique() {
        return "TKT-" + System.currentTimeMillis() + "-" +
                (participationId != null ? participationId : "0");
    }

    private String genererQRCode() {
        return "QR-" + codeUnique + "-" +
                (latitude != null ? latitude.toString() : "0") + "-" +
                (longitude != null ? longitude.toString() : "0");
    }

    public boolean estValide() {
        return statut == StatutTicket.VALIDE &&
                dateExpiration != null &&
                dateExpiration.isAfter(LocalDateTime.now());
    }

    public void marquerCommeUtilise() { this.statut = StatutTicket.UTILISE; }
    public void annuler() { this.statut = StatutTicket.ANNULE; }

    @Override
    public String toString() {
        return String.format("Ticket{id=%d, participationId=%d, userId=%d, type=%s, code=%s, statut=%s, format=%s}",
                id, participationId, userId, type, codeUnique, statut, format);
    }
}*/



package com.gestion.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Ticket {

    private Long id;
    private Long participationId;
    private Long userId;
    private TypeTicket type;
    private String codeUnique;
    private Double latitude;
    private Double longitude;
    private String lieu;
    private StatutTicket statut;
    private FormatTicket format;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private String qrCode;
    private String informationsSupplementaires;

    public enum TypeTicket {
        TICKET("Ticket d’entrée simple"),
        BADGE("Badge d’identification officiel"),
        PASS("Pass VIP / accès complet");

        private final String label;
        TypeTicket(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum StatutTicket {
        VALIDE("Valide et actif"),
        UTILISE("Déjà utilisé"),
        EXPIRE("Date d’expiration dépassée"),
        ANNULE("Annulé par l’organisateur ou l’utilisateur");

        private final String label;
        StatutTicket(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum FormatTicket {
        NUMERIQUE("QR code numérique"),
        PHYSIQUE("Ticket imprimé"),
        HYBRIDE("Numérique + physique");

        private final String label;
        FormatTicket(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public Ticket() {
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutTicket.VALIDE;
    }

    public Ticket(Long participationId, Long userId, TypeTicket type,
                  Double latitude, Double longitude, String lieu, FormatTicket format) {
        this();
        this.participationId = participationId;
        this.userId = userId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lieu = lieu;
        this.format = format;
        this.codeUnique = genererCodeUnique();
        this.qrCode = genererQRCode();
        this.dateExpiration = dateCreation.plusDays(7);
    }

    // Getters & Setters (inchangés mais complets)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long participationId) { this.participationId = participationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public TypeTicket getType() { return type; }
    public void setType(TypeTicket type) { this.type = type; }

    public String getCodeUnique() { return codeUnique; }
    public void setCodeUnique(String codeUnique) { this.codeUnique = codeUnique; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public StatutTicket getStatut() { return statut; }
    public void setStatut(StatutTicket statut) { this.statut = statut; }

    public FormatTicket getFormat() { return format; }
    public void setFormat(FormatTicket format) { this.format = format; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getInformationsSupplementaires() { return informationsSupplementaires; }
    public void setInformationsSupplementaires(String infos) { this.informationsSupplementaires = infos; }

    // Méthodes utilitaires
    private String genererCodeUnique() {
        return "TKT-" + System.currentTimeMillis() + "-" + (participationId != null ? participationId : "X");
    }

    private String genererQRCode() {
        return "QR-" + codeUnique + "-" + (latitude != null ? latitude : "0") + "-" + (longitude != null ? longitude : "0");
    }

    public boolean estValide() {
        return statut == StatutTicket.VALIDE &&
                (dateExpiration == null || dateExpiration.isAfter(LocalDateTime.now()));
    }

    public void marquerCommeUtilise() {
        if (estValide()) {
            this.statut = StatutTicket.UTILISE;
        }
    }

    public void annuler() {
        this.statut = StatutTicket.ANNULE;
    }

    @Override
    public String toString() {
        return String.format(
                "Ticket #%d | Type: %-8s | Statut: %-8s | Format: %-9s | Code: %s | Lieu: %s | Expiration: %s",
                id, type, statut, format, codeUnique, lieu, dateExpiration != null ? dateExpiration : "Aucune"
        );
    }
}

