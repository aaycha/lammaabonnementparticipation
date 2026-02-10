

/*package com.gestion.services;

import com.gestion.criteria.AbonnementCriteria;
import com.gestion.entities.Abonnement;
import com.gestion.interfaces.AbonnementService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

/**
 * Service complet de gestion des abonnements
 */
/*public class AbonnementServiceImpl implements AbonnementService {
    private static final Logger logger = LoggerFactory.getLogger(AbonnementServiceImpl.class);
    private final MyConnection dbConnection;

    public AbonnementServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    // -------------------- CREATE --------------------
    @Override
    public Abonnement create(Abonnement abonnement) {
        if (!validerAbonnement(abonnement)) throw new IllegalArgumentException("Abonnement invalide");

        String sql = "INSERT INTO abonnements (user_id, type, date_debut, date_fin, prix, statut, avantages, auto_renew, points_accumules, churn_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            if (affectedRows == 0) throw new SQLException("Création d'abonnement échouée");

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    abonnement.setId(generatedKeys.getLong(1));
                    logger.info("Abonnement créé avec succès: ID {}", abonnement.getId());
                    return abonnement;
                } else throw new SQLException("Aucun ID généré pour l'abonnement");
            }
        } catch (SQLException e) {
            logger.error("Erreur create: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // -------------------- READ --------------------
    @Override
    public Optional<Abonnement> findById(Long id) {
        String sql = "SELECT * FROM abonnements WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToAbonnement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findById: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Abonnement> findAll() {
        return findAll("date_debut", "DESC");
    }

    @Override
    public List<Abonnement> findAll(String sortBy, String sortOrder) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToAbonnement(rs));

        } catch (SQLException e) {
            logger.error("Erreur findAll: {}", e.getMessage());
        }
        return sortAbonnements(list, sortBy, sortOrder);
    }

    @Override
    public List<Abonnement> findByUserId(Long userId) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE user_id = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAbonnement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findByUserId: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByStatut(Abonnement.StatutAbonnement statut) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE statut = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, statut.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAbonnement(rs));
            }

        } catch (SQLException e) {
            logger.error("Erreur findByStatut: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByType(Abonnement.TypeAbonnement type) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE type = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAbonnement(rs));
            }

        } catch (SQLException e) {
            logger.error("Erreur findByType: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findAbonnementsProchesExpiration(int jours) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE statut = 'ACTIF' AND date_fin BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY) ORDER BY date_fin ASC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jours);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAbonnement(rs));
            }

        } catch (SQLException e) {
            logger.error("Erreur findAbonnementsProchesExpiration: {}", e.getMessage());
        }
        return list;
    }

    // -------------------- UPDATE --------------------
    @Override
    public Abonnement update(Abonnement abonnement) {
        if (!validerAbonnement(abonnement)) throw new IllegalArgumentException("Abonnement invalide");

        String sql = "UPDATE abonnements SET type=?, date_debut=?, date_fin=?, prix=?, statut=?, avantages=?, auto_renew=?, points_accumules=?, churn_score=? WHERE id=?";
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

            pstmt.executeUpdate();
            logger.info("Abonnement mis à jour: ID {}", abonnement.getId());
            return abonnement;

        } catch (SQLException e) {
            logger.error("Erreur update: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean toggleAutoRenew(Long id, boolean autoRenew) {
        String sql = "UPDATE abonnements SET auto_renew=? WHERE id=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, autoRenew);
            pstmt.setLong(2, id);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logger.info("Auto-renew mis à jour pour abonnement {}", id);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Erreur toggleAutoRenew: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void ajouterPoints(Long id, int points) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Abonnement introuvable");
        Abonnement a = opt.get();
        a.setPointsAccumules(a.getPointsAccumules() + points);
        update(a);
    }

    @Override
    public boolean utiliserPoints(Long id, int points) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) return false;
        Abonnement a = opt.get();
        if (a.getPointsAccumules() < points) return false;
        a.setPointsAccumules(a.getPointsAccumules() - points);
        update(a);
        return true;
    }

    // -------------------- DELETE --------------------
    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) return false;

        String sql = "DELETE FROM abonnements WHERE id=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) logger.info("Abonnement supprimé: ID {}", id);
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Erreur delete: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) return false;
        Abonnement a = opt.get();
        if (a.getStatut() != Abonnement.StatutAbonnement.EXPIRE) return false;

        String sql = "SELECT COUNT(*) FROM participations WHERE user_id=? AND statut IN ('CONFIRME','EN_ATTENTE')";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, a.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            logger.error("Erreur peutEtreSupprime: {}", e.getMessage());
        }
        return false;
    }

    // -------------------- UTILITAIRES --------------------
    private Abonnement mapResultSetToAbonnement(ResultSet rs) throws SQLException {
        Abonnement a = new Abonnement();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setType(Abonnement.TypeAbonnement.valueOf(rs.getString("type")));
        a.setDateDebut(rs.getDate("date_debut").toLocalDate());
        a.setDateFin(rs.getDate("date_fin").toLocalDate());
        a.setPrix(rs.getBigDecimal("prix"));
        a.setStatut(Abonnement.StatutAbonnement.valueOf(rs.getString("statut")));
        a.setAvantages(parseAvantagesFromString(rs.getString("avantages")));
        a.setAutoRenew(rs.getBoolean("auto_renew"));
        a.setPointsAccumules(rs.getInt("points_accumules"));
        a.setChurnScore(rs.getDouble("churn_score"));
        return a;
    }

    private String convertAvantagesToString(Map<String, Object> avantages) {
        if (avantages == null) return "{}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(avantages);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAvantagesFromString(String str) {
        if (str == null || str.isBlank()) return new HashMap<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(str, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<Abonnement> sortAbonnements(List<Abonnement> list, String sortBy, String sortOrder) {
        Comparator<Abonnement> cmp = switch (sortBy != null ? sortBy.toLowerCase() : "date_debut") {
            case "datefin" -> Comparator.comparing(Abonnement::getDateFin, Comparator.nullsLast(Comparator.naturalOrder()));
            case "prix" -> Comparator.comparing(Abonnement::getPrix, Comparator.nullsLast(Comparator.naturalOrder()));
            case "statut" -> Comparator.comparing(a -> a.getStatut().name());
            default -> Comparator.comparing(Abonnement::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).toList();
    }

    @Override
    public boolean validerAbonnement(Abonnement abonnement) {
        if (abonnement == null) return false;
        if (abonnement.getUserId() == null || abonnement.getUserId() <= 0) return false;
        if (abonnement.getType() == null) return false;
        if (abonnement.getDateDebut() == null || abonnement.getDateFin() == null) return false;
        if (abonnement.getPrix() == null || abonnement.getPrix().compareTo(BigDecimal.ZERO) <= 0) return false;
        return true;
    }

    @Override
    public boolean verifierDisponiteUpgrade(Long id, Abonnement.TypeAbonnement nouveauType) {
        return false;
    }

    // -------------------- Méthodes par défaut pour interface --------------------
    @Override public List<Abonnement> search(AbonnementCriteria criteria) { return findAll(); }
    @Override public BigDecimal getTotalPointsAccumules() { return BigDecimal.ZERO; }
    @Override public BigDecimal getTotalPointsAccumulesByUserId(Long userId) { return BigDecimal.ZERO; }
    @Override public List<Abonnement> findByDateFinBefore(LocalDate date) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByDateFinBetween(LocalDate debut, LocalDate fin) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByAutoRenew(boolean autoRenew) { return new ArrayList<>(); }
    @Override public List<Abonnement> findByPointsMinimum(int pointsMin) { return new ArrayList<>(); }
    @Override public Abonnement upgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType) { return null; }
    @Override public Abonnement downgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType) { return null; }
    @Override public Abonnement renouvelerAbonnement(Long id) { return null; }
    @Override public boolean suspendreAbonnement(Long id, String raison) { return false; }
    @Override public boolean reactiverAbonnement(Long id) { return false; }
    @Override public List<Abonnement> findTopUtilisateursParPoints(int limite) { return new ArrayList<>(); }
    @Override public long countByStatut(Abonnement.StatutAbonnement statut) { return 0; }
    @Override public long countByType(Abonnement.TypeAbonnement type) { return 0; }
    @Override public BigDecimal calculerRevenuTotal() { return BigDecimal.ZERO; }
    @Override public BigDecimal calculerRevenuParMois(int mois, int annee) { return BigDecimal.ZERO; }
    @Override public List<Abonnement> findAbonnementsRisqueChurn(double seuil) { return new ArrayList<>(); }
    @Override public double calculerTauxRetention(int mois) { return 0; }
    @Override public void envoyerRappelExpiration(Long id) {}
    @Override public void envoyerConfirmationRenouvellement(Long id) {}
    @Override public void envoyerNotificationChangementStatut(Long id, Abonnement.StatutAbonnement ancienStatut) {}
    @Override public List<Abonnement> findAbonnementsAvecParticipationsActives() { return new ArrayList<>(); }
    @Override public List<Abonnement> findAbonnementsSansParticipation(int derniersMois) { return new ArrayList<>(); }
    @Override public boolean synchroniserAvecPaiement(Long id) { return false; }
}*/


