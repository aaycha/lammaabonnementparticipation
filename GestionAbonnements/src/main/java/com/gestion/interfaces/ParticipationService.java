/*package com.gestion.interfaces;

import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.Participation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des participations
 * Définit les opérations CRUD avancées et workflows métier
 */
/*public interface ParticipationService {
    
    // Opérations CRUD de base
    Participation create(Participation participation);
    List<Participation> findById(Long id);
    List<Participation> findAll();
    List<Participation> findAll(String sortBy, String sortOrder);

    Optional<Participation> findOneById(Long id);

    List<Participation> findByUserId(Long userId);
    List<Participation> findByEvenementId(Long evenementId);

    // SCRUD : Recherche et tri avancés
    List<Participation> search(ParticipationCriteria criteria);
    Participation update(Participation participation);
    boolean delete(Long id);
    
    // Opérations de recherche et filtrage avancées
    List<Participation> findByStatut(Participation.StatutParticipation statut);
    List<Participation> findByType(Participation.TypeParticipation type);
    List<Participation> findByContexteSocial(Participation.ContexteSocial contexte);
    List<Participation> findByDateInscriptionBetween(LocalDateTime debut, LocalDateTime fin);
    List<Participation> findByHebergementNuitsMinimum(int nuitsMin);
    List<Participation> findParticipationsConfirmees();
    List<Participation> findParticipationsEnAttente();
    List<Participation> findListeAttente(Long evenementId);

    // Opérations métier avancées
    Participation confirmerParticipation(Long id);
    Participation annulerParticipation(Long id, String raison);
    Participation ajouterListeAttente(Long id);
    Participation promouvoirListeAttente(Long id);
    boolean verifierDisponibiliteEvenement(Long evenementId);
    int getPlacesDisponibles(Long evenementId);
    int getNombreParticipantsConfirmes(Long evenementId);
    
    // Gestion des hébergements
    List<Participation> findAvecHebergement();
    Participation modifierHebergement(Long id, int nouvellesNuits);
    boolean validerHebergement(Participation participation);
    
    // Gestion des badges et gamification
    void attribuerBadge(Long id);
    List<Participation> findByBadge(String badge);
    List<Participation> findParticipationsAvecBadge();
    int calculerPointsParticipation(Long userId);
    
    // Matching et recommandations de groupe
    List<Participation> suggestionsMatchingGroupe(Long participationId);
    List<Participation> findParticipationsSimilaires(Long userId, Participation.ContexteSocial contexte);
    boolean creerMatchingGroupe(List<Long> participationIds);
    
    // Analytics et reporting
    long countByStatut(Participation.StatutParticipation statut);
    long countByType(Participation.TypeParticipation type);
    long countByContexteSocial(Participation.ContexteSocial contexte);
    List<Participation> findParticipationsPeriod(LocalDateTime debut, LocalDateTime fin);
    double calculerTauxConfirmation(Long evenementId);
    double calculerTauxAnnulation(Long evenementId);
    
    // Validation et contraintes métier
    boolean validerParticipation(Participation participation);
    boolean verifierConflitDates(Long userId, Long evenementId);
    boolean peutEtreSupprimee(Long id);

    
    // Notifications et workflows
    void envoyerConfirmationInscription(Long id);
    void envoyerNotificationAnnulation(Long id);
    void envoyerNotificationConfirmation(Long id);
    void notifierListeAttente(Long evenementId);
    void envoyerRappelEvenement(Long id);
    
    // Intégration avec d'autres modules
    List<Participation> findParticipationsAvecRecommandations();
    boolean synchroniserAvecTransport(Long id);
    boolean synchroniserAvecPaiement(Long id);
    List<Participation> findParticipationsAbonnementPremium();
    
    // Export et intégration externe
    String exporterCalendrier(Long userId);
    List<Participation> importerDonneesExterne(String source);
    boolean integrerCalendrierExterne(Long userId, String icalData);

    boolean isAlreadyParticipating(Long userId, Long evenementId);
}*/


package com.gestion.interfaces;

import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.Participation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des participations
 * Définit les opérations CRUD avancées et workflows métier
 */
public interface ParticipationService {

    // ────────────────────────────────────────────────
    // Opérations CRUD de base
    // ────────────────────────────────────────────────
    Participation create(Participation participation);

    /**
     * Recherche une participation par son ID
     * @return Optional contenant la participation si trouvée, sinon vide
     */
    Optional<Participation> findById(Long id);

    List<Participation> findAll();

