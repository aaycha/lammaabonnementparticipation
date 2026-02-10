
/*package com.gestion.services;

import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.Participation;
import com.gestion.interfaces.ParticipationService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParticipationServiceImpl implements ParticipationService {

    private static final Logger logger = LoggerFactory.getLogger(ParticipationServiceImpl.class);
    private final MyConnection dbConnection;

    public ParticipationServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    // ================= CREATE =================
    @Override
    public Participation create(Participation participation) {
        if (!validerParticipation(participation))
            throw new IllegalArgumentException("Participation invalide");

        if (isAlreadyParticipating(participation.getUserId(), participation.getEvenementId()))
            throw new RuntimeException("Vous êtes déjà inscrit à cet événement !");

        String sql = "INSERT INTO participations (user_id, evenement_id, date_inscription, type, statut, hebergement_nuits, contexte_social, badge_associe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, participation.getUserId());
            pstmt.setLong(2, participation.getEvenementId());
            pstmt.setTimestamp(3, Timestamp.valueOf(participation.getDateInscription()));
            pstmt.setString(4, participation.getType().name());
            pstmt.setString(5, participation.getStatut().name());
            pstmt.setInt(6, participation.getHebergementNuits());
            pstmt.setString(7, participation.getContexteSocial().name());
            pstmt.setString(8, participation.getBadgeAssocie());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Échec création participation");

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    participation.setId(keys.getLong(1));
                    logger.info("Participation créée : ID {}", participation.getId());
                    return participation;
                } else throw new SQLException("Échec création participation, pas d'ID généré");
            }

        } catch (SQLException e) {
            logger.error("Erreur création participation: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ================= READ =================
    @Override
    public Optional<Participation> findById(Long id) {
        String sql = "SELECT * FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToParticipation(rs));
            }

        } catch (SQLException e) {
            logger.error("Erreur findById: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Participation> findAll() {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT * FROM participations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToParticipation(rs));
        } catch (SQLException e) {
            logger.error("Erreur findAll: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Participation> findAll(String sortBy, String sortOrder) {
        List<Participation> list = findAll();
        return sortParticipations(list, sortBy, sortOrder);
    }

    @Override
    public List<Participation> findByUserId(Long userId) {
        return findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .sorted(Comparator.comparing(Participation::getDateInscription).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Participation> findByEvenementId(Long evenementId) {
        return findAll().stream()
                .filter(p -> p.getEvenementId().equals(evenementId))
                .sorted(Comparator.comparing(Participation::getDateInscription))
                .collect(Collectors.toList());
    }

    @Override
    public List<Participation> search(ParticipationCriteria criteria) {
        Stream<Participation> stream = findAll().stream();

        if (criteria.getUserId() != null)
            stream = stream.filter(p -> p.getUserId().equals(criteria.getUserId()));

        if (criteria.getEvenementId() != null)
            stream = stream.filter(p -> p.getEvenementId().equals(criteria.getEvenementId()));

        if (criteria.getType() != null)
            stream = stream.filter(p -> p.getType() == criteria.getType());

        if (criteria.getContexteSocial() != null)
            stream = stream.filter(p -> p.getContexteSocial() == criteria.getContexteSocial());

        if (criteria.getDateFrom() != null)
            stream = stream.filter(p -> !p.getDateInscription().isBefore(criteria.getDateFrom()));

        if (criteria.getDateTo() != null)
            stream = stream.filter(p -> !p.getDateInscription().isAfter(criteria.getDateTo()));

        return stream.sorted(Comparator.comparing(Participation::getDateInscription))
                .collect(Collectors.toList());
    }

    // ================= UPDATE =================
    @Override
    public Participation update(Participation participation) {
        if (participation.getId() == null) throw new IllegalArgumentException("ID manquant pour update");

        String sql = "UPDATE participations SET user_id=?, evenement_id=?, date_inscription=?, type=?, statut=?, hebergement_nuits=?, contexte_social=?, badge_associe=? WHERE id=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, participation.getUserId());
            ps.setLong(2, participation.getEvenementId());
            ps.setTimestamp(3, Timestamp.valueOf(participation.getDateInscription()));
            ps.setString(4, participation.getType().name());
            ps.setString(5, participation.getStatut().name());
            ps.setInt(6, participation.getHebergementNuits());
            ps.setString(7, participation.getContexteSocial().name());
            ps.setString(8, participation.getBadgeAssocie());
            ps.setLong(9, participation.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Update échoué");

            logger.info("Participation {} mise à jour", participation.getId());
            return participation;
        } catch (SQLException e) {
            logger.error("Erreur update: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ================= DELETE =================
    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM participations WHERE id=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<Participation> findByStatut(Participation.StatutParticipation statut) {
        return List.of();
    }

    @Override
    public List<Participation> findByType(Participation.TypeParticipation type) {
        return List.of();
    }

    @Override
    public List<Participation> findByContexteSocial(Participation.ContexteSocial contexte) {
        return List.of();
    }

    @Override
    public List<Participation> findByDateInscriptionBetween(LocalDateTime debut, LocalDateTime fin) {
        return List.of();
    }

    @Override
    public List<Participation> findByHebergementNuitsMinimum(int nuitsMin) {
        return List.of();
    }

    @Override
    public List<Participation> findParticipationsConfirmees() {
        return List.of();
    }

    @Override
    public List<Participation> findParticipationsEnAttente() {
        return List.of();
    }

    @Override
    public List<Participation> findListeAttente(Long evenementId) {
        return List.of();
    }

    @Override
    public Participation confirmerParticipation(Long id) {
        return null;
    }

    @Override
    public Participation annulerParticipation(Long id, String raison) {
        return null;
    }

    @Override
    public Participation ajouterListeAttente(Long id) {
        return null;
    }

    @Override
    public Participation promouvoirListeAttente(Long id) {
        return null;
    }

    @Override
    public boolean verifierDisponibiliteEvenement(Long evenementId) {
        return false;
    }

    @Override
    public int getPlacesDisponibles(Long evenementId) {
        return 0;
    }

    @Override
    public int getNombreParticipantsConfirmes(Long evenementId) {
        return 0;
    }

    @Override
    public List<Participation> findAvecHebergement() {
        return List.of();
    }

    @Override
    public Participation modifierHebergement(Long id, int nouvellesNuits) {
        return null;
    }

    @Override
    public boolean validerHebergement(Participation participation) {
        return false;
    }

    @Override
    public void attribuerBadge(Long id) {

    }

    @Override
    public List<Participation> findByBadge(String badge) {
        return List.of();
    }

    @Override
    public List<Participation> findParticipationsAvecBadge() {
        return List.of();
    }

    @Override
    public int calculerPointsParticipation(Long userId) {
        return 0;
    }

    @Override
    public List<Participation> suggestionsMatchingGroupe(Long participationId) {
        return List.of();
    }

    @Override
    public List<Participation> findParticipationsSimilaires(Long userId, Participation.ContexteSocial contexte) {
        return List.of();
    }

    @Override
    public boolean creerMatchingGroupe(List<Long> participationIds) {
        return false;
    }

    @Override
    public long countByStatut(Participation.StatutParticipation statut) {
        return 0;
    }

    @Override
    public long countByType(Participation.TypeParticipation type) {
        return 0;
    }

    @Override
    public long countByContexteSocial(Participation.ContexteSocial contexte) {
        return 0;
    }

    @Override
    public List<Participation> findParticipationsPeriod(LocalDateTime debut, LocalDateTime fin) {
        return List.of();
    }

    @Override
    public double calculerTauxConfirmation(Long evenementId) {
        return 0;
    }

    @Override
    public double calculerTauxAnnulation(Long evenementId) {
        return 0;
    }

    // ================= UTILS =================
    private Participation mapResultSetToParticipation(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setEvenementId(rs.getLong("evenement_id"));
        p.setDateInscription(rs.getTimestamp("date_inscription").toLocalDateTime());
        p.setType(Participation.TypeParticipation.valueOf(rs.getString("type")));
        p.setStatut(Participation.StatutParticipation.valueOf(rs.getString("statut")));
        p.setHebergementNuits(rs.getInt("hebergement_nuits"));
        p.setContexteSocial(Participation.ContexteSocial.valueOf(rs.getString("contexte_social")));
        p.setBadgeAssocie(rs.getString("badge_associe"));
        return p;
    }

    @Override
    public boolean validerParticipation(Participation participation) {
        return participation != null &&
                participation.getUserId() != null && participation.getUserId() > 0 &&
                participation.getEvenementId() != null && participation.getEvenementId() > 0 &&
                participation.getType() != null &&
                participation.getContexteSocial() != null &&
                participation.getHebergementNuits() >= 0;
    }

    @Override
    public boolean verifierConflitDates(Long userId, Long evenementId) {
        return false;
    }

    @Override
    public boolean peutEtreSupprimee(Long id) {
        return false;
    }

    @Override
    public void envoyerConfirmationInscription(Long id) {

    }

    @Override
    public void envoyerNotificationAnnulation(Long id) {

    }

    @Override
    public void envoyerNotificationConfirmation(Long id) {

    }

    @Override
    public void notifierListeAttente(Long evenementId) {

    }

    @Override
    public void envoyerRappelEvenement(Long id) {

    }

    @Override
    public List<Participation> findParticipationsAvecRecommandations() {
        return List.of();
    }

    @Override
    public boolean synchroniserAvecTransport(Long id) {
        return false;
    }

    @Override
    public boolean synchroniserAvecPaiement(Long id) {
        return false;
    }

    @Override
    public List<Participation> findParticipationsAbonnementPremium() {
        return List.of();
    }

    @Override
    public String exporterCalendrier(Long userId) {
        return "";
    }

    @Override
    public List<Participation> importerDonneesExterne(String source) {
        return List.of();
    }

    @Override
    public boolean integrerCalendrierExterne(Long userId, String icalData) {
        return false;
    }

    @Override
    public boolean isAlreadyParticipating(Long userId, Long evenementId) {
        return findByUserId(userId).stream().anyMatch(p -> p.getEvenementId().equals(evenementId));
    }

    private List<Participation> sortParticipations(List<Participation> list, String sortBy, String sortOrder) {
        Comparator<Participation> cmp = Comparator.comparing(Participation::getDateInscription);
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).collect(Collectors.toList());
    }

    // ================= MÉTHODES DE SUPPORT =================
    public Long getUserIdByNom(String nomUser) {
        String sql = "SELECT id FROM users WHERE nom=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            logger.error("getUserIdByNom error: {}", e.getMessage());
        }
        return null;
    }

    public Long getEvenementIdByNom(String titre) {
        String sql = "SELECT id_event FROM evenement WHERE titre=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id_event");
            }
        } catch (SQLException e) {
            logger.error("getEvenementIdByNom error: {}", e.getMessage());
        }
        return null;
    }
}*/
package com.gestion.services;

