/*package com.gestion.services;

import com.gestion.entities.ProgrammeRecommender;
import com.gestion.interfaces.ProgrammeService;
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

/**
 * Implémentation du service de gestion des programmes
 * Utilise une approche Stream pour le traitement des données
 */
/*public class ProgrammerecomanderServiceImpl implements ProgrammeService {
    private static final Logger logger = LoggerFactory.getLogger(ProgrammerecomanderServiceImpl.class);
    private final MyConnection dbConnection;

    public ProgrammerecomanderServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    @Override
    public ProgrammeRecommender create(ProgrammeRecommender programme) {
        if (!validerProgramme(programme)) {
            throw new IllegalArgumentException("Programme invalide");
        }

        String sql = "INSERT INTO programme (event_id, titre, debut, fin) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, programme.getEventId());
            pstmt.setString(2, programme.getTitre());
            pstmt.setTimestamp(3, Timestamp.valueOf(programme.getDebut()));
            pstmt.setTimestamp(4, Timestamp.valueOf(programme.getFin()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Création de programme échouée, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    programme.setIdProg(generatedKeys.getLong(1));
                    logger.info("Programme créé avec succès: ID {}", programme.getIdProg());
                    return programme;
                } else {
                    throw new SQLException("Création de programme échouée, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création du programme: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer le programme", e);
        }
    }

    @Override
    public Optional<ProgrammeRecommender> findById(Long id) {
        String sql = "SELECT * FROM programme WHERE id_prog = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProgramme(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de programme par ID {}: {}", id, e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<ProgrammeRecommender> findAll() {
        return findAll("debut", "ASC");
    }

    @Override
    public List<ProgrammeRecommender> findAll(String sortBy, String sortOrder) {
        List<ProgrammeRecommender> list = new ArrayList<>();
        String sql = "SELECT * FROM programme";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les programmes: {}", e.getMessage());
            return list;
        }
        return sortProgrammes(list, sortBy != null ? sortBy : "debut", sortOrder != null ? sortOrder : "ASC");
    }

    private List<ProgrammeRecommender> sortProgrammes(List<ProgrammeRecommender> list, String sortBy, String sortOrder) {
        Comparator<ProgrammeRecommender> cmp = switch (sortBy != null ? sortBy.toLowerCase() : "debut") {
            case "titre" -> Comparator.comparing(ProgrammeRecommender::getTitre);
            case "fin" -> Comparator.comparing(ProgrammeRecommender::getFin, Comparator.nullsLast(Comparator.naturalOrder()));
            case "eventid" -> Comparator.comparing(ProgrammeRecommender::getEventId);
            default -> Comparator.comparing(ProgrammeRecommender::getDebut, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public ProgrammeRecommender update(ProgrammeRecommender programme) {
        if (!validerProgramme(programme)) {
            throw new IllegalArgumentException("Programme invalide");
        }

        String sql = "UPDATE programme SET event_id = ?, titre = ?, debut = ?, fin = ? WHERE id_prog = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, programme.getEventId());
            pstmt.setString(2, programme.getTitre());
            pstmt.setTimestamp(3, Timestamp.valueOf(programme.getDebut()));
            pstmt.setTimestamp(4, Timestamp.valueOf(programme.getFin()));
            pstmt.setLong(5, programme.getIdProg());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Mise à jour de programme échouée, aucune ligne affectée.");
            }

            logger.info("Programme mis à jour avec succès: ID {}", programme.getIdProg());
            return programme;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du programme {}: {}", programme.getIdProg(), e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le programme", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (!peutEtreSupprime(id)) {
            throw new IllegalArgumentException("Le programme ne peut pas être supprimé");
        }

        String sql = "DELETE FROM programme WHERE id_prog = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Programme supprimé avec succès: ID {}", id);
            }
            
            return deleted;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du programme {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<ProgrammeRecommender> findByEventId(Long eventId) {
        return findAll().stream()
                .filter(p -> p.getEventId().equals(eventId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findByTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String titreLower = titre.toLowerCase();
        return findAll().stream()
                .filter(p -> p.getTitre() != null && p.getTitre().toLowerCase().contains(titreLower))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findProgrammesEnCours() {
        return findAll().stream()
                .filter(ProgrammeRecommender::estEnCours)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findProgrammesTermines() {
        return findAll().stream()
                .filter(ProgrammeRecommender::estTermine)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findProgrammesAVenir() {
        return findAll().stream()
                .filter(ProgrammeRecommender::estAVenir)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findByDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return findAll().stream()
                .filter(p -> p.getDebut() != null && p.getFin() != null)
                .filter(p -> !p.getDebut().isBefore(debut) && !p.getFin().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findByDateDebutAfter(LocalDateTime date) {
        return findAll().stream()
                .filter(p -> p.getDebut() != null && p.getDebut().isAfter(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> findByDateFinBefore(LocalDateTime date) {
        return findAll().stream()
                .filter(p -> p.getFin() != null && p.getFin().isBefore(date))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerProgramme(ProgrammeRecommender programme) {
        if (programme == null) return false;
        if (programme.getEventId() == null || programme.getEventId() <= 0) return false;
        if (programme.getTitre() == null || programme.getTitre().trim().isEmpty()) return false;
        if (programme.getDebut() == null) return false;
        if (programme.getFin() == null) return false;
        if (programme.getFin().isBefore(programme.getDebut())) return false;
        
        return true;
    }

    @Override
    public boolean peutEtreSupprime(Long id) {
        return true; // Un programme peut toujours être supprimé
    }

    // Méthodes utilitaires privées
    private ProgrammeRecommender mapResultSetToProgramme(ResultSet rs) throws SQLException {
        ProgrammeRecommender programme = new ProgrammeRecommender();
        programme.setIdProg(rs.getLong("id_prog"));
        programme.setEventId(rs.getLong("event_id"));
        programme.setTitre(rs.getString("titre"));
        
        Timestamp debut = rs.getTimestamp("debut");
        if (debut != null) {
            programme.setDebut(debut.toLocalDateTime());
        }
        
        Timestamp fin = rs.getTimestamp("fin");
        if (fin != null) {
            programme.setFin(fin.toLocalDateTime());
        }
        
        return programme;
    }
}*/


package com.gestion.services;

import com.gestion.entities.ProgrammeRecommender;
import com.gestion.interfaces.ProgrammeService;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ProgrammerecomanderServiceImpl implements ProgrammeService {

    // Simulation DB (remplace JDBC pour simplifier)
    private final List<ProgrammeRecommender> storage = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public ProgrammeRecommender create(ProgrammeRecommender reco) {
        reco.setId(idGen.getAndIncrement());
        storage.add(reco);
        return reco;
    }

    @Override
    public Optional<ProgrammeRecommender> findById(Long id) {
        return storage.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<ProgrammeRecommender> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public List<ProgrammeRecommender> findByUser(Long userId) {
        return storage.stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRecommender> topRecommandations(Long userId, int limite) {
        return storage.stream()
                .filter(r -> r.getUserId().equals(userId))
                .filter(ProgrammeRecommender::estValide)
                .sorted(Comparator.comparingDouble(ProgrammeRecommender::getScore).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    @Override
    public ProgrammeRecommender update(ProgrammeRecommender reco) {
        delete(reco.getId());
        storage.add(reco);
        return reco;
    }

    @Override
    public boolean delete(Long id) {
        return storage.removeIf(r -> r.getId().equals(id));
    }

    @Override
    public void marquerCommeUtilisee(Long id) {
        findById(id).ifPresent(r -> r.setUtilise(true));
    }
}


