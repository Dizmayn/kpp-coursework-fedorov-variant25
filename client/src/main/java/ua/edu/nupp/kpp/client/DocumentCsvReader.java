package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Початковий послідовний сценарій CSV-імпорту (Етап III): читання файлу,
 * пропуск заголовка, розбір одного тестового рядка та створення клієнтської
 * моделі Document. Фоновий імпорт усього файлу (Task, ProgressBar) буде
 * реалізовано на наступному етапі.
 *
 * Формат (розділювач «;», є заголовок):
 * author_id;title;doc_type;deadline_date;criticality;dependent_processes
 *
 * Клас не звертається до бази даних: готова модель передається серверу
 * через HttpApprovalClient.
 */
public final class DocumentCsvReader {

    /** Відносний шлях: працює, якщо клієнт запущено з каталогу client. */
    public static final Path DEFAULT_PATH = Path.of("sample-data", "documents.csv");

    public static final String SEPARATOR = ";";
    public static final int COLUMNS = 6;

    private DocumentCsvReader() {
    }

    /**
     * Читає файл, пропускає заголовок і повертає перший рядок даних як Document.
     *
     * @throws IOException              файл недоступний
     * @throws IllegalArgumentException у файлі немає рядків даних або рядок некоректний
     */
    public static Document readFirstDocument(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        boolean headerSkipped = false;
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }
            return parseLine(line);
        }
        throw new IllegalArgumentException("У CSV-файлі немає рядків даних");
    }

    /**
     * Розбирає один рядок CSV. Якщо кількість стовпців або типи значень
     * некоректні, модель не створюється.
     * Предметні правила (діапазони, існування автора) перевіряє сервер.
     */
    public static Document parseLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Порожній рядок CSV");
        }
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != COLUMNS) {
            throw new IllegalArgumentException("Рядок CSV має містити " + COLUMNS
                    + " стовпців, а містить " + parts.length);
        }
        try {
            return new Document(
                    null,
                    Long.parseLong(parts[0].trim()),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    Integer.parseInt(parts[4].trim()),
                    Integer.parseInt(parts[5].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некоректне числове значення у рядку CSV: " + line, e);
        }
    }
}
