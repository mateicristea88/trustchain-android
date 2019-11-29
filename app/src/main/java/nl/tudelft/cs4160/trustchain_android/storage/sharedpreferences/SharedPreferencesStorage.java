package nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * The class that holds all the functions necessary to store data locally.
 */
public final class SharedPreferencesStorage {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static Gson gson;

    /**
     * Returns the string value that should be stored under some given key.
     *
     * @param context
     * @param key
     * @return
     */
    public static String readSharedPreferences(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

        if (settings.contains(key)) {
            String object = settings.getString(key, null);
            return object;
        } else {
            return null;
        }
    }

    /**
     * Stores a given String value under a given key.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void writeSharedPreferences(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Gets all locally stored values.
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getAll();
    }


    public static <T> T readSharedPreferences(Context context, String key, Class<T> type) throws IOException, ClassNotFoundException {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        if (settings.contains(key)) {
            String object = settings.getString(key, null);
            if (gson == null) {
                gson = new GsonBuilder().create();
            }
            return gson.fromJson(object, type);
        } else {
            return null;
        }
    }

    public static void writeSharedPreferences(Context context, String key, Object value) throws IOException {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        String a = gson.toJson(value);
        editor.putString(key, a);
        editor.apply();
    }

    /**
     * Loops through all entries in SharedPreferences and checks if the entry matches the key.
     * If it does it removes it. This makes it easier to remove all entries with a certain prefix.
     * @param context
     * @param key - the key that needs to be matched
     */
    public static void removeAllWithKey(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        Map<String, ?> allEntries = settings.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String k = entry.getKey();
            if (k.contains(key)) {
                editor.remove(k);
            }
            editor.commit();
        }
    }
}
