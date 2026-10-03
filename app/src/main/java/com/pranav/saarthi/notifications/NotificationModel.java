package com.pranav.saarthi.notifications;

public class NotificationModel {
    private int id;
    private String packageName;
    private String title;
    private String text;
    private String bigText;
    private String postTime;
    private boolean reply;
    private String notificationKey;

    public NotificationModel(int id, String packageName, String title, String text, String bigText,
                             String postTime, boolean reply, String notificationKey) {
        this.id = id;
        this.packageName = packageName;
        this.title = title;
        this.text = text;
        this.bigText = bigText;
        this.postTime = postTime;
        this.reply = reply;
        this.notificationKey = notificationKey;
    }

    public NotificationModel(String packageName, String title, String text, String bigText,
                             String postTime, boolean reply, String notificationKey) {
        this.packageName = packageName;
        this.title = title;
        this.text = text;
        this.bigText = bigText;
        this.postTime = postTime;
        this.reply = reply;
        this.notificationKey = notificationKey;
    }

    public int getId() {
        return id;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getBigText() {
        return bigText;
    }

    public String getPostTime() {
        return postTime;
    }

    public boolean getReply() {
        return reply;
    }

    public String getNotificationKey() {
        return notificationKey;
    }
}
