package com.example.timetable.fragments_settings;

public class Class_admin
{
    private String adminID;
    private String adminValue;

    public Class_admin()
    { }

    public Class_admin(String adminID, String adminValue)
    {
        this.adminID = adminID;
        this.adminValue = adminValue;
    }

    public String getAdminID()
    {
        return adminID;
    }

    public String getAdminValue()
    {
        return adminValue;
    }
}