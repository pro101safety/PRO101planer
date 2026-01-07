package by.instruction.planer;

public final class ReminderConfig {

    private ReminderConfig() {
    }

    public static final int RECURRENCE_NONE = 0;
    public static final int RECURRENCE_DAY = 1;
    public static final int RECURRENCE_WEEK = 2;
    public static final int RECURRENCE_MONTH = 3;
    public static final int RECURRENCE_QUARTER = 4;
    public static final int RECURRENCE_YEAR = 5;
    public static final int RECURRENCE_HOUR = 6;

    public static final int[] REMINDER_OFFSETS = {0, 15, 30, 60};
}

