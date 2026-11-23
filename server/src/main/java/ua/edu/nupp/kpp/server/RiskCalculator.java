package ua.edu.nupp.kpp.server;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Розрахунок факторів F1–F3 та інтегрального показника
 * I = 0,40·F1 + 0,35·F2 + 0,25·F3 (усі фактори нормуються до 0..100).
 *
 * F1 — близькість до граничного строку погодження;
 * F2 — критичність документа;
 * F3 — кількість залежних процесів.
 */
public final class RiskCalculator {

    /** Вікно, у межах якого строк вважається «близьким» (днів). */
    public static final int DEADLINE_WINDOW_DAYS = 30;

    /** Кількість залежних процесів, якій відповідає F3 = 100. */
    public static final int PROCESSES_FOR_MAX = 10;

    private static final double W1 = 0.40;
    private static final double W2 = 0.35;
    private static final double W3 = 0.25;

    private RiskCalculator() {
    }

    /**
     * F1: 0, якщо до строку 30 і більше днів; зростає лінійно в міру
     * наближення строку; 100, якщо строк настав або прострочений.
     * F1 = max(0; min(100; (30 − днів до строку) / 30 · 100)).
     */
    public static double factor1(LocalDate deadline, LocalDate today) {
        if (deadline == null || today == null) {
            throw new IllegalArgumentException("Не вказано дату для розрахунку F1");
        }
        long daysLeft = ChronoUnit.DAYS.between(today, deadline);
        double value = (DEADLINE_WINDOW_DAYS - daysLeft) * 100.0 / DEADLINE_WINDOW_DAYS;
        return round2(Math.max(0.0, Math.min(100.0, value)));
    }

    /** F2 дорівнює критичності документа (вона вже в діапазоні 0..100). */
    public static double factor2(int criticality) {
        if (criticality < 0 || criticality > 100) {
            throw new IllegalArgumentException("Критичність має бути в діапазоні 0..100");
        }
        return criticality;
    }

    /**
     * F3 = min(100; кількість залежних процесів / 10 · 100).
     */
    public static double factor3(int dependentProcesses) {
        if (dependentProcesses < 0) {
            throw new IllegalArgumentException(
                    "Кількість залежних процесів не може бути від'ємною");
        }
        double value = dependentProcesses * 100.0 / PROCESSES_FOR_MAX;
        return round2(Math.min(100.0, value));
    }

    /** Інтегральний показник I, округлений до двох знаків після коми. */
    public static double calculate(double f1, double f2, double f3) {
        if (!inRange(f1) || !inRange(f2) || !inRange(f3)) {
            throw new IllegalArgumentException("F1, F2 і F3 мають бути в діапазоні 0..100");
        }
        return round2(W1 * f1 + W2 * f2 + W3 * f3);
    }

    private static boolean inRange(double f) {
        return f >= 0.0 && f <= 100.0;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
