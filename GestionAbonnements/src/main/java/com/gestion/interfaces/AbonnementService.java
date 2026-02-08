package com.gestion.interfaces;

import com.gestion.criteria.AbonnementCriteria;
import com.gestion.entities.Abonnement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des abonnements
 * Définit les opérations CRUD avancées et workflows métier
 */
public interface AbonnementService {
    
    // Opérations CRUD de base
    Abonnement create(Abonnement abonnement);
    Optional<Abonnement> findById(Long id);
    List<Abonnement> findAll();
    List<Abonnement> findAll(String sortBy, String sortOrder);
    List<Abonnement> findByUserId(Long userId);

    // SCRUD : Recherche et tri avancés
    List<Abonnement> search(AbonnementCriteria criteria);
    BigDecimal getTotalPointsAccumules();
    BigDecimal getTotalPointsAccumulesByUserId(Long userId);
    Abonnement update(Abonnement abonnement);
    boolean delete(Long id);
    
    // Opérations de recherche et filtrage avancées
    List<Abonnement> findByStatut(Abonnement.StatutAbonnement statut);
    List<Abonnement> findByType(Abonnement.TypeAbonnement type);
    List<Abonnement> findByDateFinBefore(LocalDate date);
    List<Abonnement> findByDateFinBetween(LocalDate debut, LocalDate fin);
    List<Abonnement> findAbonnementsProchesExpiration(int jours);
    List<Abonnement> findByAutoRenew(boolean autoRenew);
    List<Abonnement> findByPointsMinimum(int pointsMin);
    
    // Opérations métier avancées
    Abonnement upgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType);
    Abonnement downgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType);
    boolean toggleAutoRenew(Long id, boolean autoRenew);
    Abonnement renouvelerAbonnement(Long id);
    boolean suspendreAbonnement(Long id, String raison);
    boolean reactiverAbonnement(Long id);
    
    // Gestion des points et avantages
    void ajouterPoints(Long id, int points);
    boolean utiliserPoints(Long id, int points);
    List<Abonnement> findTopUtilisateursParPoints(int limite);
    
    // Analytics et reporting
    long countByStatut(Abonnement.StatutAbonnement statut);
    long countByType(Abonnement.TypeAbonnement type);
    BigDecimal calculerRevenuTotal();
    BigDecimal calculerRevenuParMois(int mois, int annee);
    List<Abonnement> findAbonnementsRisqueChurn(double seuil);
    double calculerTauxRetention(int mois);
    
    // Validation et contraintes métier
    boolean validerAbonnement(Abonnement abonnement);
    boolean verifierDisponiteUpgrade(Long id, Abonnement.TypeAbonnement nouveauType);
    boolean peutEtreSupprime(Long id);
    
    // Notifications et workflows
    void envoyerRappelExpiration(Long id);
    void envoyerConfirmationRenouvellement(Long id);
    void envoyerNotificationChangementStatut(Long id, Abonnement.StatutAbonnement ancienStatut);
    
    // Intégration avec d'autres modules
    List<Abonnement> findAbonnementsAvecParticipationsActives();
    List<Abonnement> findAbonnementsSansParticipation(int derniersMois);
    boolean synchroniserAvecPaiement(Long id);
}
