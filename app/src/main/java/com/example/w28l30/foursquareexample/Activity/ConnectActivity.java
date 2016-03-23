package com.example.w28l30.foursquareexample.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.SocketService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class ConnectActivity extends Activity {
    private static final String TAG ="Main";
    private Button toLoginBtn;
    private EditText ipaddress;
    private EditText portnum;
    private String preference = "PREFERENCES";
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        toLoginBtn = (Button)findViewById(R.id.tologinBtn);
        ipaddress = (EditText)findViewById(R.id.ipaddress);
        portnum = (EditText)findViewById(R.id.portnumber);
        sharedpreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);
        if(!checkPlayServices(this))
        {
            Log.d(TAG, "not available");
        }
        if(sharedpreferences.contains("IPADDRESS"))
        {
            ipaddress.setText(sharedpreferences.getString("IPADDRESS", ""));
        }
        if(sharedpreferences.contains("PORTNUM"))
        {
            portnum.setText(sharedpreferences.getString("PORTNUM", ""));
        }
        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=sharedpreferences.edit();
                editor.putString("IPADDRESS",ipaddress.getText().toString());
                editor.putString("PORTNUM", portnum.getText().toString());
                editor.commit();
                Intent Login = new Intent(getApplicationContext(), Login.class);
                startService(new Intent(getApplicationContext(),SocketService.class));
                Login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(Login);
            }
        });

    }

    private boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }


}
