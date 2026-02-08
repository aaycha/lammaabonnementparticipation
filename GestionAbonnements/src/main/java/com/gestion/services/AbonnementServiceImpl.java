package com.gestion.services;

import com.gestion.criteria.AbonnementCriteria;
import com.gestion.entities.Abonnement;
import com.gestion.interfaces.AbonnementService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implémentation du service de gestion des abonnements
 * Utilise une approche Stream pour le traitement des données
 */
public class AbonnementServiceImpl implements AbonnementService {
    private static final Logger logger = LoggerFactory.getLogger(AbonnementServiceImpl.class);
    private final MyConnection dbConnection;

    public AbonnementServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    @Override
    public Abonnement create(Abonnement abonnement) {
        if (!validerAbonnement(abonnement)) {
            throw new IllegalArgumentException("Abonnement invalide");
        }

        String sql = "INSERT INTO abonnements (user_id, type, date_debut, date_fin, prix, statut, " +
                    "avantages, auto_renew, points_accumules, churn_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, abonnement.getUserId());
            pstmt.setString(2, abonnement.getType().name());
            pstmt.setDate(3, Date.valueOf(abonnement.getDateDebut()));
            pstmt.setDate(4, Date.valueOf(abonnement.getDateFin()));
            pstmt.setBigDecimal(5, abonnement.getPrix());
            pstmt.setString(6, abonnement.getStatut().name());
            pstmt.setString(7, convertAvantagesToString(abonnement.getAvantages()));
            pstmt.setBoolean(8, abonnement.isAutoRenew());
            pstmt.setInt(9, abonnement.getPointsAccumules());
            pstmt.setDouble(10, abonnement.getChurnScore());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Création d'abonnement échouée, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    abonnement.setId(generatedKeys.getLong(1));
                    logger.info("Abonnement créé avec succès: ID {}", abonnement.getId());
                    envoyerConfirmationCreation(abonnement.getId());
                    return abonnement;
                } else {
                    throw new SQLException("Création d'abonnement échouée, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'abonnement: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer l'abonnement", e);
        }
    }

    @Override
    public Optional<Abonnement> findById(Long id) {
        String sql = "SELECT * FROM abonnements WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche d'abonnement par ID {}: {}", id, e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<Abonnement> findAll() {
        return findAll("dateDebut", "DESC");
    }

    @Override
    public List<Abonnement> findAll(String sortBy, String sortOrder) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAbonnement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les abonnements: {}", e.getMessage());
            return list;
        }
        return sortAbonnements(list, sortBy != null ? sortBy : "dateDebut", sortOrder != null ? sortOrder : "DESC");
    }

    @Override
    public List<Abonnement> search(AbonnementCriteria criteria) {
        if (criteria == null) return findAll();
        List<Abonnement> list = findAll();
        Stream<Abonnement> stream = list.stream();
        if (criteria.getStatut() != null) {
            stream = stream.filter(a -> a.getStatut() == criteria.getStatut());
        }
        if (criteria.getType() != null) {
            stream = stream.filter(a -> a.getType() == criteria.getType());
        }
        if (criteria.getUserId() != null) {
            stream = stream.filter(a -> criteria.getUserId().equals(a.getUserId()));
        }
        if (criteria.getDateFinAvant() != null) {
            stream = stream.filter(a -> a.getDateFin() != null && !a.getDateFin().isAfter(criteria.getDateFinAvant()));
        }
        if (criteria.getDateFinApres() != null) {
            stream = stream.filter(a -> a.getDateFin() != null && !a.getDateFin().isBefore(criteria.getDateFinApres()));
        }
        if (criteria.getAutoRenew() != null) {
            stream = stream.filter(a -> a.isAutoRenew() == criteria.getAutoRenew());
        }
        if (criteria.getPointsMinimum() != null) {
            stream = stream.filter(a -> a.getPointsAccumules() >= criteria.getPointsMinimum());
        }
        list = stream.toList();
        return sortAbonnements(list, criteria.getSortBy(), criteria.getSortOrder());
    }

    @Override
    public BigDecimal getTotalPointsAccumules() {
        return findAll().stream()
                .map(a -> BigDecimal.valueOf(a.getPointsAccumules()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalPointsAccumulesByUserId(Long userId) {
        return findByUserId(userId).stream()
                .map(a -> BigDecimal.valueOf(a.getPointsAccumules()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Abonnement> sortAbonnements(List<Abonnement> list, String sortBy, String sortOrder) {
        Comparator<Abonnement> cmp = switch (sortBy != null ? sortBy.toLowerCase() : "datedebut") {
            case "datefin" -> Comparator.comparing(Abonnement::getDateFin, Comparator.nullsLast(Comparator.naturalOrder()));
            case "prix" -> Comparator.comparing(Abonnement::getPrix, Comparator.nullsLast(Comparator.naturalOrder()));
            case "statut" -> Comparator.comparing(a -> a.getStatut().name());
            case "pointsaccumules" -> Comparator.comparingInt(Abonnement::getPointsAccumules);
            case "churnscore" -> Comparator.comparingDouble(Abonnement::getChurnScore);
            default -> Comparator.comparing(Abonnement::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).toList();
    }

    @Override
    public List<Abonnement> findByUserId(Long userId) {
        String sql = "SELECT * FROM abonnements WHERE user_id = ? ORDER BY date_debut DESC";
        List<Abonnement> abonnements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    abonnements.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche d'abonnements pour l'utilisateur {}: {}", userId, e.getMessage());
        }
        
        return abonnements;
    }

    @Override
    public Abonnement update(Abonnement abonnement) {
        if (!validerAbonnement(abonnement)) {
            throw new IllegalArgumentException("Abonnement invalide");
        }

        String sql = "UPDATE abonnements SET type = ?, date_debut = ?, date_fin = ?, prix = ?, " +
                    "statut = ?, avantages = ?, auto_renew = ?, points_accumules = ?, churn_score = ? " +
                    "WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, abonnement.getType().name());
            pstmt.setDate(2, Date.valueOf(abonnement.getDateDebut()));
            pstmt.setDate(3, Date.valueOf(abonnement.getDateFin()));
            pstmt.setBigDecimal(4, abonnement.getPrix());
            pstmt.setString(5, abonnement.getStatut().name());
            pstmt.setString(6, convertAvantagesToString(abonnement.getAvantages()));
            pstmt.setBoolean(7, abonnement.isAutoRenew());
            pstmt.setInt(8, abonnement.getPointsAccumules());
            pstmt.setDouble(9, abonnement.getChurnScore());
            pstmt.setLong(10, abonnement.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Mise à jour d'abonnement échouée, aucune ligne affectée.");
            }

            logger.info("Abonnement mis à jour avec succès: ID {}", abonnement.getId());
            return abonnement;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'abonnement {}: {}", abonnement.getId(), e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour l'abonnement", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) {
            throw new IllegalArgumentException("L'abonnement ne peut pas être supprimé (participations actives)");
        }

        String sql = "DELETE FROM abonnements WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Abonnement supprimé avec succès: ID {}", id);
            }
            
            return deleted;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'abonnement {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Abonnement> findByStatut(Abonnement.StatutAbonnement statut) {
        String sql = "SELECT * FROM abonnements WHERE statut = ? ORDER BY date_debut DESC";
        List<Abonnement> abonnements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, statut.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    abonnements.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche d'abonnements par statut {}: {}", statut, e.getMessage());
        }
        
        return abonnements;
    }

    @Override
    public List<Abonnement> findAbonnementsProchesExpiration(int jours) {
        String sql = "SELECT * FROM abonnements WHERE statut = 'ACTIF' AND " +
                    "date_fin BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY) " +
                    "ORDER BY date_fin ASC";
        List<Abonnement> abonnements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, jours);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    abonnements.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche d'abonnements proches de l'expiration: {}", e.getMessage());
        }
        
        return abonnements;
    }

    @Override
    public Abonnement upgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType) {
        Optional<Abonnement> optAbonnement = findById(id);
        if (optAbonnement.isEmpty()) {
            throw new IllegalArgumentException("Abonnement non trouvé");
        }

        Abonnement abonnement = optAbonnement.get();
        Abonnement.TypeAbonnement ancienType = abonnement.getType();
        
        if (!verifierDisponiteUpgrade(id, nouveauType)) {
            throw new IllegalArgumentException("Upgrade non disponible");
        }

        // Calcul du prix avec remise pour upgrade
        BigDecimal nouveauPrix = calculerPrixUpgrade(abonnement, nouveauType);
        abonnement.setType(nouveauType);
        abonnement.setPrix(nouveauPrix);
        abonnement.setStatut(Abonnement.StatutAbonnement.ACTIF);
        
        // Mise à jour des avantages
        abonnement.setAvantages(getAvantagesParType(nouveauType));
        
        Abonnement updated = update(abonnement);
        logger.info("Abonnement {} upgrade de {} vers {}", id, ancienType, nouveauType);
        
        return updated;
    }

    @Override
    public boolean toggleAutoRenew(Long id, boolean autoRenew) {
        String sql = "UPDATE abonnements SET auto_renew = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, autoRenew);
            pstmt.setLong(2, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Auto-renew mis à jour pour l'abonnement {}: {}", id, autoRenew);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour d'auto-renew pour l'abonnement {}: {}", id, e.getMessage());
        }
        
        return false;
    }

    @Override
    public boolean validerAbonnement(Abonnement abonnement) {
        if (abonnement == null) return false;
        if (abonnement.getUserId() == null || abonnement.getUserId() <= 0) return false;
        if (abonnement.getType() == null) return false;
        if (abonnement.getDateDebut() == null) return false;
        if (abonnement.getDateFin() == null) return false;
        if (abonnement.getPrix() == null || abonnement.getPrix().compareTo(BigDecimal.ZERO) <= 0) return false;
        if (abonnement.getDateFin().isBefore(abonnement.getDateDebut().plusMonths(1))) return false;
        
        return true;
    }

    @Override
    public boolean verifierDisponiteUpgrade(Long id, Abonnement.TypeAbonnement nouveauType) {
        Optional<Abonnement> optAbonnement = findById(id);
        if (optAbonnement.isEmpty()) return false;
        
        Abonnement abonnement = optAbonnement.get();
        return abonnement.estActif() && !abonnement.getType().equals(nouveauType);
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        // Vérifier s'il existe des participations actives liées
        String sql = "SELECT COUNT(*) FROM participations WHERE user_id = ? AND statut IN ('CONFIRME', 'EN_ATTENTE')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Optional<Abonnement> optAbonnement = findById(id);
            if (optAbonnement.isEmpty()) return false;
            
            pstmt.setLong(1, optAbonnement.get().getUserId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification de suppression de l'abonnement {}: {}", id, e.getMessage());
        }
        
        return false;
    }

    // Méthodes utilitaires privées
    private Abonnement mapResultSetToAbonnement(ResultSet rs) throws SQLException {
        Abonnement abonnement = new Abonnement();
        abonnement.setId(rs.getLong("id"));
        abonnement.setUserId(rs.getLong("user_id"));
        abonnement.setType(Abonnement.TypeAbonnement.valueOf(rs.getString("type")));
        abonnement.setDateDebut(rs.getDate("date_debut").toLocalDate());
        abonnement.setDateFin(rs.getDate("date_fin").toLocalDate());
        abonnement.setPrix(rs.getBigDecimal("prix"));
        abonnement.setStatut(Abonnement.StatutAbonnement.valueOf(rs.getString("statut")));
        abonnement.setAvantages(parseAvantagesFromString(rs.getString("avantages")));
        abonnement.setAutoRenew(rs.getBoolean("auto_renew"));
        abonnement.setPointsAccumules(rs.getInt("points_accumules"));
        abonnement.setChurnScore(rs.getDouble("churn_score"));
        return abonnement;
    }

    private String convertAvantagesToString(java.util.Map<String, Object> avantages) {
        if (avantages == null) return "{}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(avantages);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> parseAvantagesFromString(String avantagesStr) {
        if (avantagesStr == null || avantagesStr.trim().isEmpty()) return new java.util.HashMap<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(avantagesStr, java.util.Map.class);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }

    private BigDecimal calculerPrixUpgrade(Abonnement abonnement, Abonnement.TypeAbonnement nouveauType) {
        // Logique de calcul de prix pour upgrade
        BigDecimal prixBase = abonnement.getPrix();
        switch (nouveauType) {
            case PREMIUM:
                return prixBase.multiply(new BigDecimal("1.5"));
            case ANNUEL:
                return prixBase.multiply(new BigDecimal("0.9")); // 10% de remise
            default:
                return prixBase;
        }
    }

    private java.util.Map<String, Object> getAvantagesParType(Abonnement.TypeAbonnement type) {
        return switch (type) {
            case PREMIUM -> java.util.Map.of(
                "discounts", 20,
                "prioriteWaiting", true,
                "accesEvenementsExclusifs", true,
                "supportPrioritaire", true
            );
            case ANNUEL -> java.util.Map.of(
                "discounts", 15,
                "prioriteWaiting", false,
                "accesEvenementsExclusifs", false
            );
            case MENSUEL -> java.util.Map.of(
                "discounts", 10,
                "prioriteWaiting", false,
                "accesEvenementsExclusifs", false
            );
        };
    }

    private void envoyerConfirmationCreation(Long id) {
        // Implémentation de l'envoi de confirmation
        logger.info("Confirmation de création envoyée pour l'abonnement {}", id);
    }

    // Implémentations par défaut des autres méthodes (pour simplifier)
    @Override public List<Abonnement> findByType(Abonnement.TypeAbonnement type) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByDateFinBefore(LocalDate date) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByDateFinBetween(LocalDate debut, LocalDate fin) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByAutoRenew(boolean autoRenew) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByPointsMinimum(int pointsMin) { return new ArrayList<>(); }
    @Override public Abonnement downgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType) { return null; }
    @Override public Abonnement renouvelerAbonnement(Long id) { return null; }
    @Override public boolean suspendreAbonnement(Long id, String raison) { return false; }
    @Override public boolean reactiverAbonnement(Long id) { return false; }
    @Override public void ajouterPoints(Long id, int points) {}
    @Override public boolean utiliserPoints(Long id, int points) { return false; }
    @Override public List<Abonnement> findTopUtilisateursParPoints(int limite) { return new ArrayList<>(); }
    @Override public long countByStatut(Abonnement.StatutAbonnement statut) { return 0; }
    @Override public long countByType(Abonnement.TypeAbonnement type) { return 0; }
    @Override public BigDecimal calculerRevenuTotal() { return BigDecimal.ZERO; }
    @Override public BigDecimal calculerRevenuParMois(int mois, int annee) { return BigDecimal.ZERO; }
    @Override public List<Abonnement> findAbonnementsRisqueChurn(double seuil) { return new ArrayList<>(); }
    @Override public double calculerTauxRetention(int mois) { return 0.0; }
    @Override public void envoyerRappelExpiration(Long id) {}
    @Override public void envoyerConfirmationRenouvellement(Long id) {}
    @Override public void envoyerNotificationChangementStatut(Long id, Abonnement.StatutAbonnement ancienStatut) {}
    @Override public List<Abonnement> findAbonnementsAvecParticipationsActives() { return new ArrayList<>(); }
    @Override public List<Abonnement> findAbonnementsSansParticipation(int derniersMois) { return new ArrayList<>(); }
    @Override public boolean synchroniserAvecPaiement(Long id) { return false; }
}
