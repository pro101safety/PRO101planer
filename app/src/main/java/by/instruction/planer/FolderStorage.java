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

public class FolderStorage {

    public static final String PREF_NAME = "folder_storage";

    private final SharedPreferences prefs;

    public FolderStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class FolderNode {
        public final String id;
        public final String parentId; // null for root
        public final String name;

        public FolderNode(String id, String parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }
    }

    public List<FolderNode> load(String sectionKey) {
        String json = prefs.getString(makeKey(sectionKey), null);
        List<FolderNode> list = new ArrayList<>();
        if (TextUtils.isEmpty(json)) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new FolderNode(
                        o.optString("id"),
                        o.optString("parentId", null),
                        o.optString("name", "")
                ));
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    public void save(String sectionKey, List<FolderNode> nodes) {
        JSONArray arr = new JSONArray();
        for (FolderNode n : nodes) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", n.id);
                if (n.parentId != null) o.put("parentId", n.parentId);
                o.put("name", n.name);
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
        return "folders_" + sectionKey;
    }
}

