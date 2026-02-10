

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

        String sql = "INSERT INTO participations (user_id, evenement_id, date_inscription, type, " +
                "statut, hebergement_nuits, contexte_social, badge_associe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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

    // ================= FIND =================
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
        return List.of();
    }

    // ================= STREAM UTILS =================
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

    // ================= SEARCH =================
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

        return stream
                .sorted(Comparator.comparing(Participation::getDateInscription))
                .collect(Collectors.toList());
    }

    // ================= UTILITAIRES =================
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
                participation.getUserId() != null &&
                participation.getUserId() > 0 &&
                participation.getEvenementId() != null &&
                participation.getEvenementId() > 0 &&
                participation.getType() != null &&
                participation.getContexteSocial() != null &&
                participation.getHebergementNuits() >= 0;
    }

    @Override
    public boolean verifierConflitDates(Long userId, Long evenementId) {
        return false;
    }

    public Long getUserIdByNom(String nomUser) {
        String sql = "SELECT id FROM users WHERE nom = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            logger.error("Erreur getUserIdByNom: {}", e.getMessage());
        }
        return null;
    }

    public Long getEvenementIdByNom(String titre) {
        String sql = "SELECT id_event FROM evenement WHERE titre = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id_event");
            }
        } catch (SQLException e) {
            logger.error("Erreur getEvenementIdByNom: {}", e.getMessage());
        }
        return null;
    }

    public boolean isAlreadyParticipating(Long userId, Long evenementId) {
        return findAll().stream()
                .anyMatch(p -> p.getUserId().equals(userId) && p.getEvenementId().equals(evenementId));
    }

    // ================= MÉTHODES OBLIGATOIRES MAIS SIMPLIFIÉES =================
    @Override public Participation update(Participation participation) { return participation; }
    @Override public boolean delete(Long id) { return true; }

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

    @Override public Participation confirmerParticipation(Long id) { return null; }
    @Override public Participation annulerParticipation(Long id, String raison) { return null; }

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

    @Override public boolean peutEtreSupprimee(Long id) { return true; }

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
}


