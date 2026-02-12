/*package com.gestion.interfaces;

import com.gestion.entities.Ticket;
import com.gestion.services.TicketServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des tickets
 * Définit les opérations CRUD et workflows métier utilisant Stream
 */
/*public interface TicketService {
    Ticket getById(Long id);
    List<Ticket> getAll();
    List<Ticket> getAll(String sortBy, String sortOrder);
    // Opérations CRUD de base
    Ticket create(Ticket ticket);
    Optional<Ticket> findById(Long id);
    List<Ticket> findAll();
    List<Ticket> findAll(String sortBy, String sortOrder);
    Ticket update(Ticket ticket);
    boolean delete(Long id);

    // Recherche par critères
    List<Ticket> findByParticipationId(Long participationId);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByType(Ticket.TypeTicket type);
    List<Ticket> findByStatut(Ticket.StatutTicket statut);
    List<Ticket> findByFormat(Ticket.FormatTicket format);

    // Recherche par coordonnées
    List<Ticket> findByCoordonnees(Double latitude, Double longitude, Double rayonKm);
    List<Ticket> findByLieu(String lieu);

    // Opérations métier
    Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                 Ticket.TypeTicket type,
                                 Double latitude, Double longitude,
                                 String lieu, Ticket.FormatTicket format);
    Ticket marquerCommeUtilise(Long id);
    Ticket annulerTicket(Long id);
    List<Ticket> findTicketsValides();
    List<Ticket> findTicketsExpires();
    boolean validerTicket(String codeUnique);

    // Recherche temporelle
    List<Ticket> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);
    List<Ticket> findByDateExpirationBefore(LocalDateTime date);

    // Validation
    boolean validerTicket(Ticket ticket);
    boolean peutEtreSupprime(Long id);
}*/
package com.gestion.interfaces;

import com.gestion.entities.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketService {

    // CRUD de base
    Ticket create(Ticket ticket);
    Optional<Ticket> findById(Long id);
    List<Ticket> findAll();
    List<Ticket> findAll(String sortBy, String sortOrder);
    Ticket update(Ticket ticket);
    boolean delete(Long id);

    // Alias pratiques pour le menu console (implémentés en default)
    default Ticket getById(Long id) {
        return findById(id).orElse(null);
    }

    default List<Ticket> getAll() {
        return findAll();
    }

    default List<Ticket> getAll(String sortBy, String sortOrder) {
        return findAll(sortBy, sortOrder);
    }

    // Recherche par critères
    List<Ticket> findByParticipationId(Long participationId);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByType(Ticket.TypeTicket type);
    List<Ticket> findByStatut(Ticket.StatutTicket statut);
    List<Ticket> findByFormat(Ticket.FormatTicket format);

    // Recherche géographique
    List<Ticket> findByCoordonnees(Double latitude, Double longitude, Double rayonKm);
    List<Ticket> findByLieu(String lieu);

    // Opérations métier
    Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                 Ticket.TypeTicket type,
                                 Double latitude, Double longitude,
                                 String lieu, Ticket.FormatTicket format);

    Ticket marquerCommeUtilise(Long id);
    Ticket annulerTicket(Long id);
    List<Ticket> findTicketsValides();
    List<Ticket> findTicketsExpires();
    boolean validerTicket(String codeUnique);

    // Recherche temporelle
    List<Ticket> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);
    List<Ticket> findByDateExpirationBefore(LocalDateTime date);

    // Validation et suppression
    boolean validerTicket(Ticket ticket);
    boolean peutEtreSupprime(Long id);
}
