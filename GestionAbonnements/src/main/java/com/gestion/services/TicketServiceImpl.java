/*package com.gestion.services;

import com.gestion.entities.Ticket;
import com.gestion.interfaces.TicketService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implémentation du service de gestion des tickets
 * Utilise une approche Stream pour le traitement des données
 */
/*public class TicketServiceImpl implements TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final MyConnection dbConnection;

    public TicketServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }


    @Override
    public Ticket create(Ticket ticket) {
        if (!validerTicket(ticket)) {
            throw new IllegalArgumentException("Ticket invalide");
        }

        // Vérifier que la participation existe
        if (!participationExists(ticket.getParticipationId())) {
            throw new IllegalArgumentException("La participation avec ID " + ticket.getParticipationId() + " n'existe pas !");
        }

        String sql = "INSERT INTO tickets (id, user_id, type, code_unique, " +
                "latitude, longitude, lieu, statut, format, date_creation, date_expiration, qr_code, informations_supplementaires) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, ticket.getParticipationId());
            pstmt.setLong(2, ticket.getUserId());
            pstmt.setString(3, ticket.getType().name());
            pstmt.setString(4, ticket.getCodeUnique());
            pstmt.setObject(5, ticket.getLatitude(), Types.DOUBLE);
            pstmt.setObject(6, ticket.getLongitude(), Types.DOUBLE);
            pstmt.setString(7, ticket.getLieu());
            pstmt.setString(8, ticket.getStatut().name());
            pstmt.setString(9, ticket.getFormat().name());
            pstmt.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            pstmt.setTimestamp(11, ticket.getDateExpiration() != null ?
                    Timestamp.valueOf(ticket.getDateExpiration()) : null);
            pstmt.setString(12, ticket.getQrCode());
            pstmt.setString(13, ticket.getInformationsSupplementaires());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Création de ticket échouée, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setId(generatedKeys.getLong(1));
                    return ticket;
                } else {
                    throw new SQLException("Création de ticket échouée, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de créer le ticket", e);
        }
    }

    // Vérifie que la participation existe
    private boolean participationExists(Long participationId) {
        String sql = "SELECT COUNT(*) FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, participationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur vérification participation : {}", e.getMessage());
        }
        return false;
    }







    @Override
    public Optional<Ticket> findById(Long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de ticket par ID {}: {}", id, e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        return findAll("dateCreation", "DESC");
    }

    @Override
    public List<Ticket> findAll(String sortBy, String sortOrder) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les tickets: {}", e.getMessage());
            return list;
        }
        return sortTickets(list, sortBy != null ? sortBy : "dateCreation", sortOrder != null ? sortOrder : "DESC");
    }

    private List<Ticket> sortTickets(List<Ticket> list, String sortBy, String sortOrder) {
        Comparator<Ticket> cmp = switch (sortBy != null ? sortBy.toLowerCase() : "datecreation") {
            case "statut" -> Comparator.comparing(t -> t.getStatut().name());
            case "type" -> Comparator.comparing(t -> t.getType().name());
            case "format" -> Comparator.comparing(t -> t.getFormat().name());
            case "dateexpiration" -> Comparator.comparing(Ticket::getDateExpiration, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Ticket::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public Ticket update(Ticket ticket) {
        if (!validerTicket(ticket)) {
            throw new IllegalArgumentException("Ticket invalide");
        }

        String sql = "UPDATE tickets SET id = ?, user_id = ?, type = ?, " +
                    "code_unique = ?, latitude = ?, longitude = ?, lieu = ?, statut = ?, " +
                    "format = ?, date_creation = ?, date_expiration = ?, qr_code = ?, " +
                    "informations_supplementaires = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, ticket.getParticipationId());
            pstmt.setLong(2, ticket.getUserId());
            pstmt.setString(3, ticket.getType().name());
            pstmt.setString(4, ticket.getCodeUnique());
            pstmt.setObject(5, ticket.getLatitude(), Types.DOUBLE);
            pstmt.setObject(6, ticket.getLongitude(), Types.DOUBLE);
            pstmt.setString(7, ticket.getLieu());
            pstmt.setString(8, ticket.getStatut().name());
            pstmt.setString(9, ticket.getFormat().name());
            pstmt.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            pstmt.setTimestamp(11, ticket.getDateExpiration() != null ? 
                             Timestamp.valueOf(ticket.getDateExpiration()) : null);
            pstmt.setString(12, ticket.getQrCode());
            pstmt.setString(13, ticket.getInformationsSupplementaires());
            pstmt.setLong(14, ticket.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Mise à jour de ticket échouée, aucune ligne affectée.");
            }

            logger.info("Ticket mis à jour avec succès: ID {}", ticket.getId());
            return ticket;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du ticket {}: {}", ticket.getId(), e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le ticket", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) {
            throw new IllegalArgumentException("Le ticket ne peut pas être supprimé");
        }

        String sql = "DELETE FROM tickets WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Ticket supprimé avec succès: ID {}", id);
            }
            
            return deleted;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du ticket {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Ticket> findByParticipationId(Long participationId) {
        return findAll().stream()
                .filter(t -> t.getParticipationId().equals(participationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByUserId(Long userId) {
        return findAll().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByType(Ticket.TypeTicket type) {
        return findAll().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByStatut(Ticket.StatutTicket statut) {
        return findAll().stream()
                .filter(t -> t.getStatut() == statut)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByFormat(Ticket.FormatTicket format) {
        return findAll().stream()
                .filter(t -> t.getFormat() == format)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByCoordonnees(Double latitude, Double longitude, Double rayonKm) {
        if (latitude == null || longitude == null || rayonKm == null) {
            return new ArrayList<>();
        }
        
        return findAll().stream()
                .filter(t -> t.getLatitude() != null && t.getLongitude() != null)
                .filter(t -> {
                    double distance = calculerDistance(latitude, longitude, 
                                                      t.getLatitude(), t.getLongitude());
                    return distance <= rayonKm;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByLieu(String lieu) {
        if (lieu == null || lieu.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lieuLower = lieu.toLowerCase();
        return findAll().stream()
                .filter(t -> t.getLieu() != null && t.getLieu().toLowerCase().contains(lieuLower))
                .collect(Collectors.toList());
    }

    @Override
    public Ticket creerTicketSelonChoix(Long participationId, Long userId, 
                                        Ticket.TypeTicket type, 
                                        Double latitude, Double longitude, 
                                        String lieu, Ticket.FormatTicket format) {
        Ticket ticket = new Ticket(participationId, userId, type, latitude, longitude, lieu, format);
        return create(ticket);
    }




    @Override
    public Ticket marquerCommeUtilise(Long id) {
        Optional<Ticket> optTicket = findById(id);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket non trouvé");
        }

        Ticket ticket = optTicket.get();
        ticket.marquerCommeUtilise();
        return update(ticket);
    }

    @Override
    public Ticket annulerTicket(Long id) {
        Optional<Ticket> optTicket = findById(id);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket non trouvé");
        }

        Ticket ticket = optTicket.get();
        ticket.annuler();
        return update(ticket);
    }

    @Override
    public List<Ticket> findTicketsValides() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(t -> t.getStatut() == Ticket.StatutTicket.VALIDE)
                .filter(t -> t.getDateExpiration() == null || t.getDateExpiration().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findTicketsExpires() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null && t.getDateExpiration().isBefore(now))
                .filter(t -> t.getStatut() != Ticket.StatutTicket.ANNULE)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerTicket(String codeUnique) {
        if (codeUnique == null || codeUnique.trim().isEmpty()) {
            return false;
        }
        
        return findAll().stream()
                .filter(t -> codeUnique.equals(t.getCodeUnique()))
                .anyMatch(Ticket::estValide);
    }

    @Override
    public List<Ticket> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin) {
        return findAll().stream()
                .filter(t -> t.getDateCreation() != null)
                .filter(t -> !t.getDateCreation().isBefore(debut) && !t.getDateCreation().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByDateExpirationBefore(LocalDateTime date) {
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null)
                .filter(t -> t.getDateExpiration().isBefore(date))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerTicket(Ticket ticket) {
        if (ticket == null) return false;
        if (ticket.getParticipationId() == null || ticket.getParticipationId() <= 0) return false;
        if (ticket.getUserId() == null || ticket.getUserId() <= 0) return false;
        if (ticket.getType() == null) return false;
        if (ticket.getFormat() == null) return false;
        if (ticket.getCodeUnique() == null || ticket.getCodeUnique().trim().isEmpty()) return false;
        
        return true;
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        Optional<Ticket> optTicket = findById(id);
        if (optTicket.isEmpty()) return false;
        
        Ticket ticket = optTicket.get();
        // Un ticket utilisé ne peut pas être supprimé
        return ticket.getStatut() != Ticket.StatutTicket.UTILISE;
    }

    // Méthodes utilitaires privées
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setParticipationId(rs.getLong("id"));
        ticket.setUserId(rs.getLong("user_id"));
        ticket.setType(Ticket.TypeTicket.valueOf(rs.getString("type")));
        ticket.setCodeUnique(rs.getString("code_unique"));
        
        Double lat = rs.getObject("latitude", Double.class);
        ticket.setLatitude(lat);
        Double lon = rs.getObject("longitude", Double.class);
        ticket.setLongitude(lon);
        
        ticket.setLieu(rs.getString("lieu"));
        ticket.setStatut(Ticket.StatutTicket.valueOf(rs.getString("statut")));
        ticket.setFormat(Ticket.FormatTicket.valueOf(rs.getString("format")));
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            ticket.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        Timestamp dateExpiration = rs.getTimestamp("date_expiration");
        if (dateExpiration != null) {
            ticket.setDateExpiration(dateExpiration.toLocalDateTime());
        }
        
        ticket.setQrCode(rs.getString("qr_code"));
        ticket.setInformationsSupplementaires(rs.getString("informations_supplementaires"));
        
        return ticket;
    }

    /**
     * Calcule la distance en kilomètres entre deux points GPS (formule de Haversine)
     */
    /*private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }


}
*/



