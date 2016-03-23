package com.example.w28l30.foursquareexample.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Created by XiaowenJiang on 10/30/15.
 */
public class Login extends Activity {
    private static final String TAG = "Login";
    private Intent intent;
    private BroadcastReceiver mBroadcastReceiver;

    SocketService mService;
    boolean mBound = false;
    private EditText username;
    private EditText password;
    private Button LoginBtn;
    private Button SignupBtn;
    //temporary store the input information
    private String username_str;
    private String password_str;
    private String defaultname;
    private SharedPreferences sharedpreferences;
    private String preference = "PREFERENCES";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        intent = getIntent();
        sharedpreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);
        defaultname = intent.getStringExtra(SignUp.msg_name);
        username = (EditText)findViewById(R.id.username_login);
        //if the user just set up a new account, set the default
        //username in the login window to that account name

        password = (EditText)findViewById(R.id.password_login);
        LoginBtn = (Button)findViewById(R.id.loginbtn);
        if(sharedpreferences.contains("username"))
        {
            username.setText(sharedpreferences.getString("username",""));
        }
        if(sharedpreferences.contains("password"))
        {
            password.setText(sharedpreferences.getString("password",""));
        }
        if(defaultname!=null) {
            username.setText(defaultname);
            password.setText("");
        }
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAuthorization())
                {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Username",username_str);
                        jsonObject.put("Password", password_str);
                        jsonObject.put("Command",2);
                        if(mBound&& mService!=null) {
                            try {
                                mService.sendMSG(jsonObject.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
        SignupBtn = (Button)findViewById(R.id.signupbtn);
        SignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUp.class);
                startActivity(intent);
            }
        });
    }

    public boolean checkAuthorization()
    {
        username_str = username.getText().toString();
        password_str = password.getText().toString();
        //username is empty
        if(username_str.equals(""))
        {
            Toast toast = Toast.makeText(getApplicationContext(),"please input username",Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        //password is empty
        else if(password_str.equals(""))
        {
            Toast toast = Toast.makeText(getApplicationContext(),"please input password",Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop is called");
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("bundle")) {
                    Bundle bundle = intent.getBundleExtra("bundle");
                    if(bundle.containsKey("Result")) {
                        try {
                            JSONObject object = new JSONObject(bundle.getString("Result"));
                            String toasttxt = "";
                            int result = object.getInt("result");
                            switch (result){
                                case -1:
                                {
                                    toasttxt = getResources().getString(R.string.login_nouser);
                                    break;
                                }
                                case 0:
                                {
                                    toasttxt = getResources().getString(R.string.login_wrongpassword);
                                    break;
                                }
                                case 1:
                                {
                                    toasttxt = getResources().getString(R.string.login_success);
                                    break;

                                }
                            }
                            Toast toast = Toast.makeText(getApplicationContext(),toasttxt,Toast.LENGTH_SHORT);
                            toast.show();
                            if(result==1) {
                                SharedPreferences.Editor editor=sharedpreferences.edit();
                                editor.putString("username",username_str);
                                editor.putString("password",password_str);
                                editor.commit();
                                Intent tomain = new Intent(getApplicationContext(), MainActivity.class);
                                tomain.putExtra("username", username_str);
                                startActivity(tomain);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
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



}
