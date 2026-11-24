package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Графічний інтерфейс системи погодження внутрішніх документів.
 *
 * Усі дані читаються і змінюються лише через HttpApprovalClient
 * (HTTP/JSON). Клієнт не звертається до MySQL і не перевіряє предметні
 * правила: валідацію, розрахунок показників F1–F3 та I і життєвий цикл
 * запиту виконує сервер, а помилки показуються у вікні повідомлення.
 */
public class MainApp extends Application {

    private final HttpApprovalClient api = new HttpApprovalClient();

    private final TableView<Author> authorTable = new TableView<>();
    private final TableView<Reviewer> reviewerTable = new TableView<>();
    private final TableView<Document> documentTable = new TableView<>();
    private final TableView<ApprovalRequest> requestTable = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                tab("Автори", buildAuthorsTab()),
                tab("Рецензенти", buildReviewersTab()),
                tab("Документи", buildDocumentsTab()),
                tab("Запити на погодження", buildRequestsTab()));

        stage.setTitle("Погодження внутрішніх документів");
        stage.setScene(new Scene(tabs, 1150, 680));
        stage.show();

        safe(this::refreshAll);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ---------- Вкладка «Автори» ----------

    private Node buildAuthorsTab() {
        addColumn(authorTable, "ID", "id", 60);
        addColumn(authorTable, "ПІБ", "fullName", 280);
        addColumn(authorTable, "Підрозділ", "department", 280);
        addColumn(authorTable, "Email", "email", 260);

        TextField fullName = field("ПІБ");
        TextField department = field("Підрозділ");
        TextField email = field("Email");

        authorTable.getSelectionModel().selectedItemProperty().addListener((obs, old, a) -> {
            if (a != null) {
                fullName.setText(a.getFullName());
                department.setText(a.getDepartment());
                email.setText(a.getEmail());
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addAuthor(new Author(null, fullName.getText(), department.getText(), email.getText()));
            refreshAuthors();
        }));

        Button update = new Button("Змінити");
        update.setOnAction(e -> safe(() -> {
            Author selected = selected(authorTable);
            api.updateAuthor(new Author(selected.getId(),
                    fullName.getText(), department.getText(), email.getText()));
            refreshAuthors();
        }));

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteAuthor(selected(authorTable).getId());
            refreshAuthors();
        }));

        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshAuthors));

        return layout(authorTable,
                form(new String[]{"ПІБ", "Підрозділ", "Email"}, fullName, department, email),
                buttons(add, update, delete, refresh));
    }

    // ---------- Вкладка «Рецензенти» ----------

    private Node buildReviewersTab() {
        addColumn(reviewerTable, "ID", "id", 60);
        addColumn(reviewerTable, "ПІБ", "fullName", 280);
        addColumn(reviewerTable, "Посада", "jobTitle", 280);
        addColumn(reviewerTable, "Email", "email", 260);

        TextField fullName = field("ПІБ");
        TextField jobTitle = field("Посада");
        TextField email = field("Email");

        reviewerTable.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> {
            if (r != null) {
                fullName.setText(r.getFullName());
                jobTitle.setText(r.getJobTitle());
                email.setText(r.getEmail());
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addReviewer(new Reviewer(null, fullName.getText(), jobTitle.getText(), email.getText()));
            refreshReviewers();
        }));

        Button update = new Button("Змінити");
        update.setOnAction(e -> safe(() -> {
            Reviewer selected = selected(reviewerTable);
            api.updateReviewer(new Reviewer(selected.getId(),
                    fullName.getText(), jobTitle.getText(), email.getText()));
            refreshReviewers();
        }));

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteReviewer(selected(reviewerTable).getId());
            refreshReviewers();
        }));

        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshReviewers));

        return layout(reviewerTable,
                form(new String[]{"ПІБ", "Посада", "Email"}, fullName, jobTitle, email),
                buttons(add, update, delete, refresh));
    }

    // ---------- Вкладка «Документи» ----------

    private Node buildDocumentsTab() {
        addColumn(documentTable, "ID", "id", 60);
        addColumn(documentTable, "Автор (ID)", "authorId", 90);
        addColumn(documentTable, "Назва", "title", 360);
        addColumn(documentTable, "Тип", "docType", 140);
        addColumn(documentTable, "Строк", "deadlineDate", 110);
        addColumn(documentTable, "Критичність", "criticality", 100);
        addColumn(documentTable, "Залежні процеси", "dependentProcesses", 130);

        TextField authorId = field("ID автора");
        TextField title = field("Назва");
        TextField docType = field("Тип документа");
        TextField deadline = field("РРРР-ММ-ДД");
        TextField criticality = field("0..100");
        TextField processes = field("Кількість");

        documentTable.getSelectionModel().selectedItemProperty().addListener((obs, old, d) -> {
            if (d != null) {
                authorId.setText(String.valueOf(d.getAuthorId()));
                title.setText(d.getTitle());
                docType.setText(d.getDocType());
                deadline.setText(d.getDeadlineDate());
                criticality.setText(String.valueOf(d.getCriticality()));
                processes.setText(String.valueOf(d.getDependentProcesses()));
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addDocument(readDocument(null, authorId, title, docType, deadline, criticality, processes));
            refreshDocuments();
        }));

        Button update = new Button("Змінити");
        update.setOnAction(e -> safe(() -> {
            Document selected = selected(documentTable);
            api.updateDocument(readDocument(selected.getId(),
                    authorId, title, docType, deadline, criticality, processes));
            refreshDocuments();
        }));

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteDocument(selected(documentTable).getId());
            refreshDocuments();
        }));

        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshDocuments));

        // Фоновий CSV-імпорт через JavaFX Task (прогрес, статус, скасування).
        Button importCsv = new Button("Імпортувати CSV");
        Button cancelImport = new Button("Скасувати");
        cancelImport.setDisable(true);
        ProgressBar importProgress = new ProgressBar(0);
        importProgress.setPrefWidth(220);
        Label importStatus = new Label("Імпорт не запущено");
        importCsv.setOnAction(e ->
                startImport(importCsv, cancelImport, importProgress, importStatus));

        return layout(documentTable,
                form(new String[]{"ID автора", "Назва", "Тип документа", "Строк погодження",
                                "Критичність (0..100)", "Залежні процеси"},
                        authorId, title, docType, deadline, criticality, processes),
                new VBox(8, buttons(add, update, delete, refresh),
                        new HBox(10, importCsv, cancelImport, importProgress, importStatus)));
    }

    /**
     * Запускає ImportDocumentsTask у фоновому потоці. Властивості progress і message
     * Task прив'язуються до елементів інтерфейсу; після завершення прив'язка
     * скасовується, а кнопки повертаються у вихідний стан (також після помилки).
     */
    private void startImport(Button importCsv, Button cancelImport,
                             ProgressBar progressBar, Label statusLabel) {
        ImportDocumentsTask task =
                new ImportDocumentsTask(DocumentCsvReader.DEFAULT_PATH, api);

        importCsv.setDisable(true);
        cancelImport.setDisable(false);
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        cancelImport.setOnAction(e -> task.cancel());

        Runnable finish = () -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            importCsv.setDisable(false);
            cancelImport.setDisable(true);
        };

        task.setOnSucceeded(e -> {
            finish.run();
            progressBar.setProgress(1.0);
            statusLabel.setText("Імпорт завершено. Передано записів: " + task.getValue());
            safe(this::refreshDocuments);
            showInfo("Імпорт завершено. Передано записів: " + task.getValue());
        });
        task.setOnFailed(e -> {
            finish.run();
            progressBar.setProgress(0);
            Throwable error = task.getException();
            String text = error instanceof IOException
                    ? "Не вдалося прочитати файл " + DocumentCsvReader.DEFAULT_PATH
                            + ": " + error.getMessage()
                    : String.valueOf(error.getMessage());
            statusLabel.setText("Помилка імпорту");
            safe(this::refreshDocuments);
            showError(text);
        });
        task.setOnCancelled(e -> {
            finish.run();
            statusLabel.setText("Імпорт скасовано");
            safe(this::refreshDocuments);
        });

        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    // ---------- Вкладка «Запити на погодження» ----------

    private Node buildRequestsTab() {
        addColumn(requestTable, "ID", "id", 60);
        addColumn(requestTable, "Документ (ID)", "documentId", 110);
        addColumn(requestTable, "Рецензент (ID)", "reviewerId", 120);
        addColumn(requestTable, "Стан", "state", 110);
        addColumn(requestTable, "F1", "f1", 70);
        addColumn(requestTable, "F2", "f2", 70);
        addColumn(requestTable, "F3", "f3", 70);
        addColumn(requestTable, "Індекс I", "integralIndex", 90);
        addColumn(requestTable, "Коментар", "reviewComment", 300);

        TextField documentId = field("ID документа");
        TextField reviewerId = field("ID рецензента");
        TextField comment = field("Коментар рецензента");
        ComboBox<ApprovalState> stateBox = new ComboBox<>(
                FXCollections.observableArrayList(ApprovalState.values()));
        stateBox.setPromptText("Новий стан");

        requestTable.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> {
            if (r != null) {
                documentId.setText(String.valueOf(r.getDocumentId()));
                reviewerId.setText(String.valueOf(r.getReviewerId()));
                comment.setText(r.getReviewComment() == null ? "" : r.getReviewComment());
                stateBox.setValue(r.getState());
            }
        });

        // F1, F2, F3 та I користувач не вводить: їх розраховує сервер.
        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addRequest(parseLong(documentId, "ID документа"), parseLong(reviewerId, "ID рецензента"));
            refreshRequests();
        }));

        Button update = new Button("Змінити");
        update.setOnAction(e -> safe(() -> {
            ApprovalRequest selected = selected(requestTable);
            api.updateRequest(selected.getId(),
                    parseLong(documentId, "ID документа"),
                    parseLong(reviewerId, "ID рецензента"),
                    comment.getText());
            refreshRequests();
        }));

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteRequest(selected(requestTable).getId());
            refreshRequests();
        }));

        // Клієнт лише передає вибраний стан; допустимість переходу перевіряє сервер.
        Button changeState = new Button("Змінити стан");
        changeState.setOnAction(e -> safe(() -> {
            ApprovalRequest selected = selected(requestTable);
            ApprovalState state = stateBox.getValue();
            if (state == null) {
                throw new IllegalArgumentException("Виберіть стан");
            }
            api.changeRequestState(selected.getId(), state);
            refreshRequests();
        }));

        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshRequests));

        return layout(requestTable,
                form(new String[]{"ID документа", "ID рецензента", "Коментар", "Стан"},
                        documentId, reviewerId, comment, stateBox),
                buttons(add, update, delete, changeState, refresh));
    }

    // ---------- Оновлення даних із сервера ----------

    private void refreshAll() {
        refreshAuthors();
        refreshReviewers();
        refreshDocuments();
        refreshRequests();
    }

    private void refreshAuthors() {
        authorTable.setItems(FXCollections.observableArrayList(api.getAuthors()));
    }

    private void refreshReviewers() {
        reviewerTable.setItems(FXCollections.observableArrayList(api.getReviewers()));
    }

    private void refreshDocuments() {
        documentTable.setItems(FXCollections.observableArrayList(api.getDocuments()));
    }

    private void refreshRequests() {
        requestTable.setItems(FXCollections.observableArrayList(api.getRequests()));
    }

    // ---------- Допоміжні методи інтерфейсу ----------

    /** Помилки сервера та некоректне введення показуються у вікні, а не в консолі. */
    private void safe(Runnable action) {
        try {
            action.run();
        } catch (ApiException | IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Неочікувана помилка: " + e.getMessage());
        }
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                message == null ? "Невідома помилка" : message, ButtonType.OK);
        alert.setHeaderText("Помилка");
        alert.showAndWait();
    }

    private static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Готово");
        alert.showAndWait();
    }

    private static <T> T selected(TableView<T> table) {
        T item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException("Виберіть запис");
        }
        return item;
    }

    private static <T> void addColumn(TableView<T> table, String title, String property, double width) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        table.getColumns().add(column);
    }

    private static Tab tab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private static TextField field(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(240);
        return field;
    }

    private static GridPane form(String[] labels, Node... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        for (int i = 0; i < nodes.length; i++) {
            int column = (i % 3) * 2;
            int row = i / 3;
            grid.add(new Label(labels[i]), column, row);
            grid.add(nodes[i], column + 1, row);
        }
        return grid;
    }

    private static HBox buttons(Button... buttons) {
        return new HBox(10, buttons);
    }

    private static Node layout(Node table, Node form, Node buttons) {
        VBox bottom = new VBox(10, form, buttons);
        bottom.setPadding(new Insets(10));
        BorderPane pane = new BorderPane();
        pane.setCenter(table);
        pane.setBottom(bottom);
        return pane;
    }

    /** Технічне перетворення текстових полів; предметні правила перевіряє сервер. */
    private static Document readDocument(Long id, TextField authorId, TextField title, TextField docType,
                                         TextField deadline, TextField criticality, TextField processes) {
        return new Document(id,
                parseLong(authorId, "ID автора"),
                title.getText(),
                docType.getText(),
                deadline.getText(),
                parseInt(criticality, "Критичність"),
                parseInt(processes, "Кількість залежних процесів"));
    }

    private static long parseLong(TextField field, String label) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " має бути цілим числом");
        }
    }

    private static int parseInt(TextField field, String label) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " має бути цілим числом");
        }
    }
}
