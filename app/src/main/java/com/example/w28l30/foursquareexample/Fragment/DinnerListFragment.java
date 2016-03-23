package com.example.w28l30.foursquareexample.Fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.w28l30.foursquareexample.Activity.MainActivity;
import com.example.w28l30.foursquareexample.Activity.SetDinnerActivity;
import com.example.w28l30.foursquareexample.Adapter.NearByPlacesAdapter;
import com.example.w28l30.foursquareexample.AppController;
import com.example.w28l30.foursquareexample.Model.FoursquareVenue;
import com.example.w28l30.foursquareexample.Model.MessageEvent;
import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.UpdateLatLngService;
import com.google.android.gms.maps.model.LatLng;
import com.kyleduo.switchbutton.SwitchButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class DinnerListFragment extends android.support.v4.app.Fragment implements OnRefreshListener {

    private EventBus bus = EventBus.getDefault();
    private ProgressDialog pDialog;

    // the foursquare client_id and the client_secret
    private final String CLIENT_ID = "U3VRM3SJ1S3MXCZL0EM0GDFUHK0W2GOTUJVWBIDH3QRVOTHS";
    private final String CLIENT_SECRET = "AFRE3BAJT40TIAJQGG0GDVJUYUG1KVDMAJM2SXJTXB2KYKA3";
    private static String TAG = DinnerListFragment.class.getSimpleName();

    private NearByPlacesAdapter mAdapter;
    private SwipeMenuListView mListView;
    private ArrayList<FoursquareVenue> mVenueList;
    private PullToRefreshLayout mPullToRefreshLayout;

    private double currentLatitude;
    private double currentLongitude;

    // added by jxw, broadcastreceiver
    private BroadcastReceiver mBroadcastReceiver;

    private SwitchButton switchButtonOpen;
    private SwitchButton switchButtonSort;

    private boolean isOpen = false;
    private boolean isSort = false;
    private boolean firstResponse = true;


    public DinnerListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dinner_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new UpdateLatLngService(getActivity());

        mListView = (SwipeMenuListView) view.findViewById(R.id.listView);
        mVenueList = new ArrayList<FoursquareVenue>();
        mAdapter = new NearByPlacesAdapter(getContext(), mVenueList);
        mListView.setAdapter(mAdapter);

        pDialog = new ProgressDialog(getActivity());
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        switchButtonOpen = (SwitchButton) view.findViewById(R.id.switchBtnOpenNow);
        switchButtonSort = (SwitchButton) view.findViewById(R.id.switchBtnSortByDistance);

        switchButtonOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isOpen = true;
                } else {
                    isOpen = false;
                }
                requestUpdate();
            }
        });

        switchButtonSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSort = true;
                } else {
                    isSort = false;
                }
                requestUpdate();
            }
        });


        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu swipeMenu) {
                // create "Add" item
                SwipeMenuItem addItem = new SwipeMenuItem(
                        getContext());
                // set item background
                addItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                addItem.setWidth(dp2px(90));
                addItem.setTitleColor(Color.WHITE);
                addItem.setTitleSize(18);
                addItem.setTitle("Add");
                // add to menu
                swipeMenu.addMenuItem(addItem);
            }
        };

        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                FoursquareVenue item = mVenueList.get(position);
                switch (index) {
                    case 0:
                        // add
                        Intent intent = new Intent(getActivity(), SetDinnerActivity.class);
                        MainActivity activity = (MainActivity) getActivity();
                        intent.putExtra("username", activity.getUsername());
                        intent.putExtra("restaurant", item.getName());
                        intent.putExtra("address", item.getAddress());
                        intent.putExtra("latitude", Double.toString(item.getLatitude()));
                        intent.putExtra("longitude", Double.toString(item.getlongitude()));
                        intent.putExtra("thumbnailUrl", item.getThumbnailUrl());
                        intent.putExtra("phoneNum", item.getPhone());
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });

        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_List);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener(this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

    }

    private int booleanToInt(boolean b) {
        return (b) ? 1 : 0;
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

        String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                + CLIENT_ID
                + "&client_secret="
                + CLIENT_SECRET
                + "&v=20150815&ll="
                + String.valueOf(currentLatitude)
                + ","
                + String.valueOf(currentLongitude)
                + "&section=food"
                + "&venuePhotos=1"
                + "&openNow="
                + booleanToInt(isOpen)
                + "&sortByDistance="
                + booleanToInt(isSort);

        makeJsonObjectRequest(urlJsonObj);
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
        hidePDialog();
    }

    private void makeJsonObjectRequest(String urlJsonObj) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                urlJsonObj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                hidePDialog();
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
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").getJSONObject(0).has("icon")) {
                                                        poi.setCategory(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").getJSONObject(0).getString("name"));
                                                    }
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("price")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("price").has("message")) {
                                                    poi.setPrice(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("price").getString("message"));
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("rating")) {
                                                poi.setRating(jsonArray1.getJSONObject(j).getJSONObject("venue").getDouble("rating"));
                                            }

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

                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }


    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onRefreshStarted(View view) {
        /**
         * Simulate Refresh with 4 seconds sleep
         */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(4000);
                    requestUpdate();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);


                // Notify PullToRefreshLayout that the refresh has finished
                mPullToRefreshLayout.setRefreshComplete();

            }
        }.execute();
    }
}
