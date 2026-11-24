package ua.edu.nupp.kpp.client;

/**
 * Клієнтська модель запиту на погодження. Показники F1, F2, F3 та
 * інтегральний показник розраховує сервер; клієнт лише відображає їх.
 */
public class ApprovalRequest {

    private Long id;
    private long documentId;
    private long reviewerId;
    private ApprovalState state;
    private String reviewComment;
    private double f1;
    private double f2;
    private double f3;
    private double integralIndex;

    public ApprovalRequest() {
    }

    public ApprovalRequest(Long id,
                           long documentId,
                           long reviewerId,
                           ApprovalState state,
                           String reviewComment,
                           double f1,
                           double f2,
                           double f3,
                           double integralIndex) {
        this.id = id;
        this.documentId = documentId;
        this.reviewerId = reviewerId;
        this.state = state;
        this.reviewComment = reviewComment;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.integralIndex = integralIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public ApprovalState getState() {
        return state;
    }

    public void setState(ApprovalState state) {
        this.state = state;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getIntegralIndex() {
        return integralIndex;
    }

    public void setIntegralIndex(double integralIndex) {
        this.integralIndex = integralIndex;
    }
}
