package com.example.w28l30.foursquareexample.Fragment;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.w28l30.foursquareexample.Activity.MainActivity;
import com.example.w28l30.foursquareexample.Activity.SetDinnerActivity;
import com.example.w28l30.foursquareexample.AppController;
import com.example.w28l30.foursquareexample.Model.FoursquareVenue;
import com.example.w28l30.foursquareexample.Model.MessageEvent;
import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.UpdateLatLngService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends android.support.v4.app.Fragment {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private EventBus bus = EventBus.getDefault();
    // added by jxw, broadcastreceiver
    private BroadcastReceiver mBroadcastReceiver;


    // the foursquare client_id and the client_secret
    private final String CLIENT_ID = "U3VRM3SJ1S3MXCZL0EM0GDFUHK0W2GOTUJVWBIDH3QRVOTHS";
    private final String CLIENT_SECRET = "AFRE3BAJT40TIAJQGG0GDVJUYUG1KVDMAJM2SXJTXB2KYKA3";
    private static String TAG = MapsFragment.class.getSimpleName();

    private ArrayList<FoursquareVenue> mVenueList;
    HashMap<String, HashMap> extraMarkerInfo = new HashMap<String, HashMap>();
    private Toolbar toolbar;

    private double currentLatitude;
    private double currentLongitude;
    private boolean firstResponse = true;


    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_maps, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby_list_and_maps, container, false);
    }


    public void onEvent(MessageEvent event) {
        LatLng currentLatLng = event.getLatLng();
        currentLatitude = currentLatLng.latitude;
        currentLongitude = currentLatLng.longitude;
        if (firstResponse) {
            requestUpdate();
            firstResponse = false;
        }
    }

    private void requestUpdate() {
        mVenueList.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude, currentLongitude)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                + CLIENT_ID
                + "&client_secret="
                + CLIENT_SECRET
                + "&v=20150815&ll="
                + String.valueOf(currentLatitude)
                + ","
                + String.valueOf(currentLongitude)
                + "&section=food"
                + "&venuePhotos=1";

        makeJsonObjectRequest(urlJsonObj);
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap)).getMap();
            mMap.setMyLocationEnabled(true);
            UiSettings uiSettings = mMap.getUiSettings();
            uiSettings.setCompassEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            // Check if we were successful in obtaining the map.
        }
        if (mMap != null) {
            setUpMap();
        }
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new UpdateLatLngService(getActivity());
        setUpMapIfNeeded();
        setHasOptionsMenu(true);
        mVenueList = new ArrayList<FoursquareVenue>();
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                requestUpdate();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("bundle")) {
                    Bundle bundle = intent.getBundleExtra("bundle");
                    if (bundle.containsKey("Result")) {
                        try {
                            JSONObject object = new JSONObject(bundle.getString("Result"));
                            String toasttxt = "";
                            int result = object.getInt("result");
                            Log.d(TAG, result + "");
                            switch (result) {
                                case 0: {
                                    toasttxt = "fail";
                                    break;
                                }
                                case 1: {
                                    toasttxt = "success";
                                    break;
                                }

                            }
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), toasttxt, Toast.LENGTH_LONG);
                            toast.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void makeJsonObjectRequest(String urlJsonObj) {


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                urlJsonObj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    // make an jsonObject in order to parse the response
                    JSONObject jsonObject = response;

                    // make an jsonObject in order to parse the response
                    if (jsonObject.has("response")) {
                        if (jsonObject.getJSONObject("response").has("groups")) {
                            JSONArray jsonArrayGroups = jsonObject.getJSONObject("response").getJSONArray("groups");

                            for (int i = 0; i < jsonArrayGroups.length(); i++) {
                                JSONObject jsonObject1 = jsonArrayGroups.getJSONObject(i);
                                if (jsonObject1.has("items")) {
                                    JSONArray jsonArray1 = jsonObject1.getJSONArray("items");
                                    for (int j = 0; j < jsonArray1.length(); j++) {
                                        FoursquareVenue poi = new FoursquareVenue();
                                        if (jsonArray1.getJSONObject(j).has("venue")) {
                                            poi.setName(jsonArray1.getJSONObject(j).getJSONObject("venue").getString("name"));

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("location")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").has("address")) {
                                                    if (jsonArray1.getJSONObject(j)
                                                            .getJSONObject("venue").getJSONObject("location")
                                                            .has("lat")) {
                                                        poi.setLatitude(jsonArray1
                                                                .getJSONObject(j)
                                                                .getJSONObject("venue").getJSONObject("location")
                                                                .getDouble("lat"));
                                                    }
                                                    if (jsonArray1.getJSONObject(j)
                                                            .getJSONObject("venue").getJSONObject("location")
                                                            .has("lng")) {
                                                        poi.setlongitude(jsonArray1
                                                                .getJSONObject(j)
                                                                .getJSONObject("venue").getJSONObject("location")
                                                                .getDouble("lng"));
                                                    }
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").has("address")) {
                                                        poi.setAddress(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").getString("address"));
                                                    }
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").has("distance")) {
                                                        poi.setDistance(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").getDouble("distance"));
                                                    }
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("contact")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("contact").has("phone")) {
                                                    poi.setPhone(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("contact").getString("formattedPhone"));
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("categories")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").length() > 0) {
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").getJSONObject(0).has("shortName")) {
                                                        poi.setCategory(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").getJSONObject(0).getString("shortName"));
                                                    }
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("hours")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("hours").has("isOpen")) {
                                                    poi.setIsOpenNow(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("hours").getBoolean("isOpen"));
                                                }
                                            }
//
//                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("price")) {
//                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("price").has("currency")) {
//                                                    poi.setPrice(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("price").getString("currency"));
//                                                }
//                                            }
//
//                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("rating")) {
//                                                poi.setRating(jsonArray1.getJSONObject(j).getJSONObject("venue").getDouble("rating"));
//                                            }
//
                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("featuredPhotos")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").has("items")) {
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").getJSONArray("items").getJSONObject(0).has("prefix")) {
                                                        poi.setThumbnailUrl(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").getJSONArray("items").getJSONObject(0).getString("prefix") + "original" + jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").getJSONArray("items").getJSONObject(0).getString("suffix"));
                                                    }
                                                }
                                            }


                                            mVenueList.add(poi);
                                        }
                                    }
                                }

                                addMarker(mVenueList);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void addMarker(ArrayList<FoursquareVenue> list) {


        for (int i = 0; i < list.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions().position(
                    new LatLng(list.get(i).getLatitude(), list.get(i).getlongitude())).title(list.get(i).getName());

            // Changing marker icon
            markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            markerOptions.snippet(list.get(i).getAddress());

            Marker marker = mMap.addMarker(markerOptions);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("distance", String.valueOf(list.get(i).getDistance()));
            if (list.get(i).isOpenNow() == true) {
                map.put("isOpen", "Open");
            } else {
                map.put("isOpen", "Closed");
            }
            map.put("category", list.get(i).getCategory());
            map.put("thumbnailUrl", list.get(i).getThumbnailUrl());
            map.put("phoneNum", list.get(i).getPhone());

            extraMarkerInfo.put(marker.getId(), map);
            // adding marker
        }
        addmarkerClickListener();
    }

    private void addmarkerClickListener() {
        if (mMap != null) {
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    double latitude = marker.getPosition().latitude;
                    double longitude = marker.getPosition().longitude;
                    Intent intent = new Intent(getActivity(), SetDinnerActivity.class);
                    intent.putExtra(getString(R.string.restaurant), marker.getTitle());
                    intent.putExtra(getString(R.string.address), marker.getSnippet());
                    intent.putExtra(getString(R.string.latitude), Double.toString(latitude));
                    intent.putExtra(getString(R.string.longitude), Double.toString(longitude));
                    intent.putExtra(getString(R.string.thumbnailUrl), (String) extraMarkerInfo.get(marker.getId()).get("thumbnailUrl"));
                    intent.putExtra(getString(R.string.phoneNum), (String) extraMarkerInfo.get(marker.getId()).get("phoneNum"));
                    MainActivity activity = (MainActivity) getActivity();
                    intent.putExtra(getString(R.string.username), activity.getUsername());
                    startActivity(intent);
                }
            });
        }
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View view;

        @Override
        public View getInfoWindow(Marker marker) {
            view = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window,
                    null);
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            final TextView title = (TextView) view.findViewById(R.id.infoTitle);
            final TextView snippet = (TextView) view.findViewById(R.id.infoSnippet);
            final TextView isOpenNow = (TextView) view.findViewById(R.id.infoIsOpen);
            final TextView distance = (TextView) view.findViewById(R.id.infoDistance);
            final TextView category = (TextView) view.findViewById(R.id.infoCategroy);
            title.setText(marker.getTitle());
            snippet.setText(marker.getSnippet());
            distance.setText((CharSequence) extraMarkerInfo.get(marker.getId()).get("distance") + "m");
            String openMa = (String) extraMarkerInfo.get(marker.getId()).get("isOpen");
            isOpenNow.setText(openMa);
            if (openMa == "Open") {
                isOpenNow.setTextColor(Color.GREEN);
            } else {
                isOpenNow.setTextColor(Color.RED);
            }
            category.setText((CharSequence) extraMarkerInfo.get(marker.getId()).get("category"));
            return view;
        }
    }


}
