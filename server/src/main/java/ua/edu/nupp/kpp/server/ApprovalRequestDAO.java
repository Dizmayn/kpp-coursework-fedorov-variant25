package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці approval_request. Стан зберігається як текст
 * (state.name()) і читається через ApprovalState.valueOf(...).
 * DAO не перевіряє допустимість переходів між станами — це робить Service.
 */
public class ApprovalRequestDAO {

    private static final String COLUMNS =
            "id,document_id,reviewer_id,state,review_comment,f1,f2,f3,integral_index";

    public void add(ApprovalRequest r) throws SQLException {
        String sql = "INSERT INTO approval_request"
                + "(document_id,reviewer_id,state,review_comment,f1,f2,f3,integral_index) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.executeUpdate();
        }
    }

    public List<ApprovalRequest> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM approval_request ORDER BY id";
        List<ApprovalRequest> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ApprovalRequest getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM approval_request WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(ApprovalRequest r) throws SQLException {
        String sql = "UPDATE approval_request SET document_id=?,reviewer_id=?,state=?,"
                + "review_comment=?,f1=?,f2=?,f3=?,integral_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.setLong(9, r.getId());
            p.executeUpdate();
        }
    }

    /**
     * Змінює лише поле state. Допустимість переходу тут не перевіряється.
     */
    public void updateState(long id, ApprovalState state) throws SQLException {
        String sql = "UPDATE approval_request SET state=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM approval_request WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private static void bind(PreparedStatement p, ApprovalRequest r) throws SQLException {
        p.setLong(1, r.getDocumentId());
        p.setLong(2, r.getReviewerId());
        p.setString(3, r.getState().name());
        if (r.getReviewComment() == null) {
            p.setNull(4, Types.VARCHAR);
        } else {
            p.setString(4, r.getReviewComment());
        }
        p.setDouble(5, r.getF1());
        p.setDouble(6, r.getF2());
        p.setDouble(7, r.getF3());
        p.setDouble(8, r.getIntegralIndex());
    }

    private static ApprovalRequest map(ResultSet rs) throws SQLException {
        return new ApprovalRequest(
                rs.getLong("id"),
                rs.getLong("document_id"),
                rs.getLong("reviewer_id"),
                ApprovalState.valueOf(rs.getString("state")),
                rs.getString("review_comment"),
                rs.getDouble("f1"),
                rs.getDouble("f2"),
                rs.getDouble("f3"),
                rs.getDouble("integral_index"));
    }
}