package com.gestion.services;

import com.gestion.criteria.AbonnementCriteria;
import com.gestion.entities.Abonnement;
import com.gestion.interfaces.AbonnementService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des abonnements avec validation renforcée et messages d'erreur détaillés
 */
public class AbonnementServiceImpl implements AbonnementService {
    private static final Logger logger = LoggerFactory.getLogger(AbonnementServiceImpl.class);
    private final MyConnection dbConnection;

    public AbonnementServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    // ────────────────────────────────────────────────
    // VALIDATION RENFORCÉE
    // ────────────────────────────────────────────────

    public static class AbonnementValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private AbonnementValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public String getFormattedMessage() {
            if (valid) return "Validation réussie";
            return "Erreurs de validation :\n• " + String.join("\n• ", errors);
        }

        public static AbonnementValidationResult valid() {
            return new AbonnementValidationResult(true, List.of());
        }

        public static AbonnementValidationResult invalid(String... messages) {
            return new AbonnementValidationResult(false, Arrays.asList(messages));
        }

        public static AbonnementValidationResult invalid(Collection<String> messages) {
            return new AbonnementValidationResult(false, new ArrayList<>(messages));
        }
    }

    /**
     * Validation complète pour la création d'un abonnement
     */
    public AbonnementValidationResult validateForCreation(Abonnement abonnement) {
        List<String> errors = new ArrayList<>();
        if (abonnement == null) {
            errors.add("L'abonnement ne peut pas être null. Veuillez créer un objet Abonnement valide.");
            return AbonnementValidationResult.invalid(errors);
        }

        // ID utilisateur
        if (abonnement.getUserId() == null || abonnement.getUserId() <= 0) {
            errors.add("L'identifiant utilisateur est obligatoire et doit être positif. Exemple : 1 ou plus.");
        }

        // Type d'abonnement
        if (abonnement.getType() == null) {
            errors.add("Le type d'abonnement est obligatoire. Valeurs possibles : MENSUEL, ANNUEL, PREMIUM.");
        } else {
            try {
                Abonnement.TypeAbonnement.valueOf(abonnement.getType().name());
            } catch (IllegalArgumentException e) {
                errors.add("Type d'abonnement invalide : " + abonnement.getType() + ". Valeurs possibles : MENSUEL, ANNUEL, PREMIUM.");
            }
        }

        // Dates
        if (abonnement.getDateDebut() == null) {
            errors.add("La date de début est obligatoire. Format : AAAA-MM-JJ.");
        }
        if (abonnement.getDateFin() == null) {
            errors.add("La date de fin est obligatoire. Format : AAAA-MM-JJ.");
        }
        if (abonnement.getDateDebut() != null && abonnement.getDateFin() != null) {
            if (abonnement.getDateFin().isBefore(abonnement.getDateDebut())) {
                errors.add("La date de fin doit être supérieure à la date de début. Corrigez les dates.");
            }
            long days = ChronoUnit.DAYS.between(abonnement.getDateDebut(), abonnement.getDateFin());
            long monthsApprox = ChronoUnit.MONTHS.between(abonnement.getDateDebut(), abonnement.getDateFin());
            if (abonnement.getType() != null) {
                switch (abonnement.getType()) {
                    case MENSUEL:
                        if (monthsApprox < 1 || monthsApprox > 1 || days > 35) {
                            errors.add("Un abonnement MENSUEL doit durer environ 1 mois (28-35 jours). Ajustez les dates.");
                        }
                        break;
                    case ANNUEL:
                        if (monthsApprox < 11 || monthsApprox > 13 || days < 340 || days > 375) {
                            errors.add("Un abonnement ANNUEL doit durer environ 12 mois (±1 mois). Ajustez les dates.");
                        }
                        break;
                    case PREMIUM:
                        if (monthsApprox < 1) {
                            errors.add("Un abonnement PREMIUM doit durer au minimum 1 mois. Ajustez les dates.");
                        }
                        break;
                }
            }
            if (abonnement.getDateDebut().isBefore(LocalDate.now().minusDays(1))) {
                errors.add("La date de début ne peut pas être dans le passé (sauf aujourd'hui). Utilisez une date future ou actuelle.");
            }
        }

        // Prix selon type
        if (abonnement.getPrix() == null) {
            errors.add("Le prix est obligatoire. Exemple : 19.99");
        } else {
            if (abonnement.getPrix().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Le prix doit être strictement supérieur à 0. Exemple : 9.99 ou plus.");
            }
            if (abonnement.getPrix().scale() > 2) {
                errors.add("Le prix ne peut avoir que 2 décimales maximum. Exemple : 19.99");
            }
            if (abonnement.getType() != null) {
                BigDecimal minPrice, maxPrice;
                switch (abonnement.getType()) {
                    case MENSUEL:
                        minPrice = new BigDecimal("9.99");
                        maxPrice = new BigDecimal("29.99");
                        break;
                    case ANNUEL:
                        minPrice = new BigDecimal("79.99");
                        maxPrice = new BigDecimal("199.99");
                        break;
                    case PREMIUM:
                        minPrice = new BigDecimal("29.99");
                        maxPrice = new BigDecimal("99.99");
                        break;
                    default:
                        minPrice = BigDecimal.ZERO;
                        maxPrice = new BigDecimal("9999");
                }
                if (abonnement.getPrix().compareTo(minPrice) < 0) {
                    errors.add("Le prix est trop bas pour un abonnement " + abonnement.getType() + " (minimum : " + minPrice + " €). Augmentez le prix.");
                }
                if (abonnement.getPrix().compareTo(maxPrice) > 0) {
                    errors.add("Le prix est trop élevé pour un abonnement " + abonnement.getType() + " (maximum : " + maxPrice + " €). Diminuez le prix.");
                }
            }
        }

        // Statut à la création
        if (abonnement.getStatut() == null) {
            errors.add("Le statut est obligatoire. Valeurs possibles : ACTIF, EN_ATTENTE.");
        } else if (abonnement.getStatut() != Abonnement.StatutAbonnement.ACTIF && abonnement.getStatut() != Abonnement.StatutAbonnement.EN_ATTENTE) {
            errors.add("À la création, le statut doit être ACTIF ou EN_ATTENTE. Corrigez le statut.");
        }

        // Points et churn
        if (abonnement.getPointsAccumules() < 0) {
            errors.add("Les points accumulés ne peuvent pas être négatifs. Mettez 0 ou plus.");
        }
        if (abonnement.getPointsAccumules() > 10000) {
            errors.add("Les points accumulés semblent anormalement élevés (> 10 000). Vérifiez la valeur.");
        }
        if (abonnement.getChurnScore() < 0 || abonnement.getChurnScore() > 1) {
            errors.add("Le churn score doit être compris entre 0.0 et 1.0. Exemple : 0.5");
        }

        // Auto-renew + date fin
        if (abonnement.isAutoRenew() && abonnement.getDateFin() != null) {
            if (abonnement.getDateFin().isBefore(LocalDate.now().plusMonths(1))) {
                errors.add("L'auto-renouvellement n'est autorisé que si l'abonnement est valide au moins 1 mois. Prolongez la date de fin.");
            }
        }

        return errors.isEmpty() ? AbonnementValidationResult.valid() : AbonnementValidationResult.invalid(errors);
    }

    /**
     * Validation pour la mise à jour (plus permissive sur certaines règles)
     */
    public AbonnementValidationResult validateForUpdate(Abonnement abonnement) {
        List<String> errors = new ArrayList<>();
        if (abonnement == null) {
            errors.add("L'abonnement ne peut pas être null. Veuillez fournir un objet valide.");
            return AbonnementValidationResult.invalid(errors);
        }
        if (abonnement.getId() == null || abonnement.getId() <= 0) {
            errors.add("L'ID est obligatoire pour une mise à jour et doit être positif.");
        }

        // Réutilise la validation de création mais ignore certaines règles (ex: date début passé)
        AbonnementValidationResult base = validateForCreation(abonnement);
        if (!base.isValid()) {
            base.getErrors().stream()
                    .filter(err -> !err.contains("date de début ne peut pas être dans le passé"))
                    .forEach(errors::add);
        }

        // Règles spécifiques update
        if (abonnement.getDateDebut() != null && abonnement.getDateDebut().isBefore(LocalDate.now().minusMonths(3))) {
            errors.add("Il n'est pas possible de modifier la date de début pour un abonnement commencé il y a plus de 3 mois. Laissez la date actuelle.");
        }

        return errors.isEmpty() ? AbonnementValidationResult.valid() : AbonnementValidationResult.invalid(errors);
    }

    // ────────────────────────────────────────────────
    // CREATE
    // ────────────────────────────────────────────────

    @Override
    public Abonnement create(Abonnement abonnement) {
        AbonnementValidationResult validation = validateForCreation(abonnement);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getFormattedMessage());
        }
        String sql = """
                INSERT INTO abonnements (user_id, type, date_debut, date_fin, prix, statut, avantages, auto_renew, points_accumules, churn_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
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
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucune ligne insérée");
            }
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    abonnement.setId(keys.getLong(1));
                    logger.info("Abonnement créé → ID: {}", abonnement.getId());
                    return abonnement;
                }
            }
            throw new SQLException("Aucun ID généré");
        } catch (SQLException e) {
            logger.error("Erreur création abonnement", e);
            throw new RuntimeException("Échec création abonnement", e);
        }
    }

    // ────────────────────────────────────────────────
    // READ METHODS
    // ────────────────────────────────────────────────

    @Override
    public Optional<Abonnement> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide : doit être positif.");
        }
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
            logger.error("Erreur findById: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Abonnement> findAll() {
        return findAll("date_debut", "DESC");
    }

    @Override
    public List<Abonnement> findAll(String sortBy, String sortOrder) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements ORDER BY " + (sortBy != null ? sortBy : "date_debut") + " " + (sortOrder != null ? sortOrder : "DESC");
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAbonnement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID invalide : doit être positif.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE user_id = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByUserId: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByStatut(Abonnement.StatutAbonnement statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Statut obligatoire.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE statut = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByStatut: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByType(Abonnement.TypeAbonnement type) {
        if (type == null) {
            throw new IllegalArgumentException("Type obligatoire.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE type = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByType: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findAbonnementsProchesExpiration(int jours) {
        if (jours <= 0) {
            throw new IllegalArgumentException("Nombre de jours doit être positif.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE statut = 'ACTIF' AND date_fin BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY) ORDER BY date_fin ASC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jours);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findAbonnementsProchesExpiration: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByDateFinBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date obligatoire.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE date_fin < ? ORDER BY date_fin DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByDateFinBefore: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByDateFinBetween(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || fin.isBefore(debut)) {
            throw new IllegalArgumentException("Dates invalides : début et fin obligatoires, fin > début.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE date_fin BETWEEN ? AND ? ORDER BY date_fin ASC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(debut));
            pstmt.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByDateFinBetween: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByAutoRenew(boolean autoRenew) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE auto_renew = ? ORDER BY date_debut DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, autoRenew);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByAutoRenew: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findByPointsMinimum(int pointsMin) {
        if (pointsMin < 0) {
            throw new IllegalArgumentException("Points minimum ne peut pas être négatif.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE points_accumules >= ? ORDER BY points_accumules DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pointsMin);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByPointsMinimum: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findTopUtilisateursParPoints(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("Limite doit être positive.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements ORDER BY points_accumules DESC LIMIT ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findTopUtilisateursParPoints: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findAbonnementsRisqueChurn(double seuil) {
        if (seuil < 0 || seuil > 1) {
            throw new IllegalArgumentException("Seuil doit être entre 0 et 1.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE churn_score >= ? ORDER BY churn_score DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, seuil);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findAbonnementsRisqueChurn: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findAbonnementsAvecParticipationsActives() {
        List<Abonnement> list = new ArrayList<>();
        String sql = """
                SELECT DISTINCT a.* FROM abonnements a
                INNER JOIN participations p ON a.user_id = p.user_id
                WHERE p.statut IN ('CONFIRME', 'EN_ATTENTE')
                """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAbonnement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findAbonnementsAvecParticipationsActives: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> findAbonnementsSansParticipation(int derniersMois) {
        if (derniersMois <= 0) {
            throw new IllegalArgumentException("Mois doivent être positifs.");
        }
        List<Abonnement> list = new ArrayList<>();
        String sql = """
                SELECT a.* FROM abonnements a
                WHERE NOT EXISTS (
                    SELECT 1 FROM participations p
                    WHERE p.user_id = a.user_id
                    AND p.date_inscription >= DATE_SUB(CURRENT_DATE, INTERVAL ? MONTH)
                )
                """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, derniersMois);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findAbonnementsSansParticipation: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Abonnement> search(AbonnementCriteria criteria) {
        // Implémentation basique, peut être étendue avec criteria
        if (criteria == null) {
            return findAll();
        }
        // Exemple : filtrer par type si présent
        if (criteria.getType() != null) {
            return findByType(criteria.getType());
        }
        return findAll();
    }

    // ────────────────────────────────────────────────
    // UPDATE
    // ────────────────────────────────────────────────

    @Override
    public Abonnement update(Abonnement abonnement) {
        AbonnementValidationResult validation = validateForUpdate(abonnement);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getFormattedMessage());
        }
        String sql = """
                UPDATE abonnements SET type=?, date_debut=?, date_fin=?, prix=?, statut=?, avantages=?, auto_renew=?, points_accumules=?, churn_score=?
                WHERE id = ?
                """;
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
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Abonnement non trouvé avec l'ID " + abonnement.getId());
            }
            logger.info("Abonnement mis à jour → ID: {}", abonnement.getId());
            return abonnement;
        } catch (SQLException e) {
            logger.error("Erreur mise à jour abonnement {}", abonnement.getId(), e);
            throw new RuntimeException("Échec mise à jour abonnement", e);
        }
    }

    @Override
    public boolean toggleAutoRenew(Long id, boolean autoRenew) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide.");
        }
        String sql = "UPDATE abonnements SET auto_renew = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, autoRenew);
            pstmt.setLong(2, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logger.info("Auto-renew mis à jour pour abonnement {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur toggleAutoRenew: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void ajouterPoints(Long id, int points) {
        if (id == null || id <= 0 || points < 0) {
            throw new IllegalArgumentException("ID ou points invalides.");
        }
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Abonnement introuvable.");
        }
        Abonnement a = opt.get();
        a.setPointsAccumules(a.getPointsAccumules() + points);
        update(a);
    }

    @Override
    public boolean utiliserPoints(Long id, int points) {
        if (id == null || id <= 0 || points < 0) {
            throw new IllegalArgumentException("ID ou points invalides.");
        }
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Abonnement a = opt.get();
        if (a.getPointsAccumules() < points) {
            return false;
        }
        a.setPointsAccumules(a.getPointsAccumules() - points);
        update(a);
        return true;
    }

    @Override
    public Abonnement upgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Abonnement introuvable.");
        }
        Abonnement a = opt.get();
        if (verifierDisponiteUpgrade(id, nouveauType)) {
            a.setType(nouveauType);
            // Ajuster prix et dates si nécessaire
            update(a);
            return a;
        }
        throw new IllegalArgumentException("Upgrade non disponible.");
    }

    @Override
    public Abonnement downgradeAbonnement(Long id, Abonnement.TypeAbonnement nouveauType) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Abonnement introuvable.");
        }
        Abonnement a = opt.get();
        a.setType(nouveauType);
        // Ajuster prix et dates si nécessaire
        update(a);
        return a;
    }

    @Override
    public Abonnement renouvelerAbonnement(Long id) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Abonnement introuvable.");
        }
        Abonnement a = opt.get();
        if (a.isAutoRenew()) {
            LocalDate newFin = a.getDateFin().plusMonths(1); // Exemple pour mensuel
            a.setDateFin(newFin);
            update(a);
            envoyerConfirmationRenouvellement(id);
            return a;
        }
        throw new IllegalArgumentException("Auto-renew non activé.");
    }

    @Override
    public boolean suspendreAbonnement(Long id, String raison) {
        if (raison == null || raison.isBlank()) {
            throw new IllegalArgumentException("Raison obligatoire.");
        }
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Abonnement a = opt.get();
        Abonnement.StatutAbonnement ancien = a.getStatut();
        a.setStatut(Abonnement.StatutAbonnement.SUSPENDU);
        update(a);
        envoyerNotificationChangementStatut(id, ancien);
        return true;
    }

    @Override
    public boolean reactiverAbonnement(Long id) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Abonnement a = opt.get();
        if (a.getStatut() != Abonnement.StatutAbonnement.SUSPENDU) {
            throw new IllegalArgumentException("Abonnement non suspendu.");
        }
        Abonnement.StatutAbonnement ancien = a.getStatut();
        a.setStatut(Abonnement.StatutAbonnement.ACTIF);
        update(a);
        envoyerNotificationChangementStatut(id, ancien);
        return true;
    }

    @Override
    public boolean synchroniserAvecPaiement(Long id) {
        // Simulation
        return true;
    }

    // ────────────────────────────────────────────────
    // DELETE
    // ────────────────────────────────────────────────

    @Override
    public boolean delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide.");
        }
        if (!peutEtreSupprime(id)) {
            return false;
        }
        String sql = "DELETE FROM abonnements WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logger.info("Abonnement supprimé: ID {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur delete: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        Optional<Abonnement> opt = findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Abonnement a = opt.get();
        if (a.getStatut() != Abonnement.StatutAbonnement.EXPIRE) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM participations WHERE user_id = ? AND statut IN ('CONFIRME','EN_ATTENTE')";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, a.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur peutEtreSupprime: {}", e.getMessage());
        }
        return false;
    }

    // ────────────────────────────────────────────────
    // STATS & CALCULS
    // ────────────────────────────────────────────────

    @Override
    public BigDecimal getTotalPointsAccumules() {
        String sql = "SELECT SUM(points_accumules) FROM abonnements";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            logger.error("Erreur getTotalPointsAccumules: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalPointsAccumulesByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID invalide.");
        }
        String sql = "SELECT SUM(points_accumules) FROM abonnements WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur getTotalPointsAccumulesByUserId: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public long countByStatut(Abonnement.StatutAbonnement statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Statut obligatoire.");
        }
        String sql = "SELECT COUNT(*) FROM abonnements WHERE statut = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur countByStatut: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public long countByType(Abonnement.TypeAbonnement type) {
        if (type == null) {
            throw new IllegalArgumentException("Type obligatoire.");
        }
        String sql = "SELECT COUNT(*) FROM abonnements WHERE type = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur countByType: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public BigDecimal calculerRevenuTotal() {
        String sql = "SELECT SUM(prix) FROM abonnements";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            logger.error("Erreur calculerRevenuTotal: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculerRevenuParMois(int mois, int annee) {
        if (mois < 1 || mois > 12 || annee < 1900) {
            throw new IllegalArgumentException("Mois ou année invalide.");
        }
        String sql = "SELECT SUM(prix) FROM abonnements WHERE MONTH(date_debut) = ? AND YEAR(date_debut) = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mois);
            pstmt.setInt(2, annee);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur calculerRevenuParMois: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public double calculerTauxRetention(int mois) {
        // Simulation simple
        return 0.85; // À implémenter avec logique réelle
    }

    // ────────────────────────────────────────────────
    // NOTIFICATIONS (Simulation console)
    // ────────────────────────────────────────────────

    @Override
    public void envoyerRappelExpiration(Long id) {
        System.out.println("📧 Envoi rappel expiration pour abonnement " + id);
    }

    @Override
    public void envoyerConfirmationRenouvellement(Long id) {
        System.out.println("📧 Envoi confirmation renouvellement pour abonnement " + id);
    }

    @Override
    public void envoyerNotificationChangementStatut(Long id, Abonnement.StatutAbonnement ancienStatut) {
        System.out.println("📧 Envoi notification changement statut pour abonnement " + id + " (ancien: " + ancienStatut + ")");
    }

    // ────────────────────────────────────────────────
    // UTILITAIRES
    // ────────────────────────────────────────────────

    private String convertAvantagesToString(Map<String, Object> avantages) {
        if (avantages == null || avantages.isEmpty()) return "{}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(avantages);
        } catch (Exception e) {
            logger.warn("Échec sérialisation avantages", e);
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAvantagesFromString(String str) {
        if (str == null || str.isBlank() || "{}".equals(str.trim())) return new HashMap<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(str, Map.class);
        } catch (Exception e) {
            logger.warn("Échec désérialisation avantages: {}", str, e);
            return new HashMap<>();
        }
    }

    private Abonnement mapResultSetToAbonnement(ResultSet rs) throws SQLException {
        Abonnement a = new Abonnement();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setType(Abonnement.TypeAbonnement.valueOf(rs.getString("type")));
        a.setDateDebut(rs.getDate("date_debut").toLocalDate());
        a.setDateFin(rs.getDate("date_fin").toLocalDate());
        a.setPrix(rs.getBigDecimal("prix"));
        a.setStatut(Abonnement.StatutAbonnement.valueOf(rs.getString("statut")));
        a.setAvantages(parseAvantagesFromString(rs.getString("avantages")));
        a.setAutoRenew(rs.getBoolean("auto_renew"));
        a.setPointsAccumules(rs.getInt("points_accumules"));
        a.setChurnScore(rs.getDouble("churn_score"));
        return a;
    }

    @Override
    public boolean validerAbonnement(Abonnement abonnement) {
        return validateForCreation(abonnement).isValid();
    }

    @Override
    public boolean verifierDisponiteUpgrade(Long id, Abonnement.TypeAbonnement nouveauType) {
        // Logique simple : toujours true pour exemple
        return true;
    }
}
