package by.instruction.planer;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NoteStorage {

    private static final String PREF_NAME = "note_storage";
    private static final String KEY_PREFIX = "notes_";

    private final SharedPreferences prefs;

    public NoteStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class NoteItem {
        public final String id;
        public final String folderId;
        public final String text;
        public final String imageUri;
        public final Long reminderAtMillis;
        public final Integer reminderOffsetMinutes;
        public final Integer recurrenceType;
        public final long createdAtMillis;

        public NoteItem(String id,
                        String folderId,
                        String text,
                        String imageUri,
                        Long reminderAtMillis,
                        Integer reminderOffsetMinutes,
                        Integer recurrenceType,
                        long createdAtMillis) {
            this.id = id;
            this.folderId = folderId;
            this.text = text;
            this.imageUri = imageUri;
            this.reminderAtMillis = reminderAtMillis;
            this.reminderOffsetMinutes = reminderOffsetMinutes;
            this.recurrenceType = recurrenceType;
            this.createdAtMillis = createdAtMillis;
        }

        public NoteItem withReminder(Long atMillis, Integer offsetMinutes, Integer recurrence) {
            return new NoteItem(id, folderId, text, imageUri, atMillis, offsetMinutes, recurrence, createdAtMillis);
        }
    }

    public List<NoteItem> load(String sectionKey) {
        String json = prefs.getString(makeKey(sectionKey), null);
        List<NoteItem> list = new ArrayList<>();
        if (TextUtils.isEmpty(json)) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new NoteItem(
                        o.optString("id"),
                        o.optString("folderId", null),
                        o.optString("text", ""),
                        o.optString("imageUri", null),
                        o.has("reminderAtMillis") ? o.optLong("reminderAtMillis") : null,
                        o.has("reminderOffsetMinutes") ? o.optInt("reminderOffsetMinutes") : null,
                        o.has("recurrenceType") ? o.optInt("recurrenceType") : null,
                        o.optLong("createdAtMillis", System.currentTimeMillis())
                ));
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    public void save(String sectionKey, List<NoteItem> notes) {
        JSONArray arr = new JSONArray();
        for (NoteItem n : notes) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", n.id);
                if (n.folderId != null) o.put("folderId", n.folderId);
                o.put("text", n.text == null ? "" : n.text);
                if (n.imageUri != null) o.put("imageUri", n.imageUri);
                if (n.reminderAtMillis != null) o.put("reminderAtMillis", n.reminderAtMillis);
                if (n.reminderOffsetMinutes != null) o.put("reminderOffsetMinutes", n.reminderOffsetMinutes);
                if (n.recurrenceType != null) o.put("recurrenceType", n.recurrenceType);
                o.put("createdAtMillis", n.createdAtMillis);
                arr.put(o);
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(makeKey(sectionKey), arr.toString()).apply();
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }

    public void upsert(String sectionKey, NoteItem note) {
        List<NoteItem> current = load(sectionKey);
        Map<String, NoteItem> map = new HashMap<>();
        for (NoteItem n : current) {
            map.put(n.id, n);
        }
        map.put(note.id, note);
        save(sectionKey, new ArrayList<>(map.values()));
    }

    public void delete(String sectionKey, String noteId) {
        List<NoteItem> current = load(sectionKey);
        List<NoteItem> filtered = new ArrayList<>();
        for (NoteItem n : current) {
            if (!n.id.equals(noteId)) {
                filtered.add(n);
            }
        }
        save(sectionKey, filtered);
    }

    public void deleteByFolderIds(String sectionKey, Set<String> folderIds) {
        if (folderIds == null || folderIds.isEmpty()) return;
        List<NoteItem> current = load(sectionKey);
        List<NoteItem> filtered = new ArrayList<>();
        for (NoteItem n : current) {
            if (n.folderId != null && folderIds.contains(n.folderId)) continue;
            filtered.add(n);
        }
        save(sectionKey, filtered);
    }

    private String makeKey(String sectionKey) {
        return KEY_PREFIX + sectionKey;
    }
}

