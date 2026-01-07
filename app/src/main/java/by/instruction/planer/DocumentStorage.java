package by.instruction.planer;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentStorage {

    private static final String PREF_NAME = "documents_storage";
    private static final String KEY_DATA = "documents_data";

    private final SharedPreferences prefs;

    public DocumentStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class FolderItem {
        public final String id;
        public final String parentId; // null for root
        public final String name;

        public FolderItem(String id, String parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }
    }

    public static class FileItem {
        public final String id;
        public final String folderId; // null for root
        public final String name;
        public final String uri;
        public final String mimeType;
        public final Long reminderAtMillis;
        public final Integer reminderOffsetMinutes;
        public final Integer recurrenceType;

        public FileItem(String id,
                        String folderId,
                        String name,
                        String uri,
                        String mimeType,
                        Long reminderAtMillis,
                        Integer reminderOffsetMinutes,
                        Integer recurrenceType) {
            this.id = id;
            this.folderId = folderId;
            this.name = name;
            this.uri = uri;
            this.mimeType = mimeType;
            this.reminderAtMillis = reminderAtMillis;
            this.reminderOffsetMinutes = reminderOffsetMinutes;
            this.recurrenceType = recurrenceType;
        }

        public FileItem withReminder(Long at, Integer offset, Integer recurrence) {
            return new FileItem(id, folderId, name, uri, mimeType, at, offset, recurrence);
        }
    }

    public static class Data {
        public final List<FolderItem> folders;
        public final List<FileItem> files;

        public Data(List<FolderItem> folders, List<FileItem> files) {
            this.folders = folders;
            this.files = files;
        }
    }

    public Data load() {
        String json = prefs.getString(KEY_DATA, null);
        if (TextUtils.isEmpty(json)) {
            return new Data(new ArrayList<>(), new ArrayList<>());
        }
        try {
            JSONObject obj = new JSONObject(json);
            List<FolderItem> folders = new ArrayList<>();
            List<FileItem> files = new ArrayList<>();

            JSONArray foldersArr = obj.optJSONArray("folders");
            if (foldersArr != null) {
                for (int i = 0; i < foldersArr.length(); i++) {
                    JSONObject f = foldersArr.getJSONObject(i);
                    folders.add(new FolderItem(
                            f.optString("id"),
                            f.optString("parentId", null),
                            f.optString("name", "")
                    ));
                }
            }

            JSONArray filesArr = obj.optJSONArray("files");
            if (filesArr != null) {
                for (int i = 0; i < filesArr.length(); i++) {
                    JSONObject f = filesArr.getJSONObject(i);
                    files.add(new FileItem(
                            f.optString("id"),
                            f.optString("folderId", null),
                            f.optString("name", ""),
                            f.optString("uri", ""),
                            f.optString("mimeType", null),
                            f.has("reminderAtMillis") ? f.optLong("reminderAtMillis") : null,
                            f.has("reminderOffsetMinutes") ? f.optInt("reminderOffsetMinutes") : null,
                            f.has("recurrenceType") ? f.optInt("recurrenceType") : null
                    ));
                }
            }

            return new Data(folders, files);
        } catch (JSONException e) {
            return new Data(new ArrayList<>(), new ArrayList<>());
        }
    }

    public void save(Data data) {
        JSONObject obj = new JSONObject();
        JSONArray foldersArr = new JSONArray();
        for (FolderItem f : data.folders) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", f.id);
                if (f.parentId != null) o.put("parentId", f.parentId);
                o.put("name", f.name);
                foldersArr.put(o);
            } catch (JSONException ignored) {
            }
        }

        JSONArray filesArr = new JSONArray();
        for (FileItem f : data.files) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", f.id);
                if (f.folderId != null) o.put("folderId", f.folderId);
                o.put("name", f.name);
                o.put("uri", f.uri);
                if (f.mimeType != null) o.put("mimeType", f.mimeType);
                if (f.reminderAtMillis != null) o.put("reminderAtMillis", f.reminderAtMillis);
                if (f.reminderOffsetMinutes != null) o.put("reminderOffsetMinutes", f.reminderOffsetMinutes);
                if (f.recurrenceType != null) o.put("recurrenceType", f.recurrenceType);
                filesArr.put(o);
            } catch (JSONException ignored) {
            }
        }

        try {
            obj.put("folders", foldersArr);
            obj.put("files", filesArr);
        } catch (JSONException ignored) {
        }

        prefs.edit().putString(KEY_DATA, obj.toString()).apply();
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }
}

