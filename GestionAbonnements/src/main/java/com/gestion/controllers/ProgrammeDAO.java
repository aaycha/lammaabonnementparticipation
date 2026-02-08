package com.gestion.controllers;

import com.gestion.entities.Programme;
import com.gestion.tools.MyConnection;

import com.gestion.entities.Programme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeDAO {

    public int add(Programme p) throws SQLException {
        String sql = "INSERT INTO programme (event_id, titre, debut, fin) VALUES (?, ?, ?, ?)";
        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getEventId());
            ps.setString(2, p.getTitre());
            ps.setTimestamp(3, Timestamp.valueOf(p.getDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(p.getFin()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    p.setIdProg(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Programme findById(int idProg) throws SQLException {
        String sql = "SELECT * FROM programme WHERE id_prog = ?";
        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProg);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Programme p = new Programme();
                p.setIdProg(rs.getInt("id_prog"));
                p.setEventId(rs.getInt("event_id"));
                p.setTitre(rs.getString("titre"));

                Timestamp d = rs.getTimestamp("debut");
                Timestamp f = rs.getTimestamp("fin");
                p.setDebut(d != null ? d.toLocalDateTime() : null);
                p.setFin(f != null ? f.toLocalDateTime() : null);

                return p;
            }
        }
    }

    public List<Programme> findAll() throws SQLException {
        String sql = "SELECT * FROM programme ORDER BY id_prog DESC";
        List<Programme> list = new ArrayList<>();

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Programme p = new Programme();
                p.setIdProg(rs.getInt("id_prog"));
                p.setEventId(rs.getInt("event_id"));
                p.setTitre(rs.getString("titre"));

                Timestamp d = rs.getTimestamp("debut");
                Timestamp f = rs.getTimestamp("fin");
                p.setDebut(d != null ? d.toLocalDateTime() : null);
                p.setFin(f != null ? f.toLocalDateTime() : null);

                list.add(p);
            }
        }
        return list;
    }

    public List<Programme> findByEventId(int eventId) throws SQLException {
        String sql = "SELECT * FROM programme WHERE event_id=? ORDER BY debut ASC";
        List<Programme> list = new ArrayList<>();

        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Programme p = new Programme();
                    p.setIdProg(rs.getInt("id_prog"));
                    p.setEventId(rs.getInt("event_id"));
                    p.setTitre(rs.getString("titre"));

                    Timestamp d = rs.getTimestamp("debut");
                    Timestamp f = rs.getTimestamp("fin");
                    p.setDebut(d != null ? d.toLocalDateTime() : null);
                    p.setFin(f != null ? f.toLocalDateTime() : null);

                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean update(Programme p) throws SQLException {
        String sql = "UPDATE programme SET event_id=?, titre=?, debut=?, fin=? WHERE id_prog=?";
        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, p.getEventId());
            ps.setString(2, p.getTitre());
            ps.setTimestamp(3, Timestamp.valueOf(p.getDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(p.getFin()));
            ps.setInt(5, p.getIdProg());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idProg) throws SQLException {
        String sql = "DELETE FROM programme WHERE id_prog=?";
        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProg);
            return ps.executeUpdate() > 0;
        }
    }

    public int deleteByEventId(int eventId) throws SQLException {
        String sql = "DELETE FROM programme WHERE event_id=?";
        try (Connection cn = MyConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            return ps.executeUpdate(); // nb lignes supprimées
        }
    }
}
