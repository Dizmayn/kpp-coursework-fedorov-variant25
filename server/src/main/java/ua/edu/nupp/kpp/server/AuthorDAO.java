package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці author. Містить лише SQL/JDBC, без предметної логіки.
 */
public class AuthorDAO {

    public void add(Author a) throws SQLException {
        String sql = "INSERT INTO author(full_name,department,email) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, a);
            p.executeUpdate();
        }
    }

    public List<Author> getAll() throws SQLException {
        String sql = "SELECT id,full_name,department,email FROM author ORDER BY id";
        List<Author> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Author getById(long id) throws SQLException {
        String sql = "SELECT id,full_name,department,email FROM author WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Author a) throws SQLException {
        String sql = "UPDATE author SET full_name=?,department=?,email=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, a);
            p.setLong(4, a.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM author WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private static void bind(PreparedStatement p, Author a) throws SQLException {
        p.setString(1, a.getFullName());
        p.setString(2, a.getDepartment());
        p.setString(3, a.getEmail());
    }

    private static Author map(ResultSet rs) throws SQLException {
        return new Author(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("department"),
                rs.getString("email"));
    }
}
