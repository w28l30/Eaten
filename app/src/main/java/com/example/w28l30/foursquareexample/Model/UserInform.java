package com.example.w28l30.foursquareexample.Model;

/**
 * Created by XiaowenJiang on 10/30/15.
 */
public class UserInform {
    private int id;
    private String username;
    private String Name;
    private String password;
    private String Email;
    private int Gender;
    private int Rate;

    public UserInform(){}
    
    //constructor: pass userinform from server to client
    public UserInform(String username, String name, String email, int gender, int rate, String password)
    {
        this.username = username;
        this.Name = name;
        this.Email = email;
        this.Gender = gender;
        this.Rate = rate;
        this.password = password;
    }


    public void setID(int id)
    {
        this.id = id;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
    public void setName(String name)
    {
        this.Name = name;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public void setEmail(String email)
    {
        this.Email = email;
    }
    public void setGender(int gender)
    {
        this.Gender = gender;
    }
    public void setRate(int rate)
    {
        this.Rate = rate;
    }
    public long getID()
    {
        return this.id;
    }
    public String getUsername()
    {
        return this.username;
    }
    public String getPassword()
    {
        return this.password;
    }
    public String getName(){return this.Name;}
    public String getEmail(){return this.Email;}
    public int getGender(){return this.Gender;}
    public int getRate(){return this.Rate;}
}
