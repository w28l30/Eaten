package com.example.w28l30.foursquareexample.Model;

public class FoursquareVenue {
    private String name;
    private String address;
    private String thumbnailUrl;
    private String category;
    private double longitude;
    private double latitude;
    private String price;
    private double rating;
    private double distance;
    private String phone;
    private boolean isOpenNow;

    public FoursquareVenue() {
    }

    public void setAddress(String address) {
        if (address != null) {
            this.address = address.replaceAll("\\(", "").replaceAll("\\)", "");
        }
    }

    public void setIsOpenNow(boolean isOpenNow) {
        this.isOpenNow = isOpenNow;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setlongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getlongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public double getRating() {
        return rating;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPrice() {
        return price;
    }

    public double getDistance() {
        return distance;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isOpenNow() {
        return isOpenNow;
    }
}
