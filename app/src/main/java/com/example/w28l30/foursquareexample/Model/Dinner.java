package com.example.w28l30.foursquareexample.Model;

import com.orm.SugarRecord;

/**
 * Created by W28L30 on 15/11/1.
 */
public class Dinner extends SugarRecord<Dinner> {
    public String restaurant;
    public String address;
    public String time;
    public int num;
    public String memo;
    public String username;
    public String latitude;
    public String longitude;

    public Dinner() {

    }

    public Dinner(String restaurant, String address, int num, String time, String memo,String username,String latitude,String longitude) {
        this.restaurant = restaurant;
        this.address = address;
        this.num = num;
        this.time = time;
        this.memo = memo;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setUsername(String username) {this.username = username;}

    public void setAddress(String address) {this.address = address;}

    public void setLatitude(String latitude) {this.latitude = latitude;}

    public void setLongitude(String longitude) {this.longitude = longitude;}

    public void setMemo(String memo) {this.memo = memo;}

    public void setNum(int num) {this.num = num;}

    public void setRestaurant(String restaurant) {this.restaurant = restaurant;}

    public void setTime(String time) {this.time = time;}


    public String getLatitude() {return latitude;}

    public String getLongitude() {return longitude;}

    public String getAddress() {return address;}

    public String getRestaurant() {
        return restaurant;
    }

    public int getNum() {
        return num;
    }

    public String getTime() {
        return time;
    }

    public String getMemo() {
        return memo;
    }

    public String getUsername() {return username;}
}
