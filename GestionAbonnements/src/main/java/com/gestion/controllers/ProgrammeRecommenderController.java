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

/*package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeRecommenderController {

    private static Connection getConnection() throws SQLException {
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
    public List<ProgrammeRecommender> findAll() {
        List<ProgrammeRecommender> list = new ArrayList<>();
        String sql = "SELECT * FROM programme_recommande ORDER BY heure_debut";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération tous programmes", e);
        }
        return list;
    }

    public static List<ProgrammeRecommender> findByParticipation(Long participationId) {

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
}*/


/*package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;
import com.gestion.services.ProgrammeRecommenderService;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeRecommenderController {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gestion_evenements",
                "root",
                ""
        );
    }

    public void save(ProgrammeRecommender p) {
        String sql = """
            INSERT INTO programme_recommande
            (participation_id, activite, heure_debut, heure_fin, ambiance, justification, recommande)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

    public List<ProgrammeRecommender> findAll() {
        List<ProgrammeRecommender> list = new ArrayList<>();
        String sql = "SELECT * FROM programme_recommande ORDER BY heure_debut ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération de tous les programmes", e);
        }
        return list;
    }

    public List<ProgrammeRecommender> findByParticipation(Long participationId) {
        List<ProgrammeRecommender> list = new ArrayList<>();

        String sql = """
            SELECT * FROM programme_recommande
            WHERE participation_id = ?
            ORDER BY heure_debut ASC
        """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

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
            throw new RuntimeException("Erreur récupération des programmes pour participation " + participationId, e);
        }
        return list;
    }

    /**
     * Supprime tous les programmes recommandés liés à une participation donnée
     */
    /*public void deleteByParticipation(Long participationId) {
        String sql = "DELETE FROM programme_recommande WHERE participation_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, participationId);
            int rows = ps.executeUpdate();
            System.out.println("→ " + rows + " programme(s) supprimé(s) pour participation " + participationId);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des programmes pour participation " + participationId, e);
        }
    }

    private static final ProgrammeRecommenderService programmeRecommenderService = new ProgrammeRecommenderService();
    private static final ProgrammeRecommenderController programmeRecommenderController = new ProgrammeRecommenderController();
}*/


package com.gestion.controllers;

import com.gestion.entities.ProgrammeRecommender;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour la gestion des programmes recommandés en base de données.
 * Fournit les opérations CRUD de base.
 */
public class ProgrammeRecommenderController {

    private static final String URL = "jdbc:mysql://localhost:3306/lamma_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Sauvegarde un nouveau programme recommandé en base de données.
     * Récupère et assigne l'ID généré automatiquement.
     */
    public void save(ProgrammeRecommender p) {
        if (p == null) {
            throw new IllegalArgumentException("ProgrammeRecommender ne peut pas être null");
        }

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

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    p.setId(generatedKeys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la sauvegarde du programme recommandé pour participation " +
                            p.getParticipationId(), e
            );
        }
    }

    /**
     * Récupère tous les programmes recommandés de la base de données.
     */
    public List<ProgrammeRecommender> findAll() {
        List<ProgrammeRecommender> list = new ArrayList<>();
        String sql = "SELECT * FROM programme_recommande ORDER BY heure_debut ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les programmes recommandés", e);
        }
        return list;
    }

    /**
     * Récupère tous les programmes recommandés associés à une participation donnée.
     */
    public List<ProgrammeRecommender> findByParticipation(Long participation_id) {
        System.out.println("→ Début recherche pour participation_id = " + participation_id);

        List<ProgrammeRecommender> list = new ArrayList<>();
        String sql = """
        SELECT * FROM programme_recommande
        WHERE participation_id = ?
        ORDER BY heure_debut
    """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, participation_id);

            System.out.println("→ Requête préparée avec succès");

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("→ Requête exécutée, lecture des résultats...");
                while (rs.next()) {
                    // ...
                }
            }
        } catch (SQLException e) {
            System.out.println("→ EXCEPTION SQL : " + e.getMessage());
            System.out.println("→ Code erreur SQL : " + e.getErrorCode());
            System.out.println("→ État SQL : " + e.getSQLState());
            e.printStackTrace();
            throw new RuntimeException("Erreur SQL détaillée ci-dessus", e);
        }

        System.out.println("→ Fin recherche - " + list.size() + " programme(s) trouvé(s)");
        return list;
    }

    /**
     * Supprime tous les programmes recommandés liés à une participation donnée.
     */
    public void deleteByParticipation(Long participationId) {
        if (participationId == null || participationId <= 0) {
            throw new IllegalArgumentException("participationId doit être positif");
        }

        String sql = "DELETE FROM programme_recommande WHERE participation_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, participationId);
            int rowsAffected = ps.executeUpdate();

            System.out.println("→ " + rowsAffected + " programme(s) supprimé(s) pour participation " + participationId);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la suppression des programmes pour participation " + participationId, e
            );
        }
    }

    /**
     * Méthode utilitaire pour mapper un ResultSet vers un ProgrammeRecommender
     */
    private ProgrammeRecommender mapFromResultSet(ResultSet rs) throws SQLException {
        ProgrammeRecommender p = new ProgrammeRecommender();
        p.setId(rs.getLong("id"));
        p.setParticipationId(rs.getLong("participation_id"));
        p.setActivite(rs.getString("activite"));
        p.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        p.setHeureFin(rs.getTime("heure_fin").toLocalTime());
        p.setAmbiance(ProgrammeRecommender.Ambiance.valueOf(rs.getString("ambiance")));
        p.setJustification(rs.getString("justification"));
        p.setRecommande(rs.getBoolean("recommande"));
        return p;
    }
}