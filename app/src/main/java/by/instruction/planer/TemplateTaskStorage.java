package by.instruction.planer;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemplateTaskStorage {

    private static final String PREF_NAME = "template_tasks";
    private static final String KEY_PREFIX = "templates_";

    private final SharedPreferences prefs;

    public TemplateTaskStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class TaskItem {
        public final String id;
        public final String text;
        public final boolean isStatic;
        public final Long reminderAtMillis;
        public final Integer reminderOffsetMinutes;
        public final Integer recurrenceType;

        public TaskItem(String id, String text, boolean isStatic, Long reminderAtMillis, Integer reminderOffsetMinutes, Integer recurrenceType) {
            this.id = id;
            this.text = text;
            this.isStatic = isStatic;
            this.reminderAtMillis = reminderAtMillis;
            this.reminderOffsetMinutes = reminderOffsetMinutes;
            this.recurrenceType = recurrenceType;
        }

        public TaskItem withReminder(Long atMillis, Integer offset, Integer recurrence) {
            return new TaskItem(id, text, isStatic, atMillis, offset, recurrence);
        }
    }

    public List<TaskItem> load(String sectionKey, List<TaskItem> defaults) {
        String json = prefs.getString(makeKey(sectionKey), null);
        if (json == null && defaults != null) {
            save(sectionKey, defaults);
            return defaults;
        }
        List<TaskItem> list = new ArrayList<>();
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new TaskItem(
                        o.optString("id"),
                        o.optString("text", ""),
                        o.optBoolean("isStatic", false),
                        o.has("reminderAtMillis") ? o.optLong("reminderAtMillis") : null,
                        o.has("reminderOffsetMinutes") ? o.optInt("reminderOffsetMinutes") : null,
                        o.has("recurrenceType") ? o.optInt("recurrenceType") : null
                ));
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    public void save(String sectionKey, List<TaskItem> tasks) {
        JSONArray arr = new JSONArray();
        for (TaskItem t : tasks) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", t.id);
                o.put("text", t.text);
                o.put("isStatic", t.isStatic);
                if (t.reminderAtMillis != null) o.put("reminderAtMillis", t.reminderAtMillis);
                if (t.reminderOffsetMinutes != null) o.put("reminderOffsetMinutes", t.reminderOffsetMinutes);
                if (t.recurrenceType != null) o.put("recurrenceType", t.recurrenceType);
                arr.put(o);
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(makeKey(sectionKey), arr.toString()).apply();
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }

    private String makeKey(String sectionKey) {
        return KEY_PREFIX + sectionKey;
    }
}

