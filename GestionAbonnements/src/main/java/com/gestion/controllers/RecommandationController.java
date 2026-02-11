/*package com.gestion.controllers;

import com.gestion.criteria.RecommandationCriteria;
import com.gestion.entities.Recommandation;
import com.gestion.interfaces.RecommandationService;
import com.gestion.services.RecommandationServiceImpl;

import java.util.List;

/**
 * Contrôleur SCRUD pour les recommandations.
 * Create, Read, Update, Delete, Search, Tri.
 * API : GET /api/recommandations/user/:userId?contexte=couple (top 5 par Score DESC)
 */
/*public class RecommandationController {
    private final RecommandationService recommandationService = new RecommandationServiceImpl();

    public Recommandation create(Recommandation recommandation) {
        return recommandationService.create(recommandation);
    }

    public Recommandation getById(Long id) {
        return recommandationService.findById(id).orElse(null);
    }

    public List<Recommandation> getAll() {
        return recommandationService.findAll();
    }

    public List<Recommandation> getAll(String sortBy, String sortOrder) {
        return recommandationService.findAll(sortBy, sortOrder);
    }

    /** Top N par utilisateur, optionnellement filtré par contexte (couple, amis, famille). Score > 0.5 pour push. */
    /*public List<Recommandation> getTopByUser(Long userId, String contexte, int limite) {
        return recommandationService.findTopByUserId(userId, contexte, limite > 0 ? limite : 5);
    }

    public List<Recommandation> getByUserId(Long userId) {
        return recommandationService.findByUserId(userId);
    }

    /** Recherche avec critères (score min, algorithme, valides seulement, limite, tri) */
    /*public List<Recommandation> search(RecommandationCriteria criteria) {
        return recommandationService.search(criteria);
    }

    public List<Recommandation> search(Long userId, String contexte, Double scoreMin, String sortBy, String sortOrder, Integer limite) {
        RecommandationCriteria criteria = new RecommandationCriteria();
        criteria.setUserId(userId);
        criteria.setContexte(contexte);
        criteria.setScoreMinimum(scoreMin != null ? scoreMin : 0.5);
        criteria.setSortBy(sortBy);
        criteria.setSortOrder(sortOrder);
        criteria.setLimite(limite != null ? limite : 10);
        criteria.setValidesSeulement(true);
        return recommandationService.search(criteria);
    }

    public Recommandation update(Recommandation recommandation) {
        return recommandationService.update(recommandation);
    }

    /** Suppression si reco obsolète (ex: événement annulé). Clean-up périodique. */
   /* public boolean delete(Long id) {
        return recommandationService.delete(id);
    }
}*/


package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommandationController {

    private static final String URL = "jdbc:mysql://localhost:3306/gestion_evenements";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void save(ProgrammeRecommender p) {
        String sql = """
            INSERT INTO programme_recommande
            (participation_id, activite, heure_debut, heure_fin, ambiance, justification, recommande)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, p.getParticipationId());
            ps.setString(2, p.getActivite());
            ps.setTime(3, Time.valueOf(p.getHeureDebut()));
            ps.setTime(4, Time.valueOf(p.getHeureFin()));
            ps.setString(5, p.getAmbiance().name());
            ps.setString(6, p.getJustification());
            ps.setBoolean(7, p.isRecommande());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    p.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du programme recommandé", e);
        }
    }

    public List<ProgrammeRecommender> findByParticipation(Long participationId) {
        List<ProgrammeRecommender> list = new ArrayList<>();

        String sql = """
            SELECT * FROM programme_recommande
            WHERE participation_id = ?
            ORDER BY heure_debut ASC
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, participationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProgrammeRecommender p = new ProgrammeRecommender();
                    p.setId(rs.getLong("id"));
                    p.setParticipationId(rs.getLong("participation_id"));
                    p.setActivite(rs.getString("activite"));
                    p.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                    p.setHeureFin(rs.getTime("heure_fin").toLocalTime());
                    p.setAmbiance(ProgrammeRecommender.Ambiance.valueOf(rs.getString("ambiance")));
                    p.setJustification(rs.getString("justification"));
                    p.setRecommande(rs.getBoolean("recommande"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des programmes", e);
        }
        return list;
    }

    public void deleteByParticipation(Long participationId) {
        String sql = "DELETE FROM programme_recommande WHERE participation_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, participationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression programmes", e);
        }
    }
}
