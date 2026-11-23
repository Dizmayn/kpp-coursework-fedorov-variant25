package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка для документів: валідація, перевірка існування автора,
 * виклик DAO, логування.
 */
public class DocumentService {

    private static final Logger LOG = Logger.getLogger(DocumentService.class.getName());

    private final DocumentDAO dao = new DocumentDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();

    public List<Document> getAll() throws SQLException {
        return dao.getAll();
    }

    public Document getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Document d) throws SQLException {
        validate(d);
        requireAuthor(d.getAuthorId());
        dao.add(d);
        LOG.info("Додано документ");
    }

    public void update(Document d) throws SQLException {
        if (d == null || d.getId() == null) {
            throw new IllegalArgumentException("Не вказано документ для редагування");
        }
        validate(d);
        requireAuthor(d.getAuthorId());
        dao.update(d);
        LOG.info("Оновлено документ");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено документ");
    }

    public static void validate(Document d) {
        if (d == null || d.getAuthorId() <= 0 || blank(d.getTitle())
                || blank(d.getDocType()) || d.getDeadlineDate() == null) {
            throw new IllegalArgumentException("Некоректні основні дані документа");
        }
        if (d.getTitle().length() > 255 || d.getDocType().length() > 60) {
            throw new IllegalArgumentException("Занадто довгий текст у полях документа");
        }
        if (d.getCriticality() < 0 || d.getCriticality() > 100) {
            throw new IllegalArgumentException("Критичність має бути в діапазоні 0..100");
        }
        if (d.getDependentProcesses() < 0) {
            throw new IllegalArgumentException(
                    "Кількість залежних процесів не може бути від'ємною");
        }
    }

    private void requireAuthor(long authorId) throws SQLException {
        if (authorDAO.getById(authorId) == null) {
            throw new IllegalArgumentException("Автора не знайдено");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
