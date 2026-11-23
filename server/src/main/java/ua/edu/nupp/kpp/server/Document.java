package ua.edu.nupp.kpp.server;

import java.time.LocalDate;

/**
 * Внутрішній документ, який подається на погодження (таблиця document).
 * Поля criticality і dependentProcesses та строк deadlineDate є вихідними
 * даними для факторів F1–F3 інтегрального показника.
 */
public class Document {

    private Long id;
    private long authorId;
    private String title;
    private String docType;
    private LocalDate deadlineDate;
    private int criticality;
    private int dependentProcesses;

    public Document() {
    }

    public Document(Long id,
                    long authorId,
                    String title,
                    String docType,
                    LocalDate deadlineDate,
                    int criticality,
                    int dependentProcesses) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.docType = docType;
        this.deadlineDate = deadlineDate;
        this.criticality = criticality;
        this.dependentProcesses = dependentProcesses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDate deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public int getCriticality() {
        return criticality;
    }

    public void setCriticality(int criticality) {
        this.criticality = criticality;
    }

    public int getDependentProcesses() {
        return dependentProcesses;
    }

    public void setDependentProcesses(int dependentProcesses) {
        this.dependentProcesses = dependentProcesses;
    }
}
