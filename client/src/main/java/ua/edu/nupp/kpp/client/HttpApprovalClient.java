package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Єдина точка доступу JavaFX-клієнта до сервера через HTTP/JSON.
 * GET використовується лише для читання; додавання, редагування,
 * видалення та зміна стану виконуються через POST
 * (application/x-www-form-urlencoded) з параметром action.
 *
 * Клієнт не працює з базою даних і не перевіряє предметні правила:
 * валідацію, розрахунок показників і життєвий цикл виконує сервер.
 */
public class HttpApprovalClient {

    /** Має збігатися з finalName у server/pom.xml та context path у Tomcat. */
    public static final String DEFAULT_BASE = "http://localhost:8080/document-approval-server";

    private final String base;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public HttpApprovalClient() {
        this(DEFAULT_BASE);
    }

    public HttpApprovalClient(String base) {
        this.base = base;
    }

    // ---------- Автори ----------

    public List<Author> getAuthors() {
        return readList(get("/authors"), new TypeReference<List<Author>>() { });
    }

    public void addAuthor(Author a) {
        post("/authors", params("action", "add",
                "fullName", a.getFullName(), "department", a.getDepartment(), "email", a.getEmail()));
    }

    public void updateAuthor(Author a) {
        post("/authors", params("action", "update", "id", String.valueOf(a.getId()),
                "fullName", a.getFullName(), "department", a.getDepartment(), "email", a.getEmail()));
    }

    public void deleteAuthor(long id) {
        post("/authors", params("action", "delete", "id", String.valueOf(id)));
    }

    // ---------- Рецензенти ----------

    public List<Reviewer> getReviewers() {
        return readList(get("/reviewers"), new TypeReference<List<Reviewer>>() { });
    }

    public void addReviewer(Reviewer r) {
        post("/reviewers", params("action", "add",
                "fullName", r.getFullName(), "jobTitle", r.getJobTitle(), "email", r.getEmail()));
    }

    public void updateReviewer(Reviewer r) {
        post("/reviewers", params("action", "update", "id", String.valueOf(r.getId()),
                "fullName", r.getFullName(), "jobTitle", r.getJobTitle(), "email", r.getEmail()));
    }

    public void deleteReviewer(long id) {
        post("/reviewers", params("action", "delete", "id", String.valueOf(id)));
    }

    // ---------- Документи ----------

    public List<Document> getDocuments() {
        return readList(get("/documents"), new TypeReference<List<Document>>() { });
    }

    public void addDocument(Document d) {
        post("/documents", documentParams("add", d));
    }

    public void updateDocument(Document d) {
        Map<String, String> p = documentParams("update", d);
        p.put("id", String.valueOf(d.getId()));
        post("/documents", p);
    }

    public void deleteDocument(long id) {
        post("/documents", params("action", "delete", "id", String.valueOf(id)));
    }

    // ---------- Запити на погодження ----------

    public List<ApprovalRequest> getRequests() {
        return readList(get("/requests"), new TypeReference<List<ApprovalRequest>>() { });
    }

    /** F1–F3 та інтегральний показник сервер розраховує сам. */
    public void addRequest(long documentId, long reviewerId) {
        post("/requests", params("action", "add",
                "documentId", String.valueOf(documentId), "reviewerId", String.valueOf(reviewerId)));
    }

    public void updateRequest(long id, long documentId, long reviewerId, String reviewComment) {
        post("/requests", params("action", "update", "id", String.valueOf(id),
                "documentId", String.valueOf(documentId), "reviewerId", String.valueOf(reviewerId),
                "reviewComment", reviewComment));
    }

    public void deleteRequest(long id) {
        post("/requests", params("action", "delete", "id", String.valueOf(id)));
    }

    /** Клієнт лише передає вибраний стан; допустимість переходу перевіряє сервер. */
    public void changeRequestState(long id, ApprovalState state) {
        post("/requests", params("action", "transition", "id", String.valueOf(id),
                "state", state.name()));
    }

    // ---------- Допоміжні методи ----------

    private static Map<String, String> documentParams(String action, Document d) {
        return params("action", action,
                "authorId", String.valueOf(d.getAuthorId()),
                "title", d.getTitle(),
                "docType", d.getDocType(),
                "deadlineDate", d.getDeadlineDate(),
                "criticality", String.valueOf(d.getCriticality()),
                "dependentProcesses", String.valueOf(d.getDependentProcesses()));
    }

    /** Пари «ім'я, значення» у порядку передачі; null перетворюється на порожній рядок. */
    private static Map<String, String> params(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1] == null ? "" : keyValues[i + 1]);
        }
        return map;
    }

    private String get(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();
        return send(request);
    }

    private String post(String path, Map<String, String> params) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(encode(params), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    /** Усі текстові параметри кодуються (українські літери, пробіли, +, &). */
    private static String encode(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new ApiException(errorText(response.statusCode(), response.body()));
            }
            return response.body();
        } catch (IOException e) {
            throw new ApiException("Сервер недоступний. Перевірте, що Tomcat запущено "
                    + "і застосунок розгорнуто за адресою " + base, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запит до сервера було перервано", e);
        }
    }

    /** Текст помилки з JSON {"error": "..."} або загальне повідомлення з HTTP-кодом. */
    private String errorText(int status, String body) {
        try {
            JsonNode error = mapper.readTree(body).get("error");
            if (error != null && !error.asText().isBlank()) {
                return error.asText();
            }
        } catch (IOException | RuntimeException ignored) {
            // тіло відповіді не є JSON — використовуємо загальний текст
        }
        return "Сервер повернув помилку HTTP " + status;
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new ApiException("Сервер повернув некоректні дані (JSON)", e);
        }
    }
}
