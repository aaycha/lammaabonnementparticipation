package com.gestion.criteria;

import com.gestion.entities.Ticket;

import java.time.LocalDateTime;

/**
 * Critères de recherche et tri pour les tickets.
 * Permet de filtrer par type, statut, format, coordonnées, etc.
 */
public class TicketCriteria {
    private Long participationId;
    private Long userId;
    private Ticket.TypeTicket type;
    private Ticket.StatutTicket statut;
    private Ticket.FormatTicket format;
    private Double latitude;
    private Double longitude;
    private Double rayonKm;
    private String lieu;
    private LocalDateTime dateCreationFrom;
    private LocalDateTime dateCreationTo;
    private LocalDateTime dateExpirationBefore;
    private String sortBy;   // dateCreation, statut, type, format, dateExpiration
    private String sortOrder; // ASC, DESC

    public TicketCriteria() {
        this.sortBy = "dateCreation";
        this.sortOrder = "DESC";
    }

    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long participationId) { this.participationId = participationId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Ticket.TypeTicket getType() { return type; }
    public void setType(Ticket.TypeTicket type) { this.type = type; }
    
    public Ticket.StatutTicket getStatut() { return statut; }
    public void setStatut(Ticket.StatutTicket statut) { this.statut = statut; }
    
    public Ticket.FormatTicket getFormat() { return format; }
    public void setFormat(Ticket.FormatTicket format) { this.format = format; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getRayonKm() { return rayonKm; }
    public void setRayonKm(Double rayonKm) { this.rayonKm = rayonKm; }
    
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    
    public LocalDateTime getDateCreationFrom() { return dateCreationFrom; }
    public void setDateCreationFrom(LocalDateTime dateCreationFrom) { this.dateCreationFrom = dateCreationFrom; }
    
    public LocalDateTime getDateCreationTo() { return dateCreationTo; }
    public void setDateCreationTo(LocalDateTime dateCreationTo) { this.dateCreationTo = dateCreationTo; }
    
    public LocalDateTime getDateExpirationBefore() { return dateExpirationBefore; }
    public void setDateExpirationBefore(LocalDateTime dateExpirationBefore) { this.dateExpirationBefore = dateExpirationBefore; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy != null ? sortBy : "dateCreation"; }
    
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { 
        this.sortOrder = "DESC".equalsIgnoreCase(sortOrder != null ? sortOrder : "") ? "DESC" : "ASC"; 
    }
}
