package com.pranav.saarthi;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AiNotesListResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("notes")
    private List<Note> notes;

    public String getMessage() {
        return message;
    }

    public List<Note> getNotes() {
        return notes;
    }
}
