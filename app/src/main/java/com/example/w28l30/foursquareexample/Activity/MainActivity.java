package com.example.w28l30.foursquareexample.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.w28l30.foursquareexample.Fragment.CreatedDinnerFragment;
import com.example.w28l30.foursquareexample.Fragment.DinnerListFragment;
import com.example.w28l30.foursquareexample.Fragment.FragmentDrawer;
import com.example.w28l30.foursquareexample.Fragment.HomeFragment;
import com.example.w28l30.foursquareexample.Fragment.MapsFragment;
import com.example.w28l30.foursquareexample.Fragment.SearchByAddressFragment;
import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {
    //added by jxw, TAG string
    private static final String TAG = "MainActivity";
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    // added by jxw, username, service args
    private String username;
    SocketService mService;
    boolean mBound = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        Intent intent = getIntent();
        username = intent.getStringExtra(getString(R.string.username));
        //added by jxw, bind main activity to service
        Intent service = new Intent(getApplicationContext(), SocketService.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        // display the first navigation drawer view on app launch
        displayView(0);

    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;
            case 1:
                fragment = new MapsFragment();
                title = getString(R.string.title_Map);
                break;
            case 2:
                fragment = new DinnerListFragment();
                title = getString(R.string.title_NearbyList);
                break;
            case 3:
                fragment = new SearchByAddressFragment();
                title = getString(R.string.title_SearchByLocation);
                break;
            case 4:
                fragment = new CreatedDinnerFragment();
                title = getString(R.string.title_JoinDinner);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");


    }

    public SocketService getService() {
        return mService;
    }

    public boolean getmBound(){
        return mBound;
    }

    @Override
    protected void onPause() {
        //unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop is called");
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
