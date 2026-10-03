package com.pranav.saarthi.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private String name;
    private String text;
    private String title;
    private String status;
    private String remarks;
    @SerializedName("date_time")
    private String dateTime;

    public Task(int id, String name, String text, String title, String status, String remarks, String dateTime) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.title = title;
        this.status = status;
        this.remarks = remarks;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