    List<Participation> findAll(String sortBy, String sortOrder);

    Optional<Participation> findOneById(Long id);

    List<Participation> findByUserId(Long userId);

    List<Participation> findByEvenementId(Long evenementId);

    // ────────────────────────────────────────────────
    // Recherche et tri avancés
    // ────────────────────────────────────────────────
    List<Participation> search(ParticipationCriteria criteria);

    Participation update(Participation participation);

    boolean delete(Long id);

    // ────────────────────────────────────────────────
    // Filtres spécifiques
    // ────────────────────────────────────────────────
    List<Participation> findByStatut(Participation.StatutParticipation statut);

    List<Participation> findByType(Participation.TypeParticipation type);

    List<Participation> findByContexteSocial(Participation.ContexteSocial contexte);

    List<Participation> findByDateInscriptionBetween(LocalDateTime debut, LocalDateTime fin);

    List<Participation> findByHebergementNuitsMinimum(int nuitsMin);

    List<Participation> findParticipationsConfirmees();

    List<Participation> findParticipationsEnAttente();

    List<Participation> findListeAttente(Long evenementId);

    // ────────────────────────────────────────────────
    // Opérations métier avancées
    // ────────────────────────────────────────────────
    Participation confirmerParticipation(Long id);

    Participation annulerParticipation(Long id, String raison);

    Participation ajouterListeAttente(Long id);

    Participation promouvoirListeAttente(Long id);

    boolean verifierDisponibiliteEvenement(Long evenementId);

    int getPlacesDisponibles(Long evenementId);

    int getNombreParticipantsConfirmes(Long evenementId);

    // ────────────────────────────────────────────────
    // Gestion des hébergements
    // ────────────────────────────────────────────────
    List<Participation> findAvecHebergement();

    Participation modifierHebergement(Long id, int nouvellesNuits);

    boolean validerHebergement(Participation participation);

    // ────────────────────────────────────────────────
    // Gestion des badges et gamification
    // ────────────────────────────────────────────────
    void attribuerBadge(Long id);

    List<Participation> findByBadge(String badge);

    List<Participation> findParticipationsAvecBadge();

    int calculerPointsParticipation(Long userId);

    // ────────────────────────────────────────────────
    // Matching et recommandations de groupe
    // ────────────────────────────────────────────────
    List<Participation> suggestionsMatchingGroupe(Long participationId);

    List<Participation> findParticipationsSimilaires(Long userId, Participation.ContexteSocial contexte);

    boolean creerMatchingGroupe(List<Long> participationIds);

    // ────────────────────────────────────────────────
    // Analytics et reporting
    // ────────────────────────────────────────────────
    long countByStatut(Participation.StatutParticipation statut);

    long countByType(Participation.TypeParticipation type);

    long countByContexteSocial(Participation.ContexteSocial contexte);

    List<Participation> findParticipationsPeriod(LocalDateTime debut, LocalDateTime fin);

    double calculerTauxConfirmation(Long evenementId);

    double calculerTauxAnnulation(Long evenementId);

    // ────────────────────────────────────────────────
    // Validation et contraintes métier
    // ────────────────────────────────────────────────
    boolean validerParticipation(Participation participation);

    boolean verifierConflitDates(Long userId, Long evenementId);

    boolean peutEtreSupprimee(Long id);

    // ────────────────────────────────────────────────
    // Notifications et workflows
    // ────────────────────────────────────────────────
    void envoyerConfirmationInscription(Long id);

    void envoyerNotificationAnnulation(Long id);

    void envoyerNotificationConfirmation(Long id);

    void notifierListeAttente(Long evenementId);

    void envoyerRappelEvenement(Long id);

    // ────────────────────────────────────────────────
    // Intégration avec d'autres modules
    // ────────────────────────────────────────────────
    List<Participation> findParticipationsAvecRecommandations();

    boolean synchroniserAvecTransport(Long id);

    boolean synchroniserAvecPaiement(Long id);

    List<Participation> findParticipationsAbonnementPremium();

    // ────────────────────────────────────────────────
    // Export et intégration externe
    // ────────────────────────────────────────────────
    String exporterCalendrier(Long userId);

    List<Participation> importerDonneesExterne(String source);

    boolean integrerCalendrierExterne(Long userId, String icalData);

    // ────────────────────────────────────────────────
    // Vérification existence
    // ────────────────────────────────────────────────
    boolean isAlreadyParticipating(Long userId, Long evenementId);
}


