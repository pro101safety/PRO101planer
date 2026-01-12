package by.instruction.planer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class PlannerWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_PREV_DAY = "by.instruction.planer.ACTION_PREV_DAY";
    public static final String ACTION_NEXT_DAY = "by.instruction.planer.ACTION_NEXT_DAY";
    public static final String ACTION_ADD_TASK = "by.instruction.planer.ACTION_ADD_TASK";
    public static final String ACTION_OPEN_APP = "by.instruction.planer.ACTION_OPEN_APP";
    public static final String EXTRA_WIDGET_ID = "widget_id";

    // Static map to pass dayOffset to factory
    private static final java.util.Map<Integer, Integer> widgetDayOffsets = new java.util.HashMap<>();

    public static int getWidgetDayOffset(int widgetId) {
        return widgetDayOffsets.getOrDefault(widgetId, 0);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            PlannerWidgetPrefs prefs = new PlannerWidgetPrefs(context, appWidgetId);
            int dayOffset = prefs.getDayOffset();
            widgetDayOffsets.put(appWidgetId, dayOffset);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (action != null) {
            int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                switch (action) {
                    case ACTION_PREV_DAY:
                        handlePrevDay(context, appWidgetManager, widgetId);
                        break;
                    case ACTION_NEXT_DAY:
                        handleNextDay(context, appWidgetManager, widgetId);
                        break;
                    case ACTION_ADD_TASK:
                        handleAddTask(context, appWidgetManager, widgetId);
                        break;
                    case ACTION_OPEN_APP:
                        handleOpenApp(context);
                        break;
                }
            }
        }
    }

    private void handlePrevDay(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        PlannerWidgetPrefs prefs = new PlannerWidgetPrefs(context, widgetId);
        int newOffset = prefs.getDayOffset() - 1;
        prefs.setDayOffset(newOffset);
        widgetDayOffsets.put(widgetId, newOffset);
        updateAppWidget(context, appWidgetManager, widgetId);
        // Force list data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list);
    }

    private void handleNextDay(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        PlannerWidgetPrefs prefs = new PlannerWidgetPrefs(context, widgetId);
        int newOffset = prefs.getDayOffset() + 1;
        prefs.setDayOffset(newOffset);
        widgetDayOffsets.put(widgetId, newOffset);
        updateAppWidget(context, appWidgetManager, widgetId);
        // Force list data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list);
    }

    private void handleAddTask(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void handleOpenApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_planner);

        // Get preferences
        PlannerWidgetPrefs prefs = new PlannerWidgetPrefs(context, appWidgetId);

        // Set up the list with unique intent to force refresh
        Intent serviceIntent = new Intent(context, PlannerWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra("day_offset", prefs.getDayOffset());
        serviceIntent.putExtra("unique_key", appWidgetId + "_" + prefs.getDayOffset() + "_" + System.currentTimeMillis());
        views.setRemoteAdapter(R.id.widget_list, serviceIntent);

        // Set up buttons
        views.setOnClickPendingIntent(R.id.widget_prev_button, getPendingIntent(context, ACTION_PREV_DAY, appWidgetId));
        views.setOnClickPendingIntent(R.id.widget_next_button, getPendingIntent(context, ACTION_NEXT_DAY, appWidgetId));
        views.setOnClickPendingIntent(R.id.widget_add_button, getPendingIntent(context, ACTION_ADD_TASK, appWidgetId));
        views.setOnClickPendingIntent(R.id.widget_title, getPendingIntent(context, ACTION_OPEN_APP, appWidgetId));

        // Update title with current date
        views.setTextViewText(R.id.widget_title, getWidgetTitle(context, prefs.getDayOffset()));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent getPendingIntent(Context context, String action, int widgetId) {
        Intent intent = new Intent(context, PlannerWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);

        return PendingIntent.getBroadcast(
            context,
            widgetId * 100 + action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static String getWidgetTitle(Context context, int dayOffset) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, dayOffset);

        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEEE", new java.util.Locale("ru"));
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("d MMM", new java.util.Locale("ru"));

        String dayName = dayFormat.format(cal.getTime());
        String date = dateFormat.format(cal.getTime());

        return dayName + " • " + date;
    }
}