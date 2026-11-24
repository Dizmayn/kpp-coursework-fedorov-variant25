package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP/JSON-інтерфейс авторів (/authors).
 * GET — список; POST action=add|update|delete.
 */
public class AuthorServlet extends HttpServlet {

    private final AuthorService service = new AuthorService();
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
                case "add" -> service.add(readAuthor(req, null));
                case "update" -> service.update(readAuthor(req, HttpJson.longParam(req, "id")));
                case "delete" -> service.delete(HttpJson.longParam(req, "id"));
                default -> throw HttpJson.unknownAction(action);
            }
            HttpJson.writeOk(resp, mapper);
        } catch (Exception e) {
            HttpJson.handleException(resp, mapper, e);
        }
    }

    private static Author readAuthor(HttpServletRequest req, Long id) {
        return new Author(id,
                HttpJson.text(req, "fullName"),
                HttpJson.text(req, "department"),
                HttpJson.text(req, "email"));
    }
}
