package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці document. Містить лише SQL/JDBC, без предметної логіки.
 */
public class DocumentDAO {

    private static final String COLUMNS =
            "id,author_id,title,doc_type,deadline_date,criticality,dependent_processes";

    public void add(Document d) throws SQLException {
        String sql = "INSERT INTO document"
                + "(author_id,title,doc_type,deadline_date,criticality,dependent_processes) "
                + "VALUES(?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, d);
            p.executeUpdate();
        }
    }

    public List<Document> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM document ORDER BY id";
        List<Document> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Document getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM document WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Document d) throws SQLException {
        String sql = "UPDATE document SET author_id=?,title=?,doc_type=?,"
                + "deadline_date=?,criticality=?,dependent_processes=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, d);
            p.setLong(7, d.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM document WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private static void bind(PreparedStatement p, Document d) throws SQLException {
        p.setLong(1, d.getAuthorId());
        p.setString(2, d.getTitle());
        p.setString(3, d.getDocType());
        p.setDate(4, Date.valueOf(d.getDeadlineDate()));
        p.setInt(5, d.getCriticality());
        p.setInt(6, d.getDependentProcesses());
    }

    private static Document map(ResultSet rs) throws SQLException {
        return new Document(
                rs.getLong("id"),
                rs.getLong("author_id"),
                rs.getString("title"),
                rs.getString("doc_type"),
                rs.getDate("deadline_date").toLocalDate(),
                rs.getInt("criticality"),
                rs.getInt("dependent_processes"));
    }
}
