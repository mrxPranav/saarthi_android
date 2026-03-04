package com.pranav.saarthi;

import com.google.gson.annotations.SerializedName;

public class HealthCheck {
    private int id;

    @SerializedName("request_time")
    private String requestTime;

    @SerializedName("response_time")
    private String responseTime;

    private double difference;
    private String status;

    public int getId() {
        return id;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public double getDifference() {
        return difference;
    }

    public String getStatus() {
        return status;
    }
}
