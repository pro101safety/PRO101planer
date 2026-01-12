package by.instruction.planer;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlannerWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int widgetId;
    private final PlannerStorage storage;
    private final PlannerWidgetPrefs prefs;
    private List<WidgetTaskItem> tasks = new ArrayList<>();

    public PlannerWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.widgetId = intent.getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID);
        this.storage = new PlannerStorage(context);
        this.prefs = new PlannerWidgetPrefs(context, widgetId);
    }

    @Override
    public void onCreate() {
        // Initialize
    }

    @Override
    public void onDataSetChanged() {
        tasks.clear();
        int currentDayOffset = PlannerWidgetProvider.getWidgetDayOffset(widgetId);
        Calendar currentDay = Calendar.getInstance();
        currentDay.add(Calendar.DAY_OF_MONTH, currentDayOffset);

        for (int hour = 0; hour < 24; hour++) {
            PlannerEntry entry = storage.getEntry(currentDay, hour);
            if (entry != null && !android.text.TextUtils.isEmpty(entry.getText())) {
                tasks.add(new WidgetTaskItem(hour, entry.getText(), entry.isDone()));
            }
        }
    }

    @Override
    public void onDestroy() {
        tasks.clear();
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= tasks.size()) {
            return null;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_planner_item);
        WidgetTaskItem task = tasks.get(position);

        String timeText = String.format(Locale.getDefault(), "%02d:00", task.hour);
        views.setTextViewText(R.id.widget_item_time, timeText);
        views.setTextViewText(R.id.widget_item_text, task.text);

        // Set strike-through for completed tasks
        if (task.isDone) {
            views.setInt(R.id.widget_item_text, "setPaintFlags",
                android.graphics.Paint.STRIKE_THRU_TEXT_FLAG | android.graphics.Paint.ANTI_ALIAS_FLAG);
        } else {
            views.setInt(R.id.widget_item_text, "setPaintFlags", android.graphics.Paint.ANTI_ALIAS_FLAG);
        }

        // Set click intent to open main app at specific day and hour
        Intent fillInIntent = new Intent(context, MainActivity.class);
        fillInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fillInIntent.putExtra("widget_day_offset", PlannerWidgetProvider.getWidgetDayOffset(widgetId));
        fillInIntent.putExtra("widget_target_hour", task.hour);
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.widget_planner_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static class WidgetTaskItem {
        final int hour;
        final String text;
        final boolean isDone;

        WidgetTaskItem(int hour, String text, boolean isDone) {
            this.hour = hour;
            this.text = text;
            this.isDone = isDone;
        }
    }
}