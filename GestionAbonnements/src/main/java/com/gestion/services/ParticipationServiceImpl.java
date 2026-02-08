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

/**
 * Implémentation du service de gestion des participations
 * Sans logique de capacité
 */
/*public class ParticipationServiceImpl implements ParticipationService {
    private static final Logger logger = LoggerFactory.getLogger(ParticipationServiceImpl.class);
    private final MyConnection dbConnection;

    public ParticipationServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    @Override
    public Participation create(Participation participation) {
        if (!validerParticipation(participation)) {
            throw new IllegalArgumentException("Participation invalide");
        }

        String sql = "INSERT INTO participations (user_id, evenement_id, date_inscription, type, " +
                "statut, hebergement_nuits, contexte_social, badge_associe) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
            if (affectedRows == 0) {
                throw new SQLException("Création de participation échouée, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participation.setId(generatedKeys.getLong(1));
                    logger.info("Participation créée avec succès: ID {}", participation.getId());
                    envoyerConfirmationInscription(participation.getId());
                    return participation;
                } else {
                    throw new SQLException("Création de participation échouée, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de la participation: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer la participation", e);
        }
    }

    @Override
    public Optional<Participation> findById(Long id) {
        String sql = "SELECT * FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToParticipation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de participation par ID {}: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Participation> findAll() {
        return findAll("dateInscription", "DESC");
    }

    @Override
    public List<Participation> findAll(String sortBy, String sortOrder) {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT * FROM participations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de toutes les participations: {}", e.getMessage());
            return list;
        }
        return sortParticipations(list, sortBy != null ? sortBy : "dateInscription", sortOrder != null ? sortOrder : "DESC");
    }

    @Override
    public List<Participation> search(ParticipationCriteria criteria) {
        if (criteria == null) return findAll();
        List<Participation> list = findAll();
        Stream<Participation> stream = list.stream();
        if (criteria.getUserId() != null) stream = stream.filter(p -> criteria.getUserId().equals(p.getUserId()));
        if (criteria.getEvenementId() != null) stream = stream.filter(p -> criteria.getEvenementId().equals(p.getEvenementId()));
        if (criteria.getStatut() != null) stream = stream.filter(p -> p.getStatut() == criteria.getStatut());
        if (criteria.getType() != null) stream = stream.filter(p -> p.getType() == criteria.getType());
        if (criteria.getContexteSocial() != null) stream = stream.filter(p -> p.getContexteSocial() == criteria.getContexteSocial());
        if (criteria.getDateFrom() != null) stream = stream.filter(p -> p.getDateInscription() != null && !p.getDateInscription().isBefore(criteria.getDateFrom()));
        if (criteria.getDateTo() != null) stream = stream.filter(p -> p.getDateInscription() != null && !p.getDateInscription().isAfter(criteria.getDateTo()));
        list = stream.toList();
        return sortParticipations(list, criteria.getSortBy(), criteria.getSortOrder());
    }

    private List<Participation> sortParticipations(List<Participation> list, String sortBy, String sortOrder) {
        Comparator<Participation> cmp = switch (sortBy != null ? sortBy.toLowerCase() : "dateinscription") {
            case "statut" -> Comparator.comparing(p -> p.getStatut().name());
            case "type" -> Comparator.comparing(p -> p.getType().name());
            case "hebergementnuits" -> Comparator.comparingInt(Participation::getHebergementNuits);
            case "contexte" -> Comparator.comparing(p -> p.getContexteSocial() != null ? p.getContexteSocial().name() : "");
            default -> Comparator.comparing(Participation::getDateInscription, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).toList();
    }

    @Override
    public List<Participation> findByUserId(Long userId) {
        String sql = "SELECT * FROM participations WHERE user_id = ? ORDER BY date_inscription DESC";
        List<Participation> participations = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapResultSetToParticipation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de participations pour l'utilisateur {}: {}", userId, e.getMessage());
        }
        return participations;
    }

    @Override
    public List<Participation> findByEvenementId(Long evenementId) {
        String sql = "SELECT * FROM participations WHERE evenement_id = ? ORDER BY date_inscription ASC";
        List<Participation> participations = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, evenementId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapResultSetToParticipation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de participations pour l'événement {}: {}", evenementId, e.getMessage());
        }
        return participations;
    }

    @Override
    public Participation update(Participation participation) {
        if (!validerParticipation(participation)) throw new IllegalArgumentException("Participation invalide");
        String sql = "UPDATE participations SET user_id = ?, evenement_id = ?, date_inscription = ?, " +
                "type = ?, statut = ?, hebergement_nuits = ?, contexte_social = ?, badge_associe = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, participation.getUserId());
            pstmt.setLong(2, participation.getEvenementId());
            pstmt.setTimestamp(3, Timestamp.valueOf(participation.getDateInscription()));
            pstmt.setString(4, participation.getType().name());
            pstmt.setString(5, participation.getStatut().name());
            pstmt.setInt(6, participation.getHebergementNuits());
            pstmt.setString(7, participation.getContexteSocial().name());
            pstmt.setString(8, participation.getBadgeAssocie());
            pstmt.setLong(9, participation.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Mise à jour échouée, aucune ligne affectée.");

            logger.info("Participation mise à jour avec succès: ID {}", participation.getId());
            return participation;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la participation {}: {}", participation.getId(), e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour la participation", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprimee(id)) throw new IllegalArgumentException("La participation ne peut pas être supprimée");
        String sql = "DELETE FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) logger.info("Participation supprimée avec succès: ID {}", id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de la participation {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Participation> findByStatut(Participation.StatutParticipation statut) {
        String sql = "SELECT * FROM participations WHERE statut = ? ORDER BY date_inscription DESC";
        List<Participation> participations = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, statut.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) participations.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de participations par statut {}: {}", statut, e.getMessage());
        }
        return participations;
    }

    @Override
    public Participation confirmerParticipation(Long id) {
        Optional<Participation> optParticipation = findById(id);
        if (optParticipation.isEmpty()) throw new IllegalArgumentException("Participation non trouvée");

        Participation participation = optParticipation.get();
        participation.confirmer();
        Participation updated = update(participation);
        logger.info("Participation {} confirmée", id);
        envoyerNotificationConfirmation(id);
        return updated;
    }

    @Override
    public Participation annulerParticipation(Long id, String raison) {
        Optional<Participation> optParticipation = findById(id);
        if (optParticipation.isEmpty()) throw new IllegalArgumentException("Participation non trouvée");

        Participation participation = optParticipation.get();
        participation.annuler();
        Participation updated = update(participation);
        logger.info("Participation {} annulée. Raison: {}", id, raison);
        envoyerNotificationAnnulation(id);
        notifierListeAttente(participation.getEvenementId());
        return updated;
    }

    // Méthodes utilitaires
    private Participation mapResultSetToParticipation(ResultSet rs) throws SQLException {
        Participation participation = new Participation();
        participation.setId(rs.getLong("id"));
        participation.setUserId(rs.getLong("user_id"));
        participation.setEvenementId(rs.getLong("evenement_id"));
        participation.setDateInscription(rs.getTimestamp("date_inscription").toLocalDateTime());
        participation.setType(Participation.TypeParticipation.valueOf(rs.getString("type")));
        participation.setStatut(Participation.StatutParticipation.valueOf(rs.getString("statut")));
        participation.setHebergementNuits(rs.getInt("hebergement_nuits"));
        participation.setContexteSocial(Participation.ContexteSocial.valueOf(rs.getString("contexte_social")));
        participation.setBadgeAssocie(rs.getString("badge_associe"));
        return participation;
    }

    public void envoyerConfirmationInscription(Long id) {
        logger.info("Confirmation d'inscription envoyée pour la participation {}", id);
    }

    public void envoyerNotificationConfirmation(Long id) {
        logger.info("Notification de confirmation envoyée pour la participation {}", id);
    }

    public void envoyerNotificationAnnulation(Long id) {
        logger.info("Notification d'annulation envoyée pour la participation {}", id);
    }

    public void notifierListeAttente(Long evenementId) {
        logger.info("Liste d'attente notifiée pour l'événement {}", evenementId);
    }

    // Validation simple
    @Override
    public boolean validerParticipation(Participation participation) {
        if (participation == null) return false;
        if (participation.getUserId() == null || participation.getUserId() <= 0) return false;
        if (participation.getEvenementId() == null || participation.getEvenementId() <= 0) return false;
        if (participation.getType() == null) return false;
        if (participation.getContexteSocial() == null) return false;
        if (participation.getHebergementNuits() < 0) return false;
        return true;
    }



    @Override
    public boolean peutEtreSupprimee(Long id) {
        return findById(id).isPresent();
    }

    // Les autres méthodes restent vides ou retournent des valeurs par défaut
    @Override public List<Participation> findByType(Participation.TypeParticipation type) { return new ArrayList<>(); }
    @Override public List<Participation> findByContexteSocial(Participation.ContexteSocial contexte) { return new ArrayList<>(); }
    @Override public List<Participation> findByDateInscriptionBetween(LocalDateTime debut, LocalDateTime fin) { return new ArrayList<>(); }
    @Override public List<Participation> findByHebergementNuitsMinimum(int nuitsMin) { return new ArrayList<>(); }

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

    @Override public Participation ajouterListeAttente(Long id) { return null; }
    @Override public Participation promouvoirListeAttente(Long id) { return null; }

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

    @Override public List<Participation> findAvecHebergement() { return new ArrayList<>(); }
    @Override public Participation modifierHebergement(Long id, int nouvellesNuits) { return null; }
    @Override public boolean validerHebergement(Participation participation) { return false; }
    @Override public void attribuerBadge(Long id) {}
    @Override public List<Participation> findByBadge(String badge) { return new ArrayList<>(); }
    @Override public List<Participation> findParticipationsAvecBadge() { return new ArrayList<>(); }
    @Override public int calculerPointsParticipation(Long userId) { return 0; }
    @Override public List<Participation> suggestionsMatchingGroupe(Long participationId) { return new ArrayList<>(); }
    @Override public List<Participation> findParticipationsSimilaires(Long userId, Participation.ContexteSocial contexte) { return new ArrayList<>(); }
    @Override public boolean creerMatchingGroupe(List<Long> participationIds) { return false; }
    @Override public long countByStatut(Participation.StatutParticipation statut) { return 0; }
    @Override public long countByType(Participation.TypeParticipation type) { return 0; }
    @Override public long countByContexteSocial(Participation.ContexteSocial contexte) { return 0; }
    @Override public List<Participation> findParticipationsPeriod(LocalDateTime debut, LocalDateTime fin) { return new ArrayList<>(); }
    @Override public double calculerTauxConfirmation(Long evenementId) { return 0.0; }
    @Override public double calculerTauxAnnulation(Long evenementId) { return 0.0; }
    @Override public boolean verifierConflitDates(Long userId, Long evenementId) { return false; }
    @Override public void envoyerRappelEvenement(Long id) {}
    @Override public List<Participation> findParticipationsAvecRecommandations() { return new ArrayList<>(); }
    @Override public boolean synchroniserAvecTransport(Long id) { return false; }
    @Override public boolean synchroniserAvecPaiement(Long id) { return false; }
    @Override public List<Participation> findParticipationsAbonnementPremium() { return new ArrayList<>(); }
    @Override public String exporterCalendrier(Long userId) { return ""; }
    @Override public List<Participation> importerDonneesExterne(String source) { return new ArrayList<>(); }
    @Override public boolean integrerCalendrierExterne(Long userId, String icalData) { return false; }
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
}

