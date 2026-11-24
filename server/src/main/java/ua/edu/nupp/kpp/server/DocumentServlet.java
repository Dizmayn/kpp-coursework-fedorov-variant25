package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP/JSON-інтерфейс документів (/documents).
 * GET — список; POST action=add|update|delete.
 * Дата строку передається у форматі РРРР-ММ-ДД.
 */
public class DocumentServlet extends HttpServlet {

    private final DocumentService service = new DocumentService();
    private final ObjectMapper mapper = HttpJson.createMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpJson.writeJson(resp, mapper, HttpServletResponse.SC_OK, service.getAll());
        } catch (Exception e) {
            HttpJson.handleException(resp, mapper, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpJson.prepareRequest(req);
        try {
            String action = HttpJson.action(req);
            switch (action) {
                case "add" -> service.add(readDocument(req, null));
                case "update" -> service.update(readDocument(req, HttpJson.longParam(req, "id")));
                case "delete" -> service.delete(HttpJson.longParam(req, "id"));
                default -> throw HttpJson.unknownAction(action);
            }
            HttpJson.writeOk(resp, mapper);
        } catch (Exception e) {
            HttpJson.handleException(resp, mapper, e);
        }
    }

    private static Document readDocument(HttpServletRequest req, Long id) {
        return new Document(id,
                HttpJson.longParam(req, "authorId"),
                HttpJson.text(req, "title"),
                HttpJson.text(req, "docType"),
                HttpJson.dateParam(req, "deadlineDate"),
                HttpJson.intParam(req, "criticality"),
                HttpJson.intParam(req, "dependentProcesses"));
    }
}
