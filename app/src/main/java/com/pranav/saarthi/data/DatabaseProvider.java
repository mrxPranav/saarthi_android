package com.pranav.saarthi.data;

import android.content.Context;
import android.content.SharedPreferences;

public class DatabaseProvider {
    private static final String PREFS_NAME = "DatabasePrefs";
    private static final String KEY_DB_TYPE = "db_type";
    public static final String DB_TYPE_SQLITE = "sqlite";
    public static final String DB_TYPE_API = "postgres"; // Keeping value 'postgres' for preference compatibility

    public static NotesRepository getRepository(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String type = prefs.getString(KEY_DB_TYPE, DB_TYPE_SQLITE);

        if (DB_TYPE_API.equals(type)) {
            return new NetworkNotesRepository();
        } else {
            return new DatabaseHelper(context);
        }
    }
}
