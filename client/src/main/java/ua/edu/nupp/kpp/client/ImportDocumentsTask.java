package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фоновий імпорт документів із CSV-файлу.
 *
 * Метод call() виконується у фоновому потоці й не змінює елементи інтерфейсу:
 * прогрес і текст стану передаються через updateProgress() та updateMessage().
 * Task лише читає файл і надсилає кожен рядок через HttpApprovalClient;
 * INSERT у MySQL виконує сервер (Servlet → Service → DAO).
 *
 * Політика помилок:
 * 1) файл спочатку розбирається повністю; якщо будь-який рядок має технічну
 *    помилку (кількість стовпців, формат числа), імпорт завершується помилкою
 *    ще до першого HTTP-запиту, тож у базі не залишається частково імпортованих даних;
 * 2) помилку сервера (предметна валідація, недоступність) зупиняє імпорт; у тексті
 *    помилки вказано номер рядка файлу та скільки записів уже передано;
 * 3) скасування перевіряється між рядками й завершує Task станом CANCELLED.
 */
public class ImportDocumentsTask extends Task<Integer> {

    private static final Logger LOG = Logger.getLogger(ImportDocumentsTask.class.getName());

    private final Path file;
    private final HttpApprovalClient api;

    public ImportDocumentsTask(Path file, HttpApprovalClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("Читання файлу " + file + "…");
        updateProgress(0, 1);
        LOG.info("Початок імпорту CSV: " + file);

        List<NumberedDocument> rows = readRows();
        int total = rows.size();
        if (total == 0) {
            throw new IllegalArgumentException("У CSV-файлі немає рядків даних");
        }

        int sent = 0;
        for (NumberedDocument row : rows) {
            if (isCancelled()) {
                LOG.warning("Імпорт скасовано; передано записів: " + sent);
                updateMessage("Імпорт скасовано. Передано записів: " + sent);
                return sent;
            }
            try {
                api.addDocument(row.document());
            } catch (ApiException e) {
                LOG.log(Level.WARNING, "Помилка імпорту рядка " + row.lineNumber(), e);
                throw new ApiException("Рядок " + row.lineNumber() + ": " + e.getMessage()
                        + " (уже передано записів: " + sent + ")", e);
            }
            sent++;
            updateProgress(sent, total);
            updateMessage("Передано " + sent + " з " + total);
        }

        LOG.info("Імпорт CSV завершено; передано записів: " + sent);
        updateMessage("Імпорт завершено. Передано записів: " + sent);
        return sent;
    }

    /** Читає файл, пропускає заголовок і порожні рядки, розбирає всі рядки даних. */
    private List<NumberedDocument> readRows() throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<NumberedDocument> result = new ArrayList<>();
        boolean headerSkipped = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }
            try {
                result.add(new NumberedDocument(i + 1, DocumentCsvReader.parseLine(line)));
            } catch (IllegalArgumentException e) {
                LOG.log(Level.WARNING, "Некоректний рядок CSV " + (i + 1), e);
                throw new IllegalArgumentException("Рядок " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return result;
    }

    private record NumberedDocument(int lineNumber, Document document) {
    }
}
