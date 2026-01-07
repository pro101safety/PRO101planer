package by.instruction.planer;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsUtil {
    public static String readPref(Context context, String prefName, String key) {
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (key == null) {
            // export all as raw string (assumes single string entry)
            return prefs.getAll().isEmpty() ? null : new org.json.JSONObject(prefs.getAll()).toString();
        }
        return prefs.getString(key, null);
    }

    public static void writePref(Context context, String prefName, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (key == null) {
            prefs.edit().clear().apply();
            if (value != null) {
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(value);
                    java.util.Iterator<String> it = obj.keys();
                    while (it.hasNext()) {
                        String k = it.next();
                        Object v = obj.opt(k);
                        if (v instanceof String) editor.putString(k, (String) v);
                    }
                } catch (Exception ignored) {
                }
            }
            editor.apply();
            return;
        }
        editor.clear();
        if (value != null) editor.putString(key, value);
        editor.apply();
    }
}