/*package com.gestion.services;

import com.gestion.entities.Ticket;
import com.gestion.interfaces.TicketService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des tickets
 */
/*public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final MyConnection dbConnection;

    public TicketServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    // ----------------- CREATION -----------------
    @Override
    public Ticket create(Ticket ticket) {
        if (!validerTicket(ticket)) {
            throw new IllegalArgumentException("Ticket invalide");
        }

        if (!participationValide(ticket.getParticipationId())) {
            throw new IllegalArgumentException("La participation avec ID " + ticket.getParticipationId() + " n'existe pas ou n'est pas confirmée !");
        }

        if (ticketExiste(ticket.getParticipationId(), ticket.getUserId())) {
            throw new IllegalArgumentException("Un ticket existe déjà pour cette participation et cet utilisateur !");
        }

        // Par défaut, statut = VALIDE si null
        if (ticket.getStatut() == null) ticket.setStatut(Ticket.StatutTicket.VALIDE);
        if (ticket.getCodeUnique() == null || ticket.getCodeUnique().trim().isEmpty()) {
            ticket.setCodeUnique(UUID.randomUUID().toString());
        }
        if (ticket.getDateCreation() == null) ticket.setDateCreation(LocalDateTime.now());

        String sql = "INSERT INTO tickets (participationId, user_id, type, code_unique, " +
                "latitude, longitude, lieu, statut, format, date_creation, date_expiration, qr_code, informations_supplementaires) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, ticket.getParticipationId());
            pstmt.setLong(2, ticket.getUserId());
            pstmt.setString(3, ticket.getType().name());
            pstmt.setString(4, ticket.getCodeUnique());
            pstmt.setObject(5, ticket.getLatitude(), Types.DOUBLE);
            pstmt.setObject(6, ticket.getLongitude(), Types.DOUBLE);
            pstmt.setString(7, ticket.getLieu());
            pstmt.setString(8, ticket.getStatut().name());
            pstmt.setString(9, ticket.getFormat() != null ? ticket.getFormat().name() : Ticket.FormatTicket.HYBRIDE.name());
            pstmt.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            pstmt.setTimestamp(11, ticket.getDateExpiration() != null ? Timestamp.valueOf(ticket.getDateExpiration()) : null);
            pstmt.setString(12, ticket.getQrCode());
            pstmt.setString(13, ticket.getInformationsSupplementaires());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Création de ticket échouée.");

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setId(generatedKeys.getLong(1));
                    return ticket;
                } else {
                    throw new SQLException("Création de ticket échouée, aucun ID généré.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Impossible de créer le ticket", e);
        }
    }

    // ----------------- UTILITAIRES -----------------
    private boolean participationValide(Long participationId) {
        String sql = "SELECT statut FROM participations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, participationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "CONFIRME".equalsIgnoreCase(rs.getString("statut"));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur vérification participation : {}", e.getMessage());
        }
        return false;
    }

    private boolean ticketExiste(Long participationId, Long userId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE participationId = ? AND user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, participationId);
            pstmt.setLong(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erreur vérification ticket existant : {}", e.getMessage());
        }
        return false;
    }

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setParticipationId(rs.getLong("participationId")); // corrigé
        ticket.setUserId(rs.getLong("user_id"));
        ticket.setType(Ticket.TypeTicket.valueOf(rs.getString("type")));
        ticket.setCodeUnique(rs.getString("code_unique"));
        ticket.setLatitude(rs.getObject("latitude", Double.class));
        ticket.setLongitude(rs.getObject("longitude", Double.class));
        ticket.setLieu(rs.getString("lieu"));
        ticket.setStatut(Ticket.StatutTicket.valueOf(rs.getString("statut")));
        ticket.setFormat(Ticket.FormatTicket.valueOf(rs.getString("format")));
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) ticket.setDateCreation(dateCreation.toLocalDateTime());
        Timestamp dateExpiration = rs.getTimestamp("date_expiration");
        if (dateExpiration != null) ticket.setDateExpiration(dateExpiration.toLocalDateTime());
        ticket.setQrCode(rs.getString("qr_code"));
        ticket.setInformationsSupplementaires(rs.getString("informations_supplementaires"));
        return ticket;
    }

    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ----------------- RECHERCHE -----------------
    @Override
    public Optional<Ticket> findById(Long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findById: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        return findAll("date_creation", "DESC");
    }

    @Override
    public List<Ticket> findAll(String sortBy, String sortOrder) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToTicket(rs));
        } catch (SQLException e) {
            logger.error("Erreur findAll: {}", e.getMessage());
            return list;
        }
        return sortTickets(list, sortBy, sortOrder);
    }

    private List<Ticket> sortTickets(List<Ticket> list, String sortBy, String sortOrder) {
        Comparator<Ticket> cmp = switch (sortBy.toLowerCase()) {
            case "statut" -> Comparator.comparing(t -> t.getStatut().name());
            case "type" -> Comparator.comparing(t -> t.getType().name());
            case "format" -> Comparator.comparing(t -> t.getFormat().name());
            case "dateexpiration" -> Comparator.comparing(Ticket::getDateExpiration, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Ticket::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).collect(Collectors.toList());
    }

    // ----------------- MISE A JOUR -----------------
    @Override
    public Ticket update(Ticket ticket) {
        if (!validerTicket(ticket)) throw new IllegalArgumentException("Ticket invalide");
        String sql = "UPDATE tickets SET participationId = ?, user_id = ?, type = ?, code_unique = ?, " +
                "latitude = ?, longitude = ?, lieu = ?, statut = ?, format = ?, date_creation = ?, date_expiration = ?, " +
                "qr_code = ?, informations_supplementaires = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, ticket.getParticipationId());
            pstmt.setLong(2, ticket.getUserId());
            pstmt.setString(3, ticket.getType().name());
            pstmt.setString(4, ticket.getCodeUnique());
            pstmt.setObject(5, ticket.getLatitude(), Types.DOUBLE);
            pstmt.setObject(6, ticket.getLongitude(), Types.DOUBLE);
            pstmt.setString(7, ticket.getLieu());
            pstmt.setString(8, ticket.getStatut().name());
            pstmt.setString(9, ticket.getFormat().name());
            pstmt.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            pstmt.setTimestamp(11, ticket.getDateExpiration() != null ? Timestamp.valueOf(ticket.getDateExpiration()) : null);
            pstmt.setString(12, ticket.getQrCode());
            pstmt.setString(13, ticket.getInformationsSupplementaires());
            pstmt.setLong(14, ticket.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Mise à jour échouée.");
            return ticket;
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de mettre à jour le ticket", e);
        }
    }

    // ----------------- SUPPRESSION -----------------
    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) return false;
        String sql = "DELETE FROM tickets WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        Optional<Ticket> t = findById(id);
        return t.isPresent() && t.get().getStatut() != Ticket.StatutTicket.UTILISE;
    }

    // ----------------- VALIDATION -----------------
    @Override
    public boolean validerTicket(Ticket ticket) {
        if (ticket == null) return false;
        return ticket.getParticipationId() != null && ticket.getParticipationId() > 0 &&
                ticket.getUserId() != null && ticket.getUserId() > 0 &&
                ticket.getType() != null &&
                ticket.getFormat() != null;
    }

    @Override
    public boolean validerTicket(String codeUnique) {
        if (codeUnique == null || codeUnique.trim().isEmpty()) return false;
        return findAll().stream().anyMatch(t -> codeUnique.equals(t.getCodeUnique()) && t.estValide());
    }

    // ----------------- CREATION RAPIDE -----------------
    @Override
    public Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                        Ticket.TypeTicket type,
                                        Double latitude, Double longitude,
                                        String lieu, Ticket.FormatTicket format) {
        Ticket ticket = new Ticket(participationId, userId, type, latitude, longitude, lieu, format);
        ticket.setDateCreation(LocalDateTime.now());
        ticket.setStatut(Ticket.StatutTicket.VALIDE);
        ticket.setCodeUnique(UUID.randomUUID().toString());
        return create(ticket);
    }

    // ----------------- RECHERCHES AVANCEES -----------------
    @Override
    public List<Ticket> findByParticipationId(Long participationId) {
        return findAll().stream().filter(t -> t.getParticipationId().equals(participationId)).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByUserId(Long userId) {
        return findAll().stream().filter(t -> t.getUserId().equals(userId)).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByType(Ticket.TypeTicket type) {
        return findAll().stream().filter(t -> t.getType() == type).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByStatut(Ticket.StatutTicket statut) {
        return findAll().stream().filter(t -> t.getStatut() == statut).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByFormat(Ticket.FormatTicket format) {
        return findAll().stream().filter(t -> t.getFormat() == format).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByCoordonnees(Double latitude, Double longitude, Double rayonKm) {
        if (latitude == null || longitude == null || rayonKm == null) return new ArrayList<>();
        return findAll().stream()
                .filter(t -> t.getLatitude() != null && t.getLongitude() != null)
                .filter(t -> calculerDistance(latitude, longitude, t.getLatitude(), t.getLongitude()) <= rayonKm)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByLieu(String lieu) {
        if (lieu == null || lieu.trim().isEmpty()) return new ArrayList<>();
        String lieuLower = lieu.toLowerCase();
        return findAll().stream()
                .filter(t -> t.getLieu() != null && t.getLieu().toLowerCase().contains(lieuLower))
                .collect(Collectors.toList());
    }

    @Override
    public Ticket marquerCommeUtilise(Long id) {
        Ticket t = findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket non trouvé"));
        t.marquerCommeUtilise();
        return update(t);
    }

    @Override
    public Ticket annulerTicket(Long id) {
        Ticket t = findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket non trouvé"));
        t.annuler();
        return update(t);
    }

    @Override
    public List<Ticket> findTicketsValides() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(t -> t.getStatut() == Ticket.StatutTicket.VALIDE)
                .filter(t -> t.getDateExpiration() == null || t.getDateExpiration().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findTicketsExpires() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null && t.getDateExpiration().isBefore(now))
                .filter(t -> t.getStatut() != Ticket.StatutTicket.ANNULE)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin) {
        return findAll().stream()
                .filter(t -> t.getDateCreation() != null)
                .filter(t -> !t.getDateCreation().isBefore(debut) && !t.getDateCreation().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByDateExpirationBefore(LocalDateTime date) {
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null)
                .filter(t -> t.getDateExpiration().isBefore(date))
                .collect(Collectors.toList());
    }
}*/
/*package com.gestion.services;

import com.gestion.entities.Ticket;
import com.gestion.interfaces.TicketService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final MyConnection dbConnection = MyConnection.getInstance();

    // ==================== VALIDATION RICHE ====================
    private void validerTicketComplet(Ticket t) {
        List<String> erreurs = new ArrayList<>();

        if (t == null) erreurs.add("Le ticket est null... comme s'il n'avait jamais existé ! 😢");
        if (t.getParticipationId() == null || t.getParticipationId() <= 0)
            erreurs.add("Participation ID obligatoire et positif – c'est la base du ticket !");
        if (t.getUserId() == null || t.getUserId() <= 0)
            erreurs.add("User ID obligatoire – à qui on donne ce précieux sésame ?");
        if (t.getType() == null)
            erreurs.add("Type de ticket manquant – TICKET, BADGE ou PASS ?");
        if (t.getFormat() == null)
            erreurs.add("Format manquant – Numérique, Physique ou Hybride ? Choisis ton style !");
        if (t.getCodeUnique() == null || t.getCodeUnique().trim().isEmpty())
            erreurs.add("Code unique vide – chaque ticket doit être unique comme une étoile !");

        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException("Ticket invalide :\n" + String.join("\n", erreurs));
        }
    }

    @Override
    public boolean validerTicket(Ticket ticket) {
        if (ticket == null) {
            logger.warn("Validation échouée : ticket null");
            return false;
        }

        List<String> messagesErreur = new ArrayList<>();

        if (ticket.getParticipationId() == null || ticket.getParticipationId() <= 0) {
            messagesErreur.add("Participation ID manquant ou invalide (doit être > 0)");
        }
        if (ticket.getUserId() == null || ticket.getUserId() <= 0) {
            messagesErreur.add("User ID manquant ou invalide (doit être > 0)");
        }
        if (ticket.getType() == null) {
            messagesErreur.add("Type de ticket non défini (TICKET/BADGE/PASS)");
        }
        if (ticket.getFormat() == null) {
            messagesErreur.add("Format de ticket non défini (NUMERIQUE/PHYSIQUE/HYBRIDE)");
        }
        if (ticket.getCodeUnique() == null || ticket.getCodeUnique().trim().isEmpty()) {
            messagesErreur.add("Code unique absent ou vide");
        }

        if (!messagesErreur.isEmpty()) {
            logger.warn("Ticket invalide (id={}) : {}", ticket.getId(), String.join(" | ", messagesErreur));
            return false;
        }

        logger.debug("Ticket validé avec succès (participationId={})", ticket.getParticipationId());
        return true;
    }

    // ==================== MÉTHODES PRIVÉES DE VÉRIFICATION ====================
    private boolean participationExisteEtEstValide(Long id) {
        if (id == null || id <= 0) return false;

        String sql = "SELECT statut FROM participations WHERE id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "CONFIRME".equalsIgnoreCase(rs.getString("statut"));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification de la participation ID {}", id, e);
            return false;
        }
    }

    private boolean ticketExiste(Long participationId, Long userId) {
        if (participationId == null || userId == null) return false;

        String sql = "SELECT COUNT(*) FROM tickets WHERE participationId = ? AND user_id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, participationId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification d'existence de ticket", e);
            return false;
        }
    }

    @Override
    public Ticket getById(Long id) {
        return null;
    }

    @Override
    public List<Ticket> getAll() {
        return List.of();
    }

    @Override
    public List<Ticket> getAll(String sortBy, String sortOrder) {
        return List.of();
    }

    // ==================== CREATE ====================
    @Override
    public Ticket create(Ticket ticket) {
        validerTicketComplet(ticket);

        if (ticketExiste(ticket.getParticipationId(), ticket.getUserId())) {
            throw new IllegalStateException("Un ticket existe déjà pour cette participation et cet utilisateur !");
        }

        if (!participationExisteEtEstValide(ticket.getParticipationId())) {
            throw new IllegalStateException("La participation " + ticket.getParticipationId() + " n'existe pas ou n'est pas confirmée.");
        }

        if (ticket.getStatut() == null) ticket.setStatut(Ticket.StatutTicket.VALIDE);
        if (ticket.getCodeUnique() == null || ticket.getCodeUnique().trim().isEmpty()) {
            ticket.setCodeUnique(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (ticket.getDateCreation() == null) ticket.setDateCreation(LocalDateTime.now());

        String sql = """
            INSERT INTO tickets 
            (participationId, user_id, type, code_unique, latitude, longitude, lieu, statut, format,
             date_creation, date_expiration, qr_code, informations_supplementaires)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, ticket.getParticipationId());
            ps.setLong(2, ticket.getUserId());
            ps.setString(3, ticket.getType().name());
            ps.setString(4, ticket.getCodeUnique());
            ps.setObject(5, ticket.getLatitude());
            ps.setObject(6, ticket.getLongitude());
            ps.setString(7, ticket.getLieu());
            ps.setString(8, ticket.getStatut().name());
            ps.setString(9, ticket.getFormat().name());
            ps.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            ps.setTimestamp(11, ticket.getDateExpiration() != null ? Timestamp.valueOf(ticket.getDateExpiration()) : null);
            ps.setString(12, ticket.getQrCode());
            ps.setString(13, ticket.getInformationsSupplementaires());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne insérée");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ticket.setId(keys.getLong(1));
                    logger.info("Ticket créé → ID: {}", ticket.getId());
                    return ticket;
                }
            }
            throw new SQLException("Aucun ID généré");
        } catch (SQLException e) {
            logger.error("Échec création ticket", e);
            throw new RuntimeException("Impossible de créer le ticket : " + e.getMessage(), e);
        }
    }

    // ==================== CREATION RAPIDE ====================
    @Override
    public Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                        Ticket.TypeTicket type,
                                        Double latitude, Double longitude,
                                        String lieu, Ticket.FormatTicket format) {
        Ticket ticket = new Ticket(participationId, userId, type, latitude, longitude, lieu, format);
        return create(ticket);
    }

    // ==================== READ ====================
    @Override
    public Optional<Ticket> findById(Long id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapToTicket(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findById {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        return findAll("date_creation", "DESC");
    }

    @Override
    public List<Ticket> findAll(String sortBy, String sortOrder) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets";

        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapToTicket(rs));
        } catch (SQLException e) {
            logger.error("Erreur findAll", e);
        }

        return trierTickets(list, sortBy, sortOrder);
    }

    private List<Ticket> trierTickets(List<Ticket> tickets, String sortBy, String sortOrder) {
        Comparator<Ticket> comparator = switch (sortBy.toLowerCase()) {
            case "dateexpiration" -> Comparator.comparing(Ticket::getDateExpiration, Comparator.nullsLast(Comparator.naturalOrder()));
            case "statut" -> Comparator.comparing(t -> t.getStatut().name());
            case "type" -> Comparator.comparing(t -> t.getType().name());
            case "format" -> Comparator.comparing(t -> t.getFormat().name());
            default -> Comparator.comparing(Ticket::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return tickets.stream().sorted(comparator).collect(Collectors.toList());
    }

    // ==================== MAPPING ====================
    private Ticket mapToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setParticipationId(rs.getLong("participationId"));
        ticket.setUserId(rs.getLong("user_id"));
        ticket.setType(Ticket.TypeTicket.valueOf(rs.getString("type")));
        ticket.setCodeUnique(rs.getString("code_unique"));
        ticket.setLatitude(rs.getObject("latitude", Double.class));
        ticket.setLongitude(rs.getObject("longitude", Double.class));
        ticket.setLieu(rs.getString("lieu"));
        ticket.setStatut(Ticket.StatutTicket.valueOf(rs.getString("statut")));
        ticket.setFormat(Ticket.FormatTicket.valueOf(rs.getString("format")));

        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) ticket.setDateCreation(dc.toLocalDateTime());

        Timestamp de = rs.getTimestamp("date_expiration");
        if (de != null) ticket.setDateExpiration(de.toLocalDateTime());

        ticket.setQrCode(rs.getString("qr_code"));
        ticket.setInformationsSupplementaires(rs.getString("informations_supplementaires"));

        return ticket;
    }

    // ==================== UPDATE ====================
    @Override
    public Ticket update(Ticket ticket) {
        validerTicketComplet(ticket);

        String sql = """
            UPDATE tickets SET 
            participationId = ?, user_id = ?, type = ?, code_unique = ?, latitude = ?, longitude = ?, lieu = ?,
            statut = ?, format = ?, date_creation = ?, date_expiration = ?, qr_code = ?, informations_supplementaires = ?
            WHERE id = ?
        """;

        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, ticket.getParticipationId());
            ps.setLong(2, ticket.getUserId());
            ps.setString(3, ticket.getType().name());
            ps.setString(4, ticket.getCodeUnique());
            ps.setObject(5, ticket.getLatitude());
            ps.setObject(6, ticket.getLongitude());
            ps.setString(7, ticket.getLieu());
            ps.setString(8, ticket.getStatut().name());
            ps.setString(9, ticket.getFormat().name());
            ps.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            ps.setTimestamp(11, ticket.getDateExpiration() != null ? Timestamp.valueOf(ticket.getDateExpiration()) : null);
            ps.setString(12, ticket.getQrCode());
            ps.setString(13, ticket.getInformationsSupplementaires());
            ps.setLong(14, ticket.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne mise à jour");

            return ticket;
        } catch (SQLException e) {
            throw new RuntimeException("Échec mise à jour ticket", e);
        }
    }

    // ==================== DELETE ====================
    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) return false;

        String sql = "DELETE FROM tickets WHERE id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Échec suppression ticket {}", id, e);
            return false;
        }
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        return findById(id)
                .map(t -> t.getStatut() != Ticket.StatutTicket.UTILISE)
                .orElse(false);
    }

    // ==================== RECHERCHES AVEC STREAM ====================

    @Override
    public List<Ticket> findByParticipationId(Long participationId) {
        return findAll().stream()
                .filter(t -> Objects.equals(t.getParticipationId(), participationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByUserId(Long userId) {
        return findAll().stream()
                .filter(t -> Objects.equals(t.getUserId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByType(Ticket.TypeTicket type) {
        return findAll().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByStatut(Ticket.StatutTicket statut) {
        return findAll().stream()
                .filter(t -> t.getStatut() == statut)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByFormat(Ticket.FormatTicket format) {
        return findAll().stream()
                .filter(t -> t.getFormat() == format)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByCoordonnees(Double latitude, Double longitude, Double rayonKm) {
        if (latitude == null || longitude == null || rayonKm == null) return List.of();

        return findAll().stream()
                .filter(t -> t.getLatitude() != null && t.getLongitude() != null)
                .filter(t -> calculerDistance(latitude, longitude, t.getLatitude(), t.getLongitude()) <= rayonKm)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByLieu(String lieu) {
        if (lieu == null || lieu.trim().isEmpty()) return List.of();
        String lower = lieu.toLowerCase();
        return findAll().stream()
                .filter(t -> t.getLieu() != null && t.getLieu().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public Ticket marquerCommeUtilise(Long id) {
        Ticket t = findById(id).orElseThrow(() -> new NoSuchElementException("Ticket non trouvé : " + id));
        t.marquerCommeUtilise();
        return update(t);
    }

    @Override
    public Ticket annulerTicket(Long id) {
        Ticket t = findById(id).orElseThrow(() -> new NoSuchElementException("Ticket non trouvé : " + id));
        t.annuler();
        return update(t);
    }

    @Override
    public List<Ticket> findTicketsValides() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(Ticket::estValide)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findTicketsExpires() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null && t.getDateExpiration().isBefore(now))
                .filter(t -> t.getStatut() != Ticket.StatutTicket.ANNULE)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerTicket(String codeUnique) {
        if (codeUnique == null || codeUnique.trim().isEmpty()) return false;
        return findAll().stream()
                .anyMatch(t -> codeUnique.equals(t.getCodeUnique()) && t.estValide());
    }

    @Override
    public List<Ticket> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin) {
        return findAll().stream()
                .filter(t -> t.getDateCreation() != null)
                .filter(t -> !t.getDateCreation().isBefore(debut) && !t.getDateCreation().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByDateExpirationBefore(LocalDateTime date) {
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null)
                .filter(t -> t.getDateExpiration().isBefore(date))
                .collect(Collectors.toList());
    }

    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}*/
