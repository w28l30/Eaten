package com.example.w28l30.foursquareexample.Fragment;


import android.app.Fragment;
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
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.w28l30.foursquareexample.Activity.MainActivity;
import com.example.w28l30.foursquareexample.Model.Dinner;
import com.example.w28l30.foursquareexample.Model.JSONUtl;
import com.example.w28l30.foursquareexample.Model.MessageEvent;
import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.SocketService;
import com.example.w28l30.foursquareexample.Service.UpdateLatLngService;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreatedDinnerFragment extends android.support.v4.app.Fragment implements OnRefreshListener {
    private static final String TAG ="CreatedDinnerFragment";
    private ArrayList<Dinner> mDinnerList;
    private SwipeMenuListView mDinnerListView;
    private DinnerListAdapter mAdapter;
    private PullToRefreshLayout mPullToRefreshLayout;

    private double currentLatitude;
    private double currentLongitude;

    private EventBus bus = EventBus.getDefault();

    // added by jxw, broadcastreceiver
    private BroadcastReceiver mBroadcastReceiver;

    private MainActivity activity;
    private SocketService mservice;


    public void onEvent(MessageEvent event)
    {
        LatLng currentLatLng = event.getLatLng();
        currentLatitude = currentLatLng.latitude;
        currentLongitude = currentLatLng.longitude;
    }

    public CreatedDinnerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
        activity = (MainActivity)getActivity();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("bundle")) {
                    Bundle bundle = intent.getBundleExtra("bundle");
                    if(bundle.containsKey("Result")) {
                        try {
                            mDinnerList.clear();
                            JSONArray jsonArray = new JSONArray(bundle.getString("Result"));
                            String toasttxt = "hehe";
                            for(int i = 0;i<jsonArray.length();i++)
                            {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Dinner dinner = JSONUtl.JsonToDinner(object);
                                mDinnerList.add(dinner);
                            }
                            mAdapter.notifyDataSetChanged();
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),toasttxt,Toast.LENGTH_LONG);
                            toast.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
        activity.registerReceiver(mBroadcastReceiver, intentFilter);
        mservice = activity.getService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        activity.unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDinnerListView = (SwipeMenuListView) view.findViewById(R.id.createdDinnerListView);
        mDinnerList = new ArrayList<>();
        mAdapter = new DinnerListAdapter();
        mDinnerListView.setAdapter(mAdapter);
        new UpdateLatLngService(getActivity());

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Join");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);
            }
        };

        mDinnerListView.setMenuCreator(creator);

        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener(this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);
        Thread startrefresh = new Thread(new Runnable() {
            @Override
            public void run() {
                while(currentLongitude ==0.0&&currentLatitude==0.0)
                {

                }
                RefreshList();
            }
        });
        startrefresh.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_created_dinner, container, false);
    }

    @Override
    public void onRefreshStarted(View view) {

        /**
         * Simulate Refresh with 4 seconds sleep
         */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                RefreshList();
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

    class DinnerListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDinnerList.size();
        }

        @Override
        public Dinner getItem(int position) {
            return mDinnerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getActivity(),
                        R.layout.item_list_dinner, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            Dinner item = getItem(position);
            holder.tv_restaurant_address.setText(item.getAddress());
            holder.tv_restaurant.setText(item.getRestaurant());
            holder.tv_num.setText(Integer.toString(item.getNum()));
            holder.tv_time.setText(item.getTime());
            holder.tv_memo.setText(item.getMemo());
            return convertView;
        }

        class ViewHolder {
            TextView tv_restaurant;
            TextView tv_restaurant_address;
            TextView tv_time;
            TextView tv_num;
            TextView tv_memo;

            public ViewHolder(View view) {
                tv_restaurant = (TextView) view.findViewById(R.id.tv_restaurant_name);
                tv_restaurant_address = (TextView) view.findViewById(R.id.tv_restaurant_address);
                tv_time = (TextView) view.findViewById(R.id.tv_time);
                tv_num = (TextView) view.findViewById(R.id.tv_num);
                tv_memo = (TextView) view.findViewById(R.id.tv_memo);
                view.setTag(this);
            }
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void RefreshList(){
        if(mservice!=null&&activity.getmBound())
        {
            JSONObject jsonObject= new JSONObject();

            try {
                jsonObject.put("Command", 6);
                Log.d(TAG,currentLatitude+","+currentLongitude);
                jsonObject.put(getString(R.string.latitude),currentLatitude);
                jsonObject.put(getString(R.string.longitude),currentLongitude);
                mservice.sendMSG(jsonObject.toString());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

        }
    }

}
