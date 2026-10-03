package com.pranav.saarthi.model;

import com.google.gson.annotations.SerializedName;

public class Note {
    @SerializedName("id")
    private int id;

    @SerializedName("note")
    private String text;

    @SerializedName("title")
    private String title;

    @SerializedName("datetime")
    private String dateTime;

    @SerializedName("category")
    private String category;

    @SerializedName("sub_category")
    private String subCategory;

    @SerializedName("status")
    private String status;

    @SerializedName("remark")
    private String remarks;

    // Not present in JSON, but kept for SQLite compatibility
    private String comment;

    public Note(int id, String text, String title, String dateTime, String category, String subCategory, String status,
            String remarks, String comment) {
        this.id = id;
        this.text = text;
        this.title = title;
        this.dateTime = dateTime;
        this.category = category;
        this.subCategory = subCategory;
        this.status = status;
        this.remarks = remarks;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getComment() {
        return comment;
    }
}
