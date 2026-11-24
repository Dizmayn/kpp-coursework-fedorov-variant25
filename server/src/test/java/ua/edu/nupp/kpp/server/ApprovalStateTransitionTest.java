package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalStateTransitionTest {

    @Test
    void draftMayBeSubmitted() {
        assertTrue(ApprovalRequestService.isAllowedTransition(
                ApprovalState.DRAFT, ApprovalState.SUBMITTED));
    }

    @Test
    void submittedMayGoToReview() {
        assertTrue(ApprovalRequestService.isAllowedTransition(
                ApprovalState.SUBMITTED, ApprovalState.REVIEW));
    }

    @Test
    void reviewMayBeApproved() {
        assertTrue(ApprovalRequestService.isAllowedTransition(
                ApprovalState.REVIEW, ApprovalState.APPROVED));
    }

    @Test
    void skippingStatesIsForbidden() {
        assertFalse(ApprovalRequestService.isAllowedTransition(
                ApprovalState.DRAFT, ApprovalState.REVIEW));
        assertFalse(ApprovalRequestService.isAllowedTransition(
                ApprovalState.DRAFT, ApprovalState.APPROVED));
        assertFalse(ApprovalRequestService.isAllowedTransition(
                ApprovalState.SUBMITTED, ApprovalState.APPROVED));
    }

    @Test
    void movingBackwardsIsForbidden() {
        assertFalse(ApprovalRequestService.isAllowedTransition(
                ApprovalState.SUBMITTED, ApprovalState.DRAFT));
        assertFalse(ApprovalRequestService.isAllowedTransition(
                ApprovalState.REVIEW, ApprovalState.SUBMITTED));
    }

    @Test
    void finalStateAllowsNoTransitionsAndSameStateIsForbidden() {
        for (ApprovalState target : ApprovalState.values()) {
            assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.APPROVED, target));
        }
        assertFalse(ApprovalRequestService.isAllowedTransition(
                ApprovalState.DRAFT, ApprovalState.DRAFT));
    }

    @Test
    void commentLengthIsLimitedTo500Characters() {
        assertDoesNotThrow(() -> ApprovalRequestService.validateComment(null));
        assertDoesNotThrow(() -> ApprovalRequestService.validateComment("x".repeat(500)));
        assertThrows(IllegalArgumentException.class,
                () -> ApprovalRequestService.validateComment("x".repeat(501)));
    }
}
