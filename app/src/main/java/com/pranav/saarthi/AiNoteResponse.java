package com.pranav.saarthi;

import com.google.gson.annotations.SerializedName;

public class AiNoteResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("note")
    private Note note;

    public String getMessage() {
        return message;
    }

    public Note getNote() {
        return note;
    }
}
