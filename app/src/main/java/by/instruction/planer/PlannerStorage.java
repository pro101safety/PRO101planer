package by.instruction.planer;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage for planner entries with backward compatibility.
 *
 * IMPORTANT: When making changes that affect data storage:
 * 1. NEVER change PREF_NAME - it will cause data loss
 * 2. Use data versioning for structural changes
 * 3. Keep JSON field names consistent
 * 4. Use opt* methods in fromJson() for new fields
 * 5. Test data migration thoroughly
 */
public class PlannerStorage {

    public static final String PREF_NAME = "planner_prefs";
    public static final String KEY_ENTRIES = "entries";
    public static final String KEY_DATA_VERSION = "data_version";
    public static final int CURRENT_DATA_VERSION = 1;

    private final SharedPreferences preferences;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Map<String, PlannerEntry> cache = new HashMap<>();
    private final Pattern keyPattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})_(\\d{1,2})$");

    public PlannerStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkDataVersion();
        load();
    }

    private void checkDataVersion() {
        int storedVersion = preferences.getInt(KEY_DATA_VERSION, 0);
        if (storedVersion < CURRENT_DATA_VERSION) {
            // Perform data migration if needed
            migrateData(storedVersion);
            // Update version
            preferences.edit().putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION).apply();
        }
    }

    private void migrateData(int fromVersion) {
        // Future migrations can be added here
        // For now, data format is backward compatible
        android.util.Log.i("PlannerStorage", "Migrating data from version " + fromVersion + " to " + CURRENT_DATA_VERSION);
    }

    public static class SearchResult {
        public final String date; // yyyy-MM-dd
        public final int hour;
        public final String text;

        public SearchResult(String date, int hour, String text) {
            this.date = date;
            this.hour = hour;
            this.text = text;
        }
    }

    public java.util.List<SearchResult> searchEntries(String query) {
        java.util.List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return results;
        String lower = query.toLowerCase(Locale.getDefault());
        for (Map.Entry<String, PlannerEntry> entry : cache.entrySet()) {
            PlannerEntry value = entry.getValue();
            if (value == null || value.getText() == null) continue;
            if (value.getText().toLowerCase(Locale.getDefault()).contains(lower)) {
                Matcher m = keyPattern.matcher(entry.getKey());
                if (m.matches()) {
                    String date = m.group(1);
                    int hour = Integer.parseInt(m.group(2));
                    results.add(new SearchResult(date, hour, value.getText()));
                }
            }
        }
        return results;
    }

    public PlannerEntry getEntry(java.util.Calendar day, int hour) {
        return cache.get(makeKey(day, hour));
    }

    public PlannerEntry findEntryByReminderAtMillis(long reminderAtMillis) {
        for (PlannerEntry entry : cache.values()) {
            if (entry != null && entry.getReminderAtMillis() != null &&
                entry.getReminderAtMillis() == reminderAtMillis) {
                return entry;
            }
        }
        return null;
    }

    public static class EventLocation {
        public final java.util.Calendar day;
        public final int hour;

        public EventLocation(java.util.Calendar day, int hour) {
            this.day = day;
            this.hour = hour;
        }
    }

    public EventLocation findEventLocationByReminderAtMillis(long reminderAtMillis) {
        for (Map.Entry<String, PlannerEntry> entry : cache.entrySet()) {
            PlannerEntry value = entry.getValue();
            if (value != null && value.getReminderAtMillis() != null &&
                value.getReminderAtMillis() == reminderAtMillis) {
                Matcher m = keyPattern.matcher(entry.getKey());
                if (m.matches()) {
                    try {
                        String dateStr = m.group(1);
                        int hour = Integer.parseInt(m.group(2));
                        java.util.Calendar day = java.util.Calendar.getInstance();
                        day.setTime(dateFormatter.parse(dateStr));
                        return new EventLocation(day, hour);
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
        }
        return null;
    }

    public void saveEntry(java.util.Calendar day, int hour, String text, Integer reminderOffsetMinutes, Long reminderAtMillis, Integer recurrenceType, Boolean done) {
        String key = makeKey(day, hour);
        if (TextUtils.isEmpty(text)) {
            cache.remove(key);
            persist();
            return;
        }

        PlannerEntry entry = cache.containsKey(key) ? cache.get(key) : new PlannerEntry();
        entry.setText(text);
        if (reminderOffsetMinutes != null) {
            entry.setReminderOffsetMinutes(reminderOffsetMinutes);
        }
        if (reminderAtMillis != null) {
            entry.setReminderAtMillis(reminderAtMillis);
        }
        if (recurrenceType != null) {
            entry.setRecurrenceType(recurrenceType);
        }
        if (done != null) {
            entry.setDone(done);
        }
        cache.put(key, entry);
        persist();
    }

    public void clearReminder(java.util.Calendar day, int hour) {
        String key = makeKey(day, hour);
        PlannerEntry entry = cache.get(key);
        if (entry != null) {
            entry.setReminderOffsetMinutes(null);
            entry.setReminderAtMillis(null);
            entry.setRecurrenceType(null);
            cache.put(key, entry);
            persist();
        }
    }

    public Map<String, PlannerEntry> getAllEntries() {
        return new HashMap<>(cache);
    }

    private void load() {
        String json = preferences.getString(KEY_ENTRIES, null);
        if (TextUtils.isEmpty(json)) {
            cache.clear();
            return;
        }

        try {
            JSONObject stored = new JSONObject(json);
            Iterator<String> keys = stored.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject entryObject = stored.getJSONObject(key);
                try {
                    cache.put(key, PlannerEntry.fromJson(entryObject));
                } catch (JSONException e) {
                    // Skip corrupted entries instead of clearing all data
                    android.util.Log.w("PlannerStorage", "Skipping corrupted entry: " + key);
                }
            }
        } catch (JSONException e) {
            // Try to migrate old format or clear cache as last resort
            android.util.Log.e("PlannerStorage", "Failed to load data, clearing cache");
            cache.clear();
        }
    }

    private void persist() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, PlannerEntry> entry : cache.entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue().toJson());
            } catch (JSONException ignored) {
                // ignore broken entry and continue
            }
        }
        preferences.edit().putString(KEY_ENTRIES, jsonObject.toString()).apply();
    }

    private String makeKey(java.util.Calendar day, int hour) {
        java.util.Calendar keyDay = (java.util.Calendar) day.clone();
        keyDay.set(java.util.Calendar.HOUR_OF_DAY, 0);
        keyDay.set(java.util.Calendar.MINUTE, 0);
        keyDay.set(java.util.Calendar.SECOND, 0);
        keyDay.set(java.util.Calendar.MILLISECOND, 0);
        return dateFormatter.format(keyDay.getTime()) + "_" + hour;
    }
}

