package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка для рецензентів: валідація, виклик DAO, логування.
 */
public class ReviewerService {

    private static final Logger LOG = Logger.getLogger(ReviewerService.class.getName());

    private final ReviewerDAO dao = new ReviewerDAO();

    public List<Reviewer> getAll() throws SQLException {
        return dao.getAll();
    }

    public Reviewer getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Reviewer r) throws SQLException {
        validate(r);
        dao.add(r);
        LOG.info("Додано рецензента");
    }

    public void update(Reviewer r) throws SQLException {
        if (r == null || r.getId() == null) {
            throw new IllegalArgumentException("Не вказано рецензента для редагування");
        }
        validate(r);
        dao.update(r);
        LOG.info("Оновлено рецензента");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено рецензента");
    }

    public static void validate(Reviewer r) {
        if (r == null || blank(r.getFullName()) || blank(r.getJobTitle())
                || blank(r.getEmail())) {
            throw new IllegalArgumentException("Усі поля рецензента є обов'язковими");
        }
        if (r.getFullName().length() > 160 || r.getJobTitle().length() > 160
                || r.getEmail().length() > 120) {
            throw new IllegalArgumentException("Занадто довгий текст у полях рецензента");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
