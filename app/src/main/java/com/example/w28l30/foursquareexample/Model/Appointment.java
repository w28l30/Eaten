package com.example.w28l30.foursquareexample.Model;

/**
 * Created by XiaowenJiang on 11/14/15.
 */
public class Appointment {
    private String Time;
    private String Name;
    private String Category;
    private String Address;
    private String[] Username;
    private int Limit;
    private int CurrentUsers;

    Appointment(){}
    Appointment(String Time, String Name, String Category, String Address, String[] Username, int Limit)
    {
        this.Time = Time;
        this.Name = Name;
        this.Category = Category;
        this.Address = Address;
        this.Username = Username;
        this.Limit = Limit;
        this.CurrentUsers = Username.length;
    }
    public void setTime(String Time)
    {
        this.Time = Time;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setCategory(String category) {
        this.Category = category;
    }

    public void setAddress(String address) {
        this.Address = address;
    }

    public void setLimit(int limit) {
        this.Limit = limit;
    }

    public void setUsername(String[] username) {
        this.Username = username;
    }

    public void setCurrentUsers(int currentUsers) {
        this.CurrentUsers = currentUsers;
    }

    public String getTime() {
        return Time;
    }

    public String getName() {
        return Name;
    }

    public int getLimit() {
        return Limit;
    }

    public String getAddress() {
        return Address;
    }

    public String getCategory() {
        return Category;
    }

    public String[] getUsername() {
        return Username;
    }

    public int getCurrentUsers() {
        return CurrentUsers;
    }
}
