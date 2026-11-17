package com.pranav.saarthi.data;

import android.content.Context;

/**
 * The app always reads and writes the local SQLite store, so it works offline.
 * SyncManager keeps it in step with the server.
 */
public class DatabaseProvider {

    public static NotesRepository getRepository(Context context) {
        return new DatabaseHelper(context);
    }
}
