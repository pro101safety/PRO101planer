package by.instruction.planer;

import android.content.Context;
import android.content.SharedPreferences;

public class PlannerWidgetPrefs {
    private static final String PREF_NAME = "widget_prefs";
    private static final String KEY_DAY_OFFSET = "day_offset_";

    private final SharedPreferences prefs;
    private final int widgetId;

    public PlannerWidgetPrefs(Context context, int widgetId) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.widgetId = widgetId;
    }

    public int getDayOffset() {
        return prefs.getInt(KEY_DAY_OFFSET + widgetId, 0);
    }

    public void setDayOffset(int dayOffset) {
        prefs.edit().putInt(KEY_DAY_OFFSET + widgetId, dayOffset).apply();
    }
}