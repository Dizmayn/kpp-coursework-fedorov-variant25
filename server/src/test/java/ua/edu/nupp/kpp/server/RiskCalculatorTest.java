package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

    @Test
    void factor1IsZeroWhenDeadlineIsThirtyOrMoreDaysAway() {
        assertEquals(0.0, RiskCalculator.factor1(TODAY.plusDays(30), TODAY), 0.001);
        assertEquals(0.0, RiskCalculator.factor1(TODAY.plusDays(90), TODAY), 0.001);
    }

    @Test
    void factor1GrowsLinearlyAsDeadlineApproaches() {
        // 15 днів до строку: (30 − 15) / 30 · 100 = 50
        assertEquals(50.0, RiskCalculator.factor1(TODAY.plusDays(15), TODAY), 0.001);
        // 6 днів: (30 − 6) / 30 · 100 = 80
        assertEquals(80.0, RiskCalculator.factor1(TODAY.plusDays(6), TODAY), 0.001);
    }

    @Test
    void factor1IsHundredWhenDeadlineIsTodayOrOverdue() {
        assertEquals(100.0, RiskCalculator.factor1(TODAY, TODAY), 0.001);
        assertEquals(100.0, RiskCalculator.factor1(TODAY.minusDays(5), TODAY), 0.001);
    }

    @Test
    void factor1RejectsMissingDates() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor1(null, TODAY));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor1(TODAY, null));
    }

    @Test
    void factor2EqualsCriticalityAndRejectsOutOfRange() {
        assertEquals(75.0, RiskCalculator.factor2(75), 0.001);
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor2(-1));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor2(101));
    }

    @Test
    void factor3ScalesByTenProcessesAndIsCappedAtHundred() {
        assertEquals(50.0, RiskCalculator.factor3(5), 0.001);
        assertEquals(100.0, RiskCalculator.factor3(10), 0.001);
        assertEquals(100.0, RiskCalculator.factor3(25), 0.001);
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor3(-1));
    }

    @Test
    void calculateAppliesWeights040035025() {
        // 0,40·50 + 0,35·80 + 0,25·40 = 20 + 28 + 10 = 58
        assertEquals(58.0, RiskCalculator.calculate(50, 80, 40), 0.001);
        assertEquals(100.0, RiskCalculator.calculate(100, 100, 100), 0.001);
        assertEquals(0.0, RiskCalculator.calculate(0, 0, 0), 0.001);
    }

    @Test
    void calculateRejectsFactorsOutsideZeroToHundred() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(-0.1, 50, 50));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(50, 100.1, 50));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(50, 50, 101));
    }

    @Test
    void controlExampleFromSampleDocument() {
        // строк через 6 днів (F1 = 80), критичність 75, 5 процесів (F3 = 50):
        // 0,40·80 + 0,35·75 + 0,25·50 = 32 + 26,25 + 12,5 = 70,75
        double f1 = RiskCalculator.factor1(TODAY.plusDays(6), TODAY);
        double f2 = RiskCalculator.factor2(75);
        double f3 = RiskCalculator.factor3(5);
        assertEquals(70.75, RiskCalculator.calculate(f1, f2, f3), 0.001);
    }
}
