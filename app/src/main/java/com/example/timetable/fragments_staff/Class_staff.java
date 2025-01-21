package com.example.timetable.fragments_staff;

public class Class_staff
{
    private final String name, position, url, photoUrl;

    public Class_staff(String name, String position, String url, String photoUrl)
    {
        this.name = name;
        this.position = position;
        this.url = url;
        this.photoUrl = photoUrl;
    }

    public String getName()
    {
        return name;
    }

    public String getPosition()
    {
        return position;
    }

    public String getUrl()
    {
        return url;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }
}
