package com.example.timetable.fragments_settings;

public class Class_users
{
    private String userName;
    private String userValue;

    public Class_users()
    {}

    public Class_users(String userName, String userValue)
    {
        this.userName = userName;
        this.userValue = userValue;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserValue()
    {
        return userValue;
    }

    public void setUserValue(String userValue)
    {
        this.userValue = userValue;
    }
}
