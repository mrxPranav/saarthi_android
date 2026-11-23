package com.pranav.saarthi.model;

public class HardwareItem {
    private final String id;
    private final String category;
    private final String name;
    private String details;
    private String status;
    private boolean available;

    public HardwareItem(String id, String category, String name, String details,
                        String status, boolean available) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.details = details;
        this.status = status;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
