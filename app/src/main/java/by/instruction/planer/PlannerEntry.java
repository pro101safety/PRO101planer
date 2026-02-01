package by.instruction.planer;

import org.json.JSONException;
import org.json.JSONObject;

public class PlannerEntry {

    private String text = "";
    private Integer reminderOffsetMinutes;
    private Long reminderAtMillis;
    private boolean done = false;
    private Integer recurrenceType; // 0 - none, 1 - day, 2 - week, 3 - month, 4 - quarter, 5 - year, 6 - hour, 7 - half-year

    public PlannerEntry() {
    }

    public PlannerEntry(String text, Integer reminderOffsetMinutes) {
        this.text = text;
        this.reminderOffsetMinutes = reminderOffsetMinutes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getReminderOffsetMinutes() {
        return reminderOffsetMinutes;
    }

    public void setReminderOffsetMinutes(Integer reminderOffsetMinutes) {
        this.reminderOffsetMinutes = reminderOffsetMinutes;
    }

    public Long getReminderAtMillis() {
        return reminderAtMillis;
    }

    public void setReminderAtMillis(Long reminderAtMillis) {
        this.reminderAtMillis = reminderAtMillis;
    }

    public Integer getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(Integer recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text == null ? "" : text);
        if (reminderOffsetMinutes != null) {
            jsonObject.put("reminderOffsetMinutes", reminderOffsetMinutes);
        }
        if (reminderAtMillis != null) {
            jsonObject.put("reminderAtMillis", reminderAtMillis);
        }
        if (recurrenceType != null) {
            jsonObject.put("recurrenceType", recurrenceType);
        }
        jsonObject.put("done", done);
        return jsonObject;
    }

    /**
     * Creates PlannerEntry from JSON with backward compatibility.
     * Uses opt* methods to handle missing fields gracefully.
     */
    public static PlannerEntry fromJson(JSONObject jsonObject) throws JSONException {
        PlannerEntry entry = new PlannerEntry();
        entry.text = jsonObject.optString("text", "");
        if (jsonObject.has("reminderOffsetMinutes")) {
            entry.reminderOffsetMinutes = jsonObject.optInt("reminderOffsetMinutes");
        }
        if (jsonObject.has("reminderAtMillis")) {
            entry.reminderAtMillis = jsonObject.optLong("reminderAtMillis");
        }
        if (jsonObject.has("recurrenceType")) {
            entry.recurrenceType = jsonObject.optInt("recurrenceType");
        }
        entry.done = jsonObject.optBoolean("done", false);
        return entry;
    }
}

