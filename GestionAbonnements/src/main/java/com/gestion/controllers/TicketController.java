/*package com.gestion.controllers;

import com.gestion.entities.Ticket;
import com.gestion.interfaces.TicketService;
import com.gestion.services.TicketServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur SCRUD pour les tickets.
 * Expose : Create, Read, Update, Delete, Search, Tri.
 * Gère la création de tickets/badges selon les coordonnées et choix de l'utilisateur
 */
/*public class TicketController {
    private final TicketService ticketService = new TicketServiceImpl();

    public Ticket create(Ticket ticket) {
        return ticketService.create(ticket);
    }

    public Ticket getById(Long id) {
        return ticketService.findById(id).orElse(null);
    }

    public List<Ticket> getAll() {
        return ticketService.findAll();
    }

    public List<Ticket> getAll(String sortBy, String sortOrder) {
        return ticketService.findAll(sortBy, sortOrder);
    }

    public Ticket update(Ticket ticket) {
        return ticketService.update(ticket);
    }

    public boolean delete(Long id) {
        return ticketService.delete(id);
    }

    public List<Ticket> getByParticipationId(Long participationId) {
        return ticketService.findByParticipationId(participationId);
    }

    public List<Ticket> getByUserId(Long userId) {
        return ticketService.findByUserId(userId);
    }

    public List<Ticket> getByType(String type) {
        try {
            return ticketService.findByType(Ticket.TypeTicket.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<Ticket> getByStatut(String statut) {
        try {
            return ticketService.findByStatut(Ticket.StatutTicket.valueOf(statut.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<Ticket> getByFormat(String format) {
        try {
            return ticketService.findByFormat(Ticket.FormatTicket.valueOf(format.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    /**
     * Crée un ticket selon les coordonnées et choix de l'utilisateur
     */
    /*public Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                       String type, 
                                       Double latitude, Double longitude, 
                                       String lieu, String format) {
        try {
            Ticket.TypeTicket typeTicket = Ticket.TypeTicket.valueOf(type.toUpperCase());
            Ticket.FormatTicket formatTicket = Ticket.FormatTicket.valueOf(format.toUpperCase());
            return ticketService.creerTicketSelonChoix(participationId, userId, typeTicket, 
                                                       latitude, longitude, lieu, formatTicket);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type ou format invalide: " + e.getMessage());
        }
    }*/


   /* public Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                        String type, Double latitude, Double longitude,
                                        String lieu, String format) {
        Ticket.TypeTicket typeTicket = Ticket.TypeTicket.valueOf(type.toUpperCase());
        Ticket.FormatTicket formatTicket = Ticket.FormatTicket.valueOf(format.toUpperCase());
        Ticket ticket = new Ticket(participationId, userId, typeTicket, latitude, longitude, lieu, formatTicket);
        return ticketService.create(ticket);
    }


    public List<Ticket> getByCoordonnees(Double latitude, Double longitude, Double rayonKm) {
        return ticketService.findByCoordonnees(latitude, longitude, rayonKm);
    }

    public List<Ticket> getByLieu(String lieu) {
        return ticketService.findByLieu(lieu);
    }

    public Ticket marquerCommeUtilise(Long id) {
        return ticketService.marquerCommeUtilise(id);
    }

    public Ticket annulerTicket(Long id) {
        return ticketService.annulerTicket(id);
    }

    public List<Ticket> getTicketsValides() {
        return ticketService.findTicketsValides();
    }

    public List<Ticket> getTicketsExpires() {
        return ticketService.findTicketsExpires();
    }

    public boolean validerTicket(String codeUnique) {
        return ticketService.validerTicket(codeUnique);
    }

    public List<Ticket> getByDateCreationBetween(LocalDateTime debut, LocalDateTime fin) {
        return ticketService.findByDateCreationBetween(debut, fin);
    }

    public List<Ticket> getByDateExpirationBefore(LocalDateTime date) {
        return ticketService.findByDateExpirationBefore(date);
    }
}*/


package com.gestion.controllers;

import com.gestion.entities.Ticket;
import com.gestion.interfaces.TicketService;
import com.gestion.services.TicketServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur SCRUD + fonctionnalités métier pour les tickets
 */
public class TicketController {
    private final TicketService ticketService = new TicketServiceImpl();

    public Ticket create(Ticket ticket) {
        return ticketService.create(ticket);
    }

    public Ticket getById(Long id) {
        return ticketService.findById(id).orElse(null);
    }

    public List<Ticket> getAll() {
        return ticketService.findAll();
    }

    public List<Ticket> getAll(String sortBy, String sortOrder) {
        return ticketService.findAll(sortBy, sortOrder);
    }

    public Ticket update(Ticket ticket) {
        return ticketService.update(ticket);
    }

    public boolean delete(Long id) {
        return ticketService.delete(id);
    }

    // Recherches
    public List<Ticket> getByParticipationId(Long participationId) {
        return ticketService.findByParticipationId(participationId);
    }

    public List<Ticket> getByUserId(Long userId) {
        return ticketService.findByUserId(userId);
    }

    public List<Ticket> getByType(String type) {
        return ticketService.findByType(Ticket.TypeTicket.valueOf(type.toUpperCase()));
    }

    public List<Ticket> getByStatut(String statut) {
        return ticketService.findByStatut(Ticket.StatutTicket.valueOf(statut.toUpperCase()));
    }

    public List<Ticket> getByFormat(String format) {
        return ticketService.findByFormat(Ticket.FormatTicket.valueOf(format.toUpperCase()));
    }

    public Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                        String typeStr, Double latitude, Double longitude,
                                        String lieu, String formatStr) {

        Ticket.TypeTicket type = Ticket.TypeTicket.valueOf(typeStr.toUpperCase());
        Ticket.FormatTicket format = Ticket.FormatTicket.valueOf(formatStr.toUpperCase());

        Ticket ticket = new Ticket(participationId, userId, type, latitude, longitude, lieu, format);
        return ticketService.create(ticket);
    }

    public Ticket marquerCommeUtilise(Long id) {
        return ticketService.marquerCommeUtilise(id);
    }

    public Ticket annulerTicket(Long id) {
        return ticketService.annulerTicket(id);
    }

    public boolean validerTicket(String codeUnique) {
        return ticketService.validerTicket(codeUnique);
    }
}
