package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Допоміжні методи HTTP/JSON-рівня, спільні для всіх Servlet:
 * формування JSON-відповідей, JSON-помилок та технічне перетворення
 * параметрів запиту (String → long/int/LocalDate/enum).
 *
 * Предметні правила (діапазони, обов'язкові поля, допустимі переходи
 * станів, існування пов'язаних записів) тут не перевіряються: їх
 * виконує Service.
 */
final class HttpJson {

    private static final Logger LOG = Logger.getLogger(HttpJson.class.getName());

    private HttpJson() {
    }

    /** ObjectMapper; LocalDate передається як текст у форматі РРРР-ММ-ДД. */
    static ObjectMapper createMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, ToStringSerializer.instance);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    static void prepareRequest(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
    }

    static void writeJson(HttpServletResponse resp, ObjectMapper mapper,
                          int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapper.writeValueAsString(body));
    }

    static void writeOk(HttpServletResponse resp, ObjectMapper mapper) throws IOException {
        writeJson(resp, mapper, HttpServletResponse.SC_OK, Map.of("status", "ok"));
    }

    static void writeError(HttpServletResponse resp, ObjectMapper mapper,
                           int status, String message) throws IOException {
        writeJson(resp, mapper, status, Map.of("error", message));
    }

    /**
     * IllegalArgumentException → 400 (помилка даних або предметного правила);
     * порушення обмежень БД → 400; решта → 500 без stack trace у відповіді.
     */
    static void handleException(HttpServletResponse resp, ObjectMapper mapper,
                                Exception e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            LOG.log(Level.WARNING, "Некоректний запит: {0}", e.getMessage());
            String message = e.getMessage() != null ? e.getMessage() : "Некоректний запит";
            writeError(resp, mapper, HttpServletResponse.SC_BAD_REQUEST, message);
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            LOG.log(Level.WARNING, "Порушення обмеження БД: {0}", e.getMessage());
            writeError(resp, mapper, HttpServletResponse.SC_BAD_REQUEST,
                    "Операцію неможливо виконати: запис пов'язаний з іншими даними "
                            + "або порушує обмеження бази даних");
        } else {
            LOG.log(Level.SEVERE, "Внутрішня помилка сервера", e);
            writeError(resp, mapper, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Внутрішня помилка сервера");
        }
    }

    /** Значення параметра або null, якщо його немає в запиті. */
    static String text(HttpServletRequest req, String name) {
        return req.getParameter(name);
    }

    /** Порожній рядок вважається відсутнім значенням (null). */
    static String textOrNull(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null || value.isBlank() ? null : value;
    }

    static long longParam(HttpServletRequest req, String name) {
        String raw = required(req, name);
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр «" + name + "» має бути цілим числом");
        }
    }

    static int intParam(HttpServletRequest req, String name) {
        String raw = required(req, name);
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр «" + name + "» має бути цілим числом");
        }
    }

    static LocalDate dateParam(HttpServletRequest req, String name) {
        String raw = required(req, name);
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Параметр «" + name + "» має формат РРРР-ММ-ДД");
        }
    }

    static ApprovalState stateParam(HttpServletRequest req, String name) {
        String raw = required(req, name);
        try {
            return ApprovalState.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан запиту: " + raw);
        }
    }

    static String action(HttpServletRequest req) {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Не вказано параметр action");
        }
        return action.trim();
    }

    static IllegalArgumentException unknownAction(String action) {
        return new IllegalArgumentException("Невідома дія: " + action);
    }

    private static String required(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Не вказано параметр «" + name + "»");
        }
        return raw;
    }
}
