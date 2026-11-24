package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationTest {

    private static Document document(long authorId, String title, int criticality, int processes) {
        return new Document(null, authorId, title, "Регламент",
                LocalDate.of(2026, 12, 1), criticality, processes);
    }

    @Test
    void validDocumentPasses() {
        assertDoesNotThrow(() -> DocumentService.validate(document(1, "Положення", 70, 4)));
    }

    @Test
    void documentWithoutTitleIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document(1, "  ", 70, 4)));
    }

    @Test
    void documentWithoutAuthorIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document(0, "Положення", 70, 4)));
    }

    @Test
    void documentCriticalityMustBeInRange() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document(1, "Положення", 101, 4)));
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document(1, "Положення", -1, 4)));
    }

    @Test
    void documentDependentProcessesCannotBeNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document(1, "Положення", 70, -1)));
    }

    @Test
    void documentTitleLongerThan255IsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document(1, "a".repeat(256), 70, 4)));
    }

    @Test
    void authorRequiresAllFields() {
        assertDoesNotThrow(() -> AuthorService.validate(
                new Author(null, "Іваненко І. І.", "Кафедра ІТ", "ivanenko@example.com")));
        assertThrows(IllegalArgumentException.class, () -> AuthorService.validate(
                new Author(null, "Іваненко І. І.", "", "ivanenko@example.com")));
        assertThrows(IllegalArgumentException.class, () -> AuthorService.validate(null));
    }

    @Test
    void reviewerRequiresAllFields() {
        assertDoesNotThrow(() -> ReviewerService.validate(
                new Reviewer(null, "Петренко П. П.", "Проректор", "petrenko@example.com")));
        assertThrows(IllegalArgumentException.class, () -> ReviewerService.validate(
                new Reviewer(null, "Петренко П. П.", "Проректор", " ")));
    }
}
