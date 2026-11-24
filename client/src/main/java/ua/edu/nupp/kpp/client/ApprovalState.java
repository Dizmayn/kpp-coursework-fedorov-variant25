package ua.edu.nupp.kpp.client;

/**
 * Стани запиту на погодження. Значення збігаються з серверним enum,
 * оскільки передаються через HTTP/JSON.
 */
public enum ApprovalState {
    DRAFT,
    SUBMITTED,
    REVIEW,
    APPROVED
}