import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.Participation;
import com.gestion.interfaces.ParticipationService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ParticipationServiceImpl implements ParticipationService {

    private static final Logger logger = LoggerFactory.getLogger(ParticipationServiceImpl.class);
    private final MyConnection dbConnection;

    public ParticipationServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    // ────────────────────────────────────────────────
    // VALIDATION
    // ────────────────────────────────────────────────
    private static class ValidationResult {
        final boolean valid;
        final List<String> errors = new ArrayList<>();

        ValidationResult(boolean valid) { this.valid = valid; }

        String getMessage() {
            return valid ? "Validation réussie" : "Erreurs :\n• " + String.join("\n• ", errors);
        }

        static ValidationResult valid() { return new ValidationResult(true); }
        static ValidationResult invalid(String... msgs) {
            ValidationResult vr = new ValidationResult(false);
            vr.errors.addAll(Arrays.asList(msgs));
            return vr;
        }
    }

    private ValidationResult validate(Participation p, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        if (p == null) errors.add("Participation ne peut pas être null");
        if (p.getUserId() == null || p.getUserId() <= 0) errors.add("User ID obligatoire et > 0");
        if (p.getEvenementId() == null || p.getEvenementId() <= 0) errors.add("Événement ID obligatoire et > 0");
        if (p.getType() == null) errors.add("Type obligatoire (SIMPLE, HEBERGEMENT, GROUPE)");
        if (p.getContexteSocial() == null) errors.add("Contexte social obligatoire");
        if (p.getHebergementNuits() < 0) errors.add("Nuits ne peut être négatif");
        if (p.getDateInscription() == null) errors.add("Date inscription obligatoire");
        if (p.getDateInscription().isAfter(LocalDateTime.now().plusDays(1))) {
            errors.add("Date inscription ne peut être dans le futur");
        }

        if (p.getType() == Participation.TypeParticipation.HEBERGEMENT && p.getHebergementNuits() < 1) {
            errors.add("HEBERGEMENT nécessite au moins 1 nuit");
        }

        if (!isUpdate && isAlreadyParticipating(p.getUserId(), p.getEvenementId())) {
            errors.add("Utilisateur déjà inscrit à cet événement");
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors.toArray(new String[0]));
    }

    // ────────────────────────────────────────────────
    // CREATE
    // ────────────────────────────────────────────────
    @Override
    public Participation create(Participation p) {
        ValidationResult vr = validate(p, false);
        if (!vr.valid) throw new IllegalArgumentException(vr.getMessage());

        String sql = """
            INSERT INTO participations 
            (user_id, evenement_id, date_inscription, type, statut, hebergement_nuits, contexte_social, badge_associe)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, p.getUserId());
            ps.setLong(2, p.getEvenementId());
            ps.setTimestamp(3, Timestamp.valueOf(p.getDateInscription()));
            ps.setString(4, p.getType().name());
            ps.setString(5, p.getStatut().name());
            ps.setInt(6, p.getHebergementNuits());
            ps.setString(7, p.getContexteSocial().name());
            ps.setString(8, p.getBadgeAssocie());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne insérée");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getLong(1));
                    logger.info("Participation créée → ID: {}", p.getId());
                    return p;
                }
            }
            throw new SQLException("Aucun ID généré");
        } catch (SQLException e) {
            logger.error("Échec création participation", e);
            throw new RuntimeException("Échec création participation", e);
        }
    }

    // ────────────────────────────────────────────────
    // READ - MÉTHODES PRINCIPALES
    // ────────────────────────────────────────────────
    private Participation map(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setEvenementId(rs.getLong("evenement_id"));
        p.setDateInscription(rs.getTimestamp("date_inscription").toLocalDateTime());
        p.setType(Participation.TypeParticipation.valueOf(rs.getString("type")));
        p.setStatut(Participation.StatutParticipation.valueOf(rs.getString("statut")));
        p.setHebergementNuits(rs.getInt("hebergement_nuits"));
        p.setContexteSocial(Participation.ContexteSocial.valueOf(rs.getString("contexte_social")));
        p.setBadgeAssocie(rs.getString("badge_associe"));
        return p;
    }

    @Override
    public Optional<Participation> findById(Long id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT * FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de findById({}) : {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Participation> findAll() {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT * FROM participations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            logger.error("Erreur findAll", e);
        }
        return list;
    }

    @Override
    public List<Participation> findAll(String sortBy, String sortOrder) {
        String field = switch ((sortBy != null ? sortBy.trim().toLowerCase() : "")) {
            case "type" -> "type";
            case "statut" -> "statut";
            case "hebergement_nuits", "nuits" -> "hebergement_nuits";
            default -> "date_inscription";
        };

        String dir = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        String sql = "SELECT * FROM participations ORDER BY " + field + " " + dir;

        List<Participation> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            logger.error("Erreur findAll trié", e);
        }
        return list;
    }

    @Override
    public Optional<Participation> findOneById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Participation> search(ParticipationCriteria crit) {
        if (crit == null) throw new IllegalArgumentException("Critères de recherche obligatoires");

        StringBuilder sql = new StringBuilder("SELECT * FROM participations WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (crit.getUserId() != null) {
            sql.append(" AND user_id = ?");
            params.add(crit.getUserId());
        }
        if (crit.getEvenementId() != null) {
            sql.append(" AND evenement_id = ?");
            params.add(crit.getEvenementId());
        }
        if (crit.getStatut() != null) {
            sql.append(" AND statut = ?");
            params.add(crit.getStatut().name());
        }
        if (crit.getType() != null) {
            sql.append(" AND type = ?");
            params.add(crit.getType().name());
        }
        if (crit.getContexteSocial() != null) {
            sql.append(" AND contexte_social = ?");
            params.add(crit.getContexteSocial().name());
        }
        if (crit.getDateInscriptionFrom() != null) {
            sql.append(" AND date_inscription >= ?");
            params.add(Timestamp.valueOf(crit.getDateInscriptionFrom()));
        }
        if (crit.getDateInscriptionTo() != null) {
            sql.append(" AND date_inscription <= ?");
            params.add(Timestamp.valueOf(crit.getDateInscriptionTo()));
        }

        // Tri
        String sortField = switch ((crit.getSortBy() != null ? crit.getSortBy().trim().toLowerCase() : "")) {
            case "statut" -> "statut";
            case "type" -> "type";
            case "hebergementnuits", "nuits" -> "hebergement_nuits";
            default -> "date_inscription";
        };
        String sortDir = "ASC".equalsIgnoreCase(crit.getSortOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(sortField).append(" ").append(sortDir);

        List<Participation> result = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de search", e);
        }
        return result;
    }

    // ────────────────────────────────────────────────
    // FILTRES SIMPLES (implémentés avec SQL quand possible)
    // ────────────────────────────────────────────────

    @Override
    public List<Participation> findByUserId(Long userId) {
        if (userId == null || userId <= 0) return List.of();
        String sql = "SELECT * FROM participations WHERE user_id = ? ORDER BY date_inscription DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByUserId failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findByEvenementId(Long evenementId) {
        if (evenementId == null || evenementId <= 0) return List.of();
        String sql = "SELECT * FROM participations WHERE evenement_id = ? ORDER BY date_inscription ASC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByEvenementId failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findByStatut(Participation.StatutParticipation statut) {
        if (statut == null) return List.of();
        String sql = "SELECT * FROM participations WHERE statut = ? ORDER BY date_inscription DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByStatut failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findByType(Participation.TypeParticipation type) {
        if (type == null) return List.of();
        String sql = "SELECT * FROM participations WHERE type = ? ORDER BY date_inscription DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByType failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findByContexteSocial(Participation.ContexteSocial contexte) {
        if (contexte == null) return List.of();
        String sql = "SELECT * FROM participations WHERE contexte_social = ? ORDER BY date_inscription DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contexte.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByContexteSocial failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findByDateInscriptionBetween(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null || fin.isBefore(debut)) return List.of();
        String sql = "SELECT * FROM participations WHERE date_inscription BETWEEN ? AND ? ORDER BY date_inscription ASC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(debut));
            ps.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByDateInscriptionBetween failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findByHebergementNuitsMinimum(int nuitsMin) {
        if (nuitsMin < 0) return List.of();
        String sql = "SELECT * FROM participations WHERE hebergement_nuits >= ? ORDER BY hebergement_nuits DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nuitsMin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByHebergementNuitsMinimum failed", e);
        }
        return list;
    }

    // ────────────────────────────────────────────────
    // MÉTHODES DÉRIVÉES (simples alias ou filtres)
    // ────────────────────────────────────────────────
    @Override
    public List<Participation> findParticipationsConfirmees() {
        return findByStatut(Participation.StatutParticipation.CONFIRME);
    }

    @Override
    public List<Participation> findParticipationsEnAttente() {
        return findByStatut(Participation.StatutParticipation.EN_ATTENTE);
    }

    @Override
    public List<Participation> findListeAttente(Long evenementId) {
        return findByEvenementId(evenementId).stream()
                .filter(p -> p.getStatut() == Participation.StatutParticipation.EN_ATTENTE)
                .collect(Collectors.toList());
    }

    @Override
    public List<Participation> findAvecHebergement() {
        return findByType(Participation.TypeParticipation.HEBERGEMENT);
    }

    // ────────────────────────────────────────────────
    // UPDATE
    // ────────────────────────────────────────────────
    @Override
    public Participation update(Participation p) {
        ValidationResult vr = validate(p, true);
        if (!vr.valid) throw new IllegalArgumentException(vr.getMessage());

        String sql = """
            UPDATE participations 
            SET user_id = ?, evenement_id = ?, date_inscription = ?, type = ?, statut = ?, 
                hebergement_nuits = ?, contexte_social = ?, badge_associe = ?
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, p.getUserId());
            ps.setLong(2, p.getEvenementId());
            ps.setTimestamp(3, Timestamp.valueOf(p.getDateInscription()));
            ps.setString(4, p.getType().name());
            ps.setString(5, p.getStatut().name());
            ps.setInt(6, p.getHebergementNuits());
            ps.setString(7, p.getContexteSocial().name());
            ps.setString(8, p.getBadgeAssocie());
            ps.setLong(9, p.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Aucune participation trouvée avec ID " + p.getId());
            }
            logger.info("Participation mise à jour : {}", p.getId());
            return p;
        } catch (SQLException e) {
            logger.error("Échec mise à jour participation", e);
            throw new RuntimeException("Échec mise à jour participation", e);
        }
    }

    // ────────────────────────────────────────────────
    // ACTIONS SPÉCIALES
    // ────────────────────────────────────────────────
    @Override
    public Participation confirmerParticipation(Long id) {
        return findById(id)
                .map(p -> {
                    p.setStatut(Participation.StatutParticipation.CONFIRME);
                    return update(p);
                })
                .orElseThrow(() -> new IllegalArgumentException("Participation " + id + " introuvable"));
    }

    @Override
    public Participation annulerParticipation(Long id, String raison) {
        if (raison == null || raison.trim().isEmpty()) {
            throw new IllegalArgumentException("Raison d'annulation obligatoire");
        }
        return findById(id)
                .map(p -> {
                    p.setStatut(Participation.StatutParticipation.ANNULE);
                    return update(p);
                })
                .orElseThrow(() -> new IllegalArgumentException("Participation " + id + " introuvable"));
    }

    @Override
    public Participation ajouterListeAttente(Long id) {
        return findById(id)
                .map(p -> {
                    p.setStatut(Participation.StatutParticipation.EN_ATTENTE);
                    return update(p);
                })
                .orElseThrow(() -> new IllegalArgumentException("Participation " + id + " introuvable"));
    }

    @Override
    public Participation promouvoirListeAttente(Long id) {
        return findById(id)
                .filter(p -> p.getStatut() == Participation.StatutParticipation.EN_ATTENTE)
                .map(p -> {
                    p.setStatut(Participation.StatutParticipation.CONFIRME);
                    return update(p);
                })
                .orElseThrow(() -> new IllegalArgumentException("Participation " + id + " introuvable ou non en attente"));
    }

    @Override
    public Participation modifierHebergement(Long id, int nouvellesNuits) {
        if (nouvellesNuits < 0) throw new IllegalArgumentException("Nuits ne peuvent être négatives");
        return findById(id)
                .filter(p -> p.getType() == Participation.TypeParticipation.HEBERGEMENT)
                .map(p -> {
                    p.setHebergementNuits(nouvellesNuits);
                    return update(p);
                })
                .orElseThrow(() -> new IllegalArgumentException("Participation " + id + " introuvable ou non de type HEBERGEMENT"));
    }

    // ────────────────────────────────────────────────
    // DELETE
    // ────────────────────────────────────────────────
    @Override
    public boolean delete(Long id) {
        if (id == null || id <= 0) return false;
        if (!peutEtreSupprimee(id)) return false;

        String sql = "DELETE FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Échec suppression ID " + id, e);
            return false;
        }
    }

    @Override
    public boolean peutEtreSupprimee(Long id) {
        return findById(id)
                .map(p -> p.getStatut() != Participation.StatutParticipation.CONFIRME &&
                        p.getStatut() != Participation.StatutParticipation.EN_ATTENTE)
                .orElse(false);
    }

    // ────────────────────────────────────────────────
    // STATS & COUNTS
    // ────────────────────────────────────────────────
    @Override
    public long countByStatut(Participation.StatutParticipation statut) {
        if (statut == null) return 0;
        String sql = "SELECT COUNT(*) FROM participations WHERE statut = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("countByStatut failed", e);
        }
        return 0;
    }

    @Override
    public long countByType(Participation.TypeParticipation type) {
        if (type == null) return 0;
        String sql = "SELECT COUNT(*) FROM participations WHERE type = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("countByType failed", e);
        }
        return 0;
    }

    @Override
    public long countByContexteSocial(Participation.ContexteSocial contexte) {
        if (contexte == null) return 0;
        String sql = "SELECT COUNT(*) FROM participations WHERE contexte_social = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contexte.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("countByContexteSocial failed", e);
        }
        return 0;
    }

    @Override
    public List<Participation> findParticipationsPeriod(LocalDateTime debut, LocalDateTime fin) {
        return List.of();
    }

    @Override
    public double calculerTauxConfirmation(Long evenementId) {
        if (evenementId == null) return 0.0;
        List<Participation> parts = findByEvenementId(evenementId);
        if (parts.isEmpty()) return 0.0;
        long confirms = parts.stream().filter(p -> p.getStatut() == Participation.StatutParticipation.CONFIRME).count();
        return (double) confirms / parts.size() * 100;
    }

    @Override
    public double calculerTauxAnnulation(Long evenementId) {
        if (evenementId == null) return 0.0;
        List<Participation> parts = findByEvenementId(evenementId);
        if (parts.isEmpty()) return 0.0;
        long annuls = parts.stream().filter(p -> p.getStatut() == Participation.StatutParticipation.ANNULE).count();
        return (double) annuls / parts.size() * 100;
    }

    @Override
    public int calculerPointsParticipation(Long userId) {
        if (userId == null) return 0;
        return (int) findByUserId(userId).stream()
                .filter(p -> p.getStatut() == Participation.StatutParticipation.CONFIRME)
                .count() * 10;
    }

    @Override
    public int getNombreParticipantsConfirmes(Long evenementId) {
        if (evenementId == null) return 0;
        return (int) findByEvenementId(evenementId).stream()
                .filter(p -> p.getStatut() == Participation.StatutParticipation.CONFIRME)
                .count();
    }

    @Override
    public int getPlacesDisponibles(Long evenementId) {
        // Valeur fixe à remplacer par une vraie capacité si table evenement existe
        int capacite = 100;
        return capacite - getNombreParticipantsConfirmes(evenementId);
    }

    @Override
    public boolean verifierDisponibiliteEvenement(Long evenementId) {
        return getPlacesDisponibles(evenementId) > 0;
    }

    @Override
    public boolean verifierConflitDates(Long userId, Long evenementId) {
        return isAlreadyParticipating(userId, evenementId);
    }

    @Override
    public boolean validerHebergement(Participation p) {
        return p != null &&
                p.getType() == Participation.TypeParticipation.HEBERGEMENT &&
                p.getHebergementNuits() >= 1;
    }

    @Override
    public void attribuerBadge(Long id) {
        findById(id).ifPresent(p -> {
            p.setBadgeAssocie("BADGE_" + id + "_" + System.currentTimeMillis());
            update(p);
        });
    }

    @Override
    public List<Participation> findByBadge(String badge) {
        if (badge == null || badge.isBlank()) return List.of();
        String sql = "SELECT * FROM participations WHERE badge_associe = ? ORDER BY date_inscription DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, badge);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("findByBadge failed", e);
        }
        return list;
    }

    @Override
    public List<Participation> findParticipationsAvecBadge() {
        String sql = "SELECT * FROM participations WHERE badge_associe IS NOT NULL ORDER BY date_inscription DESC";
        List<Participation> list = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            logger.error("findParticipationsAvecBadge failed", e);
        }
        return list;
    }

    // ────────────────────────────────────────────────
    // MÉTHODES SIMULÉES OU À COMPLÉTER
    // ────────────────────────────────────────────────
    @Override
    public boolean creerMatchingGroupe(List<Long> participationIds) {
        if (participationIds == null || participationIds.isEmpty()) return false;
        for (Long id : participationIds) {
            findById(id).ifPresent(p -> {
                p.setType(Participation.TypeParticipation.GROUPE);
                update(p);
            });
        }
        return true;
    }

    @Override
    public String exporterCalendrier(Long userId) {
        if (userId == null) return "";
        return findByUserId(userId).stream()
                .map(p -> "Participation à l'événement " + p.getEvenementId() + " le " + p.getDateInscription())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public List<Participation> importerDonneesExterne(String source) {
        // À implémenter selon le format réel
        return List.of();
    }

    @Override
    public boolean integrerCalendrierExterne(Long userId, String icalData) {
        // À implémenter
        return true;
    }

    @Override
    public boolean synchroniserAvecTransport(Long id) { return true; }
    @Override
    public boolean synchroniserAvecPaiement(Long id) { return true; }

    @Override
    public List<Participation> findParticipationsAbonnementPremium() {
        // À implémenter avec jointure si table abonnements existe
        return List.of();
    }

    @Override
    public List<Participation> findParticipationsAvecRecommandations() {
        return findByContexteSocial(Participation.ContexteSocial.AMIS);
    }

    @Override
    public List<Participation> suggestionsMatchingGroupe(Long participationId) {
        return findById(participationId)
                .map(p -> findParticipationsSimilaires(p.getUserId(), p.getContexteSocial()))
                .orElse(List.of());
    }

    @Override
    public List<Participation> findParticipationsSimilaires(Long userId, Participation.ContexteSocial contexte) {
        if (userId == null || contexte == null) return List.of();
        return findAll().stream()
                .filter(p -> !Objects.equals(p.getUserId(), userId))
                .filter(p -> p.getContexteSocial() == contexte)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerParticipation(Participation p) {
        return validate(p, false).valid;
    }

    @Override
    public boolean isAlreadyParticipating(Long userId, Long evenementId) {
        if (userId == null || evenementId == null) return false;
        String sql = "SELECT 1 FROM participations WHERE user_id = ? AND evenement_id = ? LIMIT 1";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("isAlreadyParticipating failed", e);
            return false;
        }
    }

    // Notifications (simulation console ou logger)
    @Override public void envoyerConfirmationInscription(Long id) { logger.info("Confirmation inscription {}", id); }
    @Override public void envoyerNotificationAnnulation(Long id) { logger.info("Notification annulation {}", id); }
    @Override public void envoyerNotificationConfirmation(Long id) { logger.info("Notification confirmation {}", id); }
    @Override public void notifierListeAttente(Long evenementId) { logger.info("Notification liste attente event {}", evenementId); }
    @Override public void envoyerRappelEvenement(Long id) { logger.info("Rappel événement {}", id); }
}