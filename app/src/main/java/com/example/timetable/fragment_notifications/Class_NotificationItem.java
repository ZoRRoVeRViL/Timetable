package com.example.timetable.fragment_notifications;

public class Class_NotificationItem
{
    String id;
    String verification;
    String type;
    String title;
    String time;
    String url;
    String description;

    public Class_NotificationItem(String verification, String id, String type, String title, String time, String url, String description)
    {
        this.id = id;
        this.verification = verification;
        this.type = type;
        this.title = title;
        this.time = time;
        this.url = url;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getVerification()
    {
        return verification;
    }

    public String getType()
    {
        return type;
    }

    public String getTitle()
    {
        return title;
    }

    public String getTime()
    {
        return time;
    }

    public String getUrl()
    {
        return url;
    }

    public String getDescription()
    {
        return description;
    }

    public void setName(String name) {
    }

    public void setTime(String time) {
    }

    public void setUrl(String url) {
    }

    public void setDescription(String description) {
    }

    public void setType(String type) {
    }
}