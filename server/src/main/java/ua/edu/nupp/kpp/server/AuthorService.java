package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка для авторів: валідація, виклик DAO, логування.
 */
public class AuthorService {

    private static final Logger LOG = Logger.getLogger(AuthorService.class.getName());

    private final AuthorDAO dao = new AuthorDAO();

    public List<Author> getAll() throws SQLException {
        return dao.getAll();
    }

    public Author getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Author a) throws SQLException {
        validate(a);
        dao.add(a);
        LOG.info("Додано автора");
    }

    public void update(Author a) throws SQLException {
        if (a == null || a.getId() == null) {
            throw new IllegalArgumentException("Не вказано автора для редагування");
        }
        validate(a);
        dao.update(a);
        LOG.info("Оновлено автора");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено автора");
    }

    public static void validate(Author a) {
        if (a == null || blank(a.getFullName()) || blank(a.getDepartment())
                || blank(a.getEmail())) {
            throw new IllegalArgumentException("Усі поля автора є обов'язковими");
        }
        if (a.getFullName().length() > 160 || a.getDepartment().length() > 160
                || a.getEmail().length() > 120) {
            throw new IllegalArgumentException("Занадто довгий текст у полях автора");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
