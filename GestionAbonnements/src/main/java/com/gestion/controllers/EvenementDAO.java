package com.gestion.controllers;

import com.gestion.tools.MyConnection;
import com.gestion.entities.Evenement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementDAO {

    // ===================== CREATE =====================
    public int add(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenement " +
                "(titre, description, type, date_debut, date_fin, lieu) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getType());
            ps.setTimestamp(4, Timestamp.valueOf(e.getDateDebut()));

            // date_fin peut être NULL (SOIREE / RANDONNEE)
            if (e.getDateFin() == null) {
                ps.setNull(5, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(5, Timestamp.valueOf(e.getDateFin()));
            }

            ps.setString(6, e.getLieu());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    e.setIdEvent(id);
                    return id;
                }
            }
        }
        return -1;
    }

    // ===================== READ BY ID =====================
    public Evenement findById(int id) throws SQLException {
        String sql = "SELECT * FROM evenement WHERE id_event = ?";

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return mapResultSet(rs);
            }
        }
    }

    // ===================== READ ALL =====================
    public List<Evenement> findAll() throws SQLException {
        String sql = "SELECT * FROM evenement ORDER BY date_debut DESC";
        List<Evenement> list = new ArrayList<>();

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    // ===================== UPDATE =====================
    public boolean update(Evenement e) throws SQLException {
        String sql = "UPDATE evenement SET " +
                "titre=?, description=?, type=?, date_debut=?, date_fin=?, lieu=? " +
                "WHERE id_event=?";

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getType());
            ps.setTimestamp(4, Timestamp.valueOf(e.getDateDebut()));

            if (e.getDateFin() == null) {
                ps.setNull(5, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(5, Timestamp.valueOf(e.getDateFin()));
            }

            ps.setString(6, e.getLieu());
            ps.setInt(7, e.getIdEvent());

            return ps.executeUpdate() > 0;
        }
    }

    // ===================== DELETE =====================
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM evenement WHERE id_event=?";

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ===================== MAPPER =====================
    private Evenement mapResultSet(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setIdEvent(rs.getInt("id_event"));
        e.setTitre(rs.getString("titre"));
        e.setDescription(rs.getString("description"));
        e.setType(rs.getString("type"));

        Timestamp td = rs.getTimestamp("date_debut");
        Timestamp tf = rs.getTimestamp("date_fin");

        e.setDateDebut(td != null ? td.toLocalDateTime() : null);
        e.setDateFin(tf != null ? tf.toLocalDateTime() : null);

        e.setLieu(rs.getString("lieu"));
        return e;
    }
}
