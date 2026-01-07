package by.instruction.planer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import android.app.PendingIntent;
import android.app.AlarmManager;
import androidx.core.app.AlarmManagerCompat;
import android.content.Context;
import by.instruction.planer.PlannerStorage;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "planner_reminders";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_RECURRENCE = "extra_recurrence";
    public static final String EXTRA_TRIGGER_AT = "extra_trigger_at";
    public static final String EXTRA_OFFSET = "extra_offset";
    public static final String EXTRA_SKIP_SAVE = "extra_skip_save";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());
        int recurrence = intent.getIntExtra(EXTRA_RECURRENCE, 0);
        long triggerAt = intent.getLongExtra(EXTRA_TRIGGER_AT, -1);
        int offsetMinutes = intent.getIntExtra(EXTRA_OFFSET, 0);
        boolean skipSave = intent.getBooleanExtra(EXTRA_SKIP_SAVE, false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title == null ? context.getString(R.string.app_name) : title)
                .setContentText(message == null ? "" : message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message == null ? "" : message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());

        if (recurrence > 0 && triggerAt > 0) {
            Calendar next = Calendar.getInstance();
            next.setTimeInMillis(triggerAt);
            switch (recurrence) {
                case ReminderConfig.RECURRENCE_DAY:
                    next.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case ReminderConfig.RECURRENCE_WEEK:
                    next.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case ReminderConfig.RECURRENCE_MONTH:
                    next.add(Calendar.MONTH, 1);
                    break;
                case ReminderConfig.RECURRENCE_QUARTER:
                    next.add(Calendar.MONTH, 3);
                    break;
                case ReminderConfig.RECURRENCE_YEAR:
                    next.add(Calendar.YEAR, 1);
                    break;
                case ReminderConfig.RECURRENCE_HOUR:
                    next.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                default:
                    return;
            }

            Intent nextIntent = new Intent(context, ReminderReceiver.class);
            nextIntent.putExtra(EXTRA_TITLE, title);
            nextIntent.putExtra(EXTRA_MESSAGE, message);
            nextIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
            nextIntent.putExtra(EXTRA_RECURRENCE, recurrence);
            nextIntent.putExtra(EXTRA_TRIGGER_AT, next.getTimeInMillis());
            nextIntent.putExtra(EXTRA_OFFSET, offsetMinutes);
            nextIntent.putExtra(EXTRA_SKIP_SAVE, skipSave);

            Calendar reminderTime = (Calendar) next.clone();
            reminderTime.add(Calendar.MINUTE, -offsetMinutes);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            }

            // duplicate task entry for next occurrence (unless explicitly skipped)
            if (message != null && !skipSave) {
                PlannerStorage storage = new PlannerStorage(context);
                storage.saveEntry(next, next.get(Calendar.HOUR_OF_DAY), message, offsetMinutes, next.getTimeInMillis(), recurrence, false);
            }
        }
    }
}

