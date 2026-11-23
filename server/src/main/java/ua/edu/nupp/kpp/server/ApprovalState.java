package ua.edu.nupp.kpp.server;

/**
 * Стани життєвого циклу запиту на погодження:
 * ЧЕРНЕТКА → ПОДАНО → РОЗГЛЯД → СХВАЛЕНО.
 */
public enum ApprovalState {
    DRAFT,
    SUBMITTED,
    REVIEW,
    APPROVED
}
