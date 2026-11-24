package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP/JSON-інтерфейс запитів на погодження (/requests).
 * GET — список; POST action=add|update|delete|transition.
 *
 * Показники F1, F2, F3 та інтегральний показник I клієнт не передає:
 * їх розраховує ApprovalRequestService. Допустимість переходу стану
 * (action=transition) також перевіряє Service, а не Servlet.
 */
public class ApprovalRequestServlet extends HttpServlet {

    private final ApprovalRequestService service = new ApprovalRequestService();
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
                case "add" -> service.add(
                        HttpJson.longParam(req, "documentId"),
                        HttpJson.longParam(req, "reviewerId"));
                case "update" -> service.update(
                        HttpJson.longParam(req, "id"),
                        HttpJson.longParam(req, "documentId"),
                        HttpJson.longParam(req, "reviewerId"),
                        HttpJson.textOrNull(req, "reviewComment"));
                case "delete" -> service.delete(HttpJson.longParam(req, "id"));
                case "transition" -> service.changeState(
                        HttpJson.longParam(req, "id"),
                        HttpJson.stateParam(req, "state"));
                default -> throw HttpJson.unknownAction(action);
            }
            HttpJson.writeOk(resp, mapper);
        } catch (Exception e) {
            HttpJson.handleException(resp, mapper, e);
        }
    }
}
