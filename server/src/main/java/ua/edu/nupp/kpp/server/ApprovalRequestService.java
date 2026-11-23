package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка запиту на погодження: перевірка пов'язаних записів,
 * автоматичний розрахунок F1–F3 та інтегрального показника на основі
 * документа, чотиристанний життєвий цикл і логування.
 *
 * Користувач не вводить F1–F3 та інтегральний показник вручну: сервіс
 * бере документ і сам формує всі значення.
 */
public class ApprovalRequestService {

    private static final Logger LOG =
            Logger.getLogger(ApprovalRequestService.class.getName());

    private static final int MAX_COMMENT_LENGTH = 500;

    private final ApprovalRequestDAO dao = new ApprovalRequestDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final ReviewerDAO reviewerDAO = new ReviewerDAO();

    public List<ApprovalRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public ApprovalRequest getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Створює запит на погодження документа рецензентом. Новий запит завжди
     * отримує початковий стан DRAFT, а F1, F2, F3 та I розраховуються тут.
     */
    public void add(long documentId, long reviewerId) throws SQLException {
        Document document = requireDocument(documentId);
        requireReviewer(reviewerId);
        dao.add(fromDocument(null, document, reviewerId, ApprovalState.DRAFT, null));
        LOG.info("Створено запит на погодження");
    }

    /**
     * Редагує запит: змінює документ, рецензента та коментар. Показники
     * перераховуються, поточний стан не змінюється (стан змінює лише
     * {@link #changeState(long, ApprovalState)}).
     */
    public void update(long id, long documentId, long reviewerId, String reviewComment)
            throws SQLException {
        ApprovalRequest old = dao.getById(id);
        if (old == null) {
            throw new IllegalArgumentException("Запит на погодження не знайдено");
        }
        validateComment(reviewComment);
        Document document = requireDocument(documentId);
        requireReviewer(reviewerId);
        dao.update(fromDocument(id, document, reviewerId, old.getState(), reviewComment));
        LOG.info("Оновлено запит на погодження");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено запит на погодження");
    }

    /**
     * Змінює стан за життєвим циклом DRAFT → SUBMITTED → REVIEW → APPROVED.
     * Спочатку перевіряється допустимість переходу, і лише потім DAO
     * записує новий стан.
     */
    public void changeState(long id, ApprovalState next) throws SQLException {
        ApprovalRequest current = dao.getById(id);
        if (current == null) {
            throw new IllegalArgumentException("Запит на погодження не знайдено");
        }
        if (next == null || !isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану: " + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан: " + current.getState() + " -> " + next);
    }

    /**
     * Дозволені лише три послідовні переходи; пропуск проміжних станів,
     * повернення назад і переходи після кінцевого стану заборонені.
     */
    public static boolean isAllowedTransition(ApprovalState from, ApprovalState to) {
        return (from == ApprovalState.DRAFT && to == ApprovalState.SUBMITTED)
                || (from == ApprovalState.SUBMITTED && to == ApprovalState.REVIEW)
                || (from == ApprovalState.REVIEW && to == ApprovalState.APPROVED);
    }

    public static void validateComment(String comment) {
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Коментар не може перевищувати "
                    + MAX_COMMENT_LENGTH + " символів");
        }
    }

    private ApprovalRequest fromDocument(Long id,
                                         Document document,
                                         long reviewerId,
                                         ApprovalState state,
                                         String reviewComment) {
        double f1 = RiskCalculator.factor1(document.getDeadlineDate(), LocalDate.now());
        double f2 = RiskCalculator.factor2(document.getCriticality());
        double f3 = RiskCalculator.factor3(document.getDependentProcesses());
        double index = RiskCalculator.calculate(f1, f2, f3);
        return new ApprovalRequest(id, document.getId(), reviewerId, state,
                reviewComment, f1, f2, f3, index);
    }

    private Document requireDocument(long documentId) throws SQLException {
        Document document = documentDAO.getById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Документ не знайдено");
        }
        return document;
    }

    private void requireReviewer(long reviewerId) throws SQLException {
        if (reviewerDAO.getById(reviewerId) == null) {
            throw new IllegalArgumentException("Рецензента не знайдено");
        }
    }
}
