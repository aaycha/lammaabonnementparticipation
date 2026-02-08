/*package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;
import com.gestion.interfaces.ProgrammeService;
import com.gestion.services.ProgrammerecomanderServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur SCRUD pour les programmes.
 * Expose : Create, Read, Update, Delete, Search, Tri.
 */
/*public class ProgrammerecomanderController {
    private final ProgrammeService programmeService = new ProgrammerecomanderServiceImpl();

    public ProgrammeRecommender create(ProgrammeRecommender programme) {
        return programmeService.create(programme);
    }

    public ProgrammeRecommender getById(Long id) {
        return programmeService.findById(id).orElse(null);
    }

    public List<ProgrammeRecommender> getAll() {
        return programmeService.findAll();
    }

    public List<ProgrammeRecommender> getAll(String sortBy, String sortOrder) {
        return programmeService.findAll(sortBy, sortOrder);
    }

    public ProgrammeRecommender update(ProgrammeRecommender programme) {
        return programmeService.update(programme);
    }

    public boolean delete(Long id) {
        return programmeService.delete(id);
    }

    public List<ProgrammeRecommender> getByEventId(Long eventId) {
        return programmeService.findByEventId(eventId);
    }

    public List<ProgrammeRecommender> getByTitre(String titre) {
        return programmeService.findByTitre(titre);
    }

    public List<ProgrammeRecommender> getProgrammesEnCours() {
        return programmeService.findProgrammesEnCours();
    }

    public List<ProgrammeRecommender> getProgrammesTermines() {
        return programmeService.findProgrammesTermines();
    }

    public List<ProgrammeRecommender> getProgrammesAVenir() {
        return programmeService.findProgrammesAVenir();
    }

    public List<ProgrammeRecommender> getByDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return programmeService.findByDateBetween(debut, fin);
    }
}*/

package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeRecommenderController {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gestion_evenements",
                "root",
                ""
        );
    }

    public void save(ProgrammeRecommender p) {

        String sql = """
            INSERT INTO programme_recommande
            (participation_id, activite, heure_debut, heure_fin,
             ambiance, justification, recommande)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, p.getParticipationId());
            ps.setString(2, p.getActivite());
            ps.setTime(3, Time.valueOf(p.getHeureDebut()));
            ps.setTime(4, Time.valueOf(p.getHeureFin()));
            ps.setString(5, p.getAmbiance().name());
            ps.setString(6, p.getJustification());
            ps.setBoolean(7, p.isRecommande());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur sauvegarde programme recommandé", e);
        }
    }

    public List<ProgrammeRecommender> findByParticipation(Long participationId) {

        List<ProgrammeRecommender> list = new ArrayList<>();

        String sql = """
            SELECT * FROM programme_recommande
            WHERE participation_id = ?
            ORDER BY heure_debut
        """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, participationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProgrammeRecommender p = new ProgrammeRecommender(
                        rs.getLong("participation_id"),
                        rs.getString("activite"),
                        rs.getTime("heure_debut").toLocalTime(),
                        rs.getTime("heure_fin").toLocalTime(),
                        ProgrammeRecommender.Ambiance.valueOf(rs.getString("ambiance")),
                        rs.getString("justification")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération programme", e);
        }
        return list;
    }
}
