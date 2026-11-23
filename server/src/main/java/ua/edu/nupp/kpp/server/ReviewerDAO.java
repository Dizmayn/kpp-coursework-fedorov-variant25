package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці reviewer. Містить лише SQL/JDBC, без предметної логіки.
 */
public class ReviewerDAO {

    public void add(Reviewer r) throws SQLException {
        String sql = "INSERT INTO reviewer(full_name,job_title,email) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.executeUpdate();
        }
    }

    public List<Reviewer> getAll() throws SQLException {
        String sql = "SELECT id,full_name,job_title,email FROM reviewer ORDER BY id";
        List<Reviewer> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Reviewer getById(long id) throws SQLException {
        String sql = "SELECT id,full_name,job_title,email FROM reviewer WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Reviewer r) throws SQLException {
        String sql = "UPDATE reviewer SET full_name=?,job_title=?,email=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.setLong(4, r.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM reviewer WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private static void bind(PreparedStatement p, Reviewer r) throws SQLException {
        p.setString(1, r.getFullName());
        p.setString(2, r.getJobTitle());
        p.setString(3, r.getEmail());
    }

    private static Reviewer map(ResultSet rs) throws SQLException {
        return new Reviewer(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("job_title"),
                rs.getString("email"));
    }
}
