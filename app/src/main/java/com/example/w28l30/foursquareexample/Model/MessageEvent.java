package com.example.w28l30.foursquareexample.Model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by W28L30 on 15/10/28.
 */
public class MessageEvent {
    private LatLng latLng;

    public MessageEvent(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