package com.gestion.services;

import com.gestion.entities.Ticket;
import com.gestion.interfaces.TicketService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final MyConnection dbConnection = MyConnection.getInstance();

    // ==================== VALIDATION RICHE ====================
    private void validerTicketComplet(Ticket t) {
        List<String> erreurs = new ArrayList<>();

        if (t == null) erreurs.add("Le ticket est null... comme s'il n'avait jamais existé ! 😢");
        if (t.getParticipationId() == null || t.getParticipationId() <= 0)
            erreurs.add("Participation ID obligatoire et positif – c'est la base du ticket !");
        if (t.getUserId() == null || t.getUserId() <= 0)
            erreurs.add("User ID obligatoire – à qui on donne ce précieux sésame ?");
        if (t.getType() == null)
            erreurs.add("Type de ticket manquant – TICKET, BADGE ou PASS ?");
        if (t.getFormat() == null)
            erreurs.add("Format manquant – Numérique, Physique ou Hybride ? Choisis ton style !");
        if (t.getCodeUnique() == null || t.getCodeUnique().trim().isEmpty())
            erreurs.add("Code unique vide – chaque ticket doit être unique comme une étoile !");

        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException("Ticket invalide :\n" + String.join("\n", erreurs));
        }
    }

    @Override
    public boolean validerTicket(Ticket ticket) {
        if (ticket == null) {
            logger.warn("Validation échouée : ticket null");
            return false;
        }

        List<String> messagesErreur = new ArrayList<>();

        if (ticket.getParticipationId() == null || ticket.getParticipationId() <= 0) {
            messagesErreur.add("Participation ID manquant ou invalide (doit être > 0)");
        }
        if (ticket.getUserId() == null || ticket.getUserId() <= 0) {
            messagesErreur.add("User ID manquant ou invalide (doit être > 0)");
        }
        if (ticket.getType() == null) {
            messagesErreur.add("Type de ticket non défini (TICKET/BADGE/PASS)");
        }
        if (ticket.getFormat() == null) {
            messagesErreur.add("Format de ticket non défini (NUMERIQUE/PHYSIQUE/HYBRIDE)");
        }
        if (ticket.getCodeUnique() == null || ticket.getCodeUnique().trim().isEmpty()) {
            messagesErreur.add("Code unique absent ou vide");
        }

        if (!messagesErreur.isEmpty()) {
            logger.warn("Ticket invalide (id={}) : {}", ticket.getId(), String.join(" | ", messagesErreur));
            return false;
        }

        logger.debug("Ticket validé avec succès (participationId={})", ticket.getParticipationId());
        return true;
    }

    // ==================== VÉRIFICATIONS PRIVÉES ====================
    private boolean participationExisteEtEstValide(Long id) {
        if (id == null || id <= 0) return false;

        String sql = "SELECT statut FROM participations WHERE id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "CONFIRME".equalsIgnoreCase(rs.getString("statut"));
            }
        } catch (SQLException e) {
            logger.error("Erreur vérification participation ID {}", id, e);
            return false;
        }
    }

    private boolean ticketExiste(Long participationId, Long userId) {
        if (participationId == null || userId == null) return false;

        String sql = "SELECT COUNT(*) FROM tickets WHERE participationId = ? AND user_id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, participationId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erreur vérification doublon ticket", e);
            return false;
        }
    }

    // ==================== CREATE ====================
    @Override
    public Ticket create(Ticket ticket) {
        validerTicketComplet(ticket);

        if (ticketExiste(ticket.getParticipationId(), ticket.getUserId())) {
            throw new IllegalStateException("Un ticket existe déjà pour cette participation et cet utilisateur !");
        }

        if (!participationExisteEtEstValide(ticket.getParticipationId())) {
            throw new IllegalStateException("La participation " + ticket.getParticipationId() + " n'existe pas ou n'est pas confirmée.");
        }

        if (ticket.getStatut() == null) ticket.setStatut(Ticket.StatutTicket.VALIDE);
        if (ticket.getCodeUnique() == null || ticket.getCodeUnique().trim().isEmpty()) {
            ticket.setCodeUnique(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (ticket.getDateCreation() == null) ticket.setDateCreation(LocalDateTime.now());

        String sql = """
            INSERT INTO tickets 
            (participationId, user_id, type, code_unique, latitude, longitude, lieu, statut, format,
             date_creation, date_expiration, qr_code, informations_supplementaires)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, ticket.getParticipationId());
            ps.setLong(2, ticket.getUserId());
            ps.setString(3, ticket.getType().name());
            ps.setString(4, ticket.getCodeUnique());
            ps.setObject(5, ticket.getLatitude());
            ps.setObject(6, ticket.getLongitude());
            ps.setString(7, ticket.getLieu());
            ps.setString(8, ticket.getStatut().name());
            ps.setString(9, ticket.getFormat().name());
            ps.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            ps.setTimestamp(11, ticket.getDateExpiration() != null ? Timestamp.valueOf(ticket.getDateExpiration()) : null);
            ps.setString(12, ticket.getQrCode());
            ps.setString(13, ticket.getInformationsSupplementaires());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne insérée");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ticket.setId(keys.getLong(1));
                    logger.info("Ticket créé → ID: {}", ticket.getId());
                    return ticket;
                }
            }
            throw new SQLException("Aucun ID généré");
        } catch (SQLException e) {
            logger.error("Échec création ticket", e);
            throw new RuntimeException("Impossible de créer le ticket : " + e.getMessage(), e);
        }
    }

    @Override
    public Ticket creerTicketSelonChoix(Long participationId, Long userId,
                                        Ticket.TypeTicket type,
                                        Double latitude, Double longitude,
                                        String lieu, Ticket.FormatTicket format) {
        Ticket ticket = new Ticket(participationId, userId, type, latitude, longitude, lieu, format);
        return create(ticket);
    }

    // ==================== READ ====================
    @Override
    public Optional<Ticket> findById(Long id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapToTicket(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findById {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        return findAll("date_creation", "DESC");
    }

    @Override
    public List<Ticket> findAll(String sortBy, String sortOrder) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets";

        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapToTicket(rs));
        } catch (SQLException e) {
            logger.error("Erreur findAll", e);
        }

        return trierTickets(list, sortBy, sortOrder);
    }

    private List<Ticket> trierTickets(List<Ticket> tickets, String sortBy, String sortOrder) {
        Comparator<Ticket> comparator = switch (sortBy.toLowerCase()) {
            case "dateexpiration" -> Comparator.comparing(Ticket::getDateExpiration, Comparator.nullsLast(Comparator.naturalOrder()));
            case "statut" -> Comparator.comparing(t -> t.getStatut().name());
            case "type" -> Comparator.comparing(t -> t.getType().name());
            case "format" -> Comparator.comparing(t -> t.getFormat().name());
            default -> Comparator.comparing(Ticket::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return tickets.stream().sorted(comparator).collect(Collectors.toList());
    }

    private Ticket mapToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setParticipationId(rs.getLong("participationId"));
        ticket.setUserId(rs.getLong("user_id"));
        ticket.setType(Ticket.TypeTicket.valueOf(rs.getString("type")));
        ticket.setCodeUnique(rs.getString("code_unique"));
        ticket.setLatitude(rs.getObject("latitude", Double.class));
        ticket.setLongitude(rs.getObject("longitude", Double.class));
        ticket.setLieu(rs.getString("lieu"));
        ticket.setStatut(Ticket.StatutTicket.valueOf(rs.getString("statut")));
        ticket.setFormat(Ticket.FormatTicket.valueOf(rs.getString("format")));

        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) ticket.setDateCreation(dc.toLocalDateTime());

        Timestamp de = rs.getTimestamp("date_expiration");
        if (de != null) ticket.setDateExpiration(de.toLocalDateTime());

        ticket.setQrCode(rs.getString("qr_code"));
        ticket.setInformationsSupplementaires(rs.getString("informations_supplementaires"));

        return ticket;
    }

    // ==================== UPDATE ====================
    @Override
    public Ticket update(Ticket ticket) {
        validerTicketComplet(ticket);

        String sql = """
            UPDATE tickets SET 
            participationId = ?, user_id = ?, type = ?, code_unique = ?, latitude = ?, longitude = ?, lieu = ?,
            statut = ?, format = ?, date_creation = ?, date_expiration = ?, qr_code = ?, informations_supplementaires = ?
            WHERE id = ?
        """;

        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, ticket.getParticipationId());
            ps.setLong(2, ticket.getUserId());
            ps.setString(3, ticket.getType().name());
            ps.setString(4, ticket.getCodeUnique());
            ps.setObject(5, ticket.getLatitude());
            ps.setObject(6, ticket.getLongitude());
            ps.setString(7, ticket.getLieu());
            ps.setString(8, ticket.getStatut().name());
            ps.setString(9, ticket.getFormat().name());
            ps.setTimestamp(10, Timestamp.valueOf(ticket.getDateCreation()));
            ps.setTimestamp(11, ticket.getDateExpiration() != null ? Timestamp.valueOf(ticket.getDateExpiration()) : null);
            ps.setString(12, ticket.getQrCode());
            ps.setString(13, ticket.getInformationsSupplementaires());
            ps.setLong(14, ticket.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Aucune ligne mise à jour");

            return ticket;
        } catch (SQLException e) {
            throw new RuntimeException("Échec mise à jour ticket", e);
        }
    }

    // ==================== DELETE ====================
    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) return false;

        String sql = "DELETE FROM tickets WHERE id = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Échec suppression ticket {}", id, e);
            return false;
        }
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        return findById(id)
                .map(t -> t.getStatut() != Ticket.StatutTicket.UTILISE)
                .orElse(false);
    }

    // ==================== RECHERCHES AVEC STREAM ====================

    @Override
    public List<Ticket> findByParticipationId(Long participationId) {
        return findAll().stream()
                .filter(t -> Objects.equals(t.getParticipationId(), participationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByUserId(Long userId) {
        return findAll().stream()
                .filter(t -> Objects.equals(t.getUserId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByType(Ticket.TypeTicket type) {
        return findAll().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByStatut(Ticket.StatutTicket statut) {
        return findAll().stream()
                .filter(t -> t.getStatut() == statut)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByFormat(Ticket.FormatTicket format) {
        return findAll().stream()
                .filter(t -> t.getFormat() == format)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByCoordonnees(Double latitude, Double longitude, Double rayonKm) {
        if (latitude == null || longitude == null || rayonKm == null) return List.of();

        return findAll().stream()
                .filter(t -> t.getLatitude() != null && t.getLongitude() != null)
                .filter(t -> calculerDistance(latitude, longitude, t.getLatitude(), t.getLongitude()) <= rayonKm)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByLieu(String lieu) {
        if (lieu == null || lieu.trim().isEmpty()) return List.of();
        String lower = lieu.toLowerCase();
        return findAll().stream()
                .filter(t -> t.getLieu() != null && t.getLieu().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public Ticket marquerCommeUtilise(Long id) {
        Ticket t = findById(id).orElseThrow(() -> new NoSuchElementException("Ticket non trouvé : " + id));
        t.marquerCommeUtilise();
        return update(t);
    }

    @Override
    public Ticket annulerTicket(Long id) {
        Ticket t = findById(id).orElseThrow(() -> new NoSuchElementException("Ticket non trouvé : " + id));
        t.annuler();
        return update(t);
    }

    @Override
    public List<Ticket> findTicketsValides() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(Ticket::estValide)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findTicketsExpires() {
        LocalDateTime now = LocalDateTime.now();
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null && t.getDateExpiration().isBefore(now))
                .filter(t -> t.getStatut() != Ticket.StatutTicket.ANNULE)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerTicket(String codeUnique) {
        if (codeUnique == null || codeUnique.trim().isEmpty()) return false;
        return findAll().stream()
                .anyMatch(t -> codeUnique.equals(t.getCodeUnique()) && t.estValide());
    }

    @Override
    public List<Ticket> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin) {
        return findAll().stream()
                .filter(t -> t.getDateCreation() != null)
                .filter(t -> !t.getDateCreation().isBefore(debut) && !t.getDateCreation().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByDateExpirationBefore(LocalDateTime date) {
        return findAll().stream()
                .filter(t -> t.getDateExpiration() != null)
                .filter(t -> t.getDateExpiration().isBefore(date))
                .collect(Collectors.toList());
    }

    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}