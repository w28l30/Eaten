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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.w28l30.foursquareexample.Model.JSONUtl;
import com.example.w28l30.foursquareexample.Model.UserInform;
import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by XiaowenJiang on 10/30/15.
 */
public class SignUp extends Activity {
    private BroadcastReceiver mBroadcastReceiver;
    SocketService mService;
    boolean mBound = false;
    private static final String TAG = "SignUp";
    public static final String msg_name="USER_NAME";
    private String preference = "PREFERENCES";
    private String usr;
    private Intent intent;
    private Button submit;
    private Button cancel;
    private EditText username;
    private EditText name;
    private EditText emailaddress;
    private EditText password;
    private EditText confirmpassword;


    private int gender=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_signup);
        SharedPreferences sharedPreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);


        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        username = (EditText)findViewById(R.id.username_signup);
        name = (EditText)findViewById(R.id.name_signup);
        emailaddress = (EditText)findViewById(R.id.email_signup);
        password = (EditText)findViewById(R.id.password_signup);
        confirmpassword = (EditText)findViewById(R.id.confirmpw);
        submit = (Button) findViewById(R.id.submit_signup);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"submit button clicked");
                usr = username.getText().toString();
                String nametxt = name.getText().toString();
                String emailtxt = emailaddress.getText().toString();
                String pwtxt = password.getText().toString();
                String pwconfirmtxt = confirmpassword.getText().toString();
                if(CheckInfo(usr,nametxt,emailtxt,pwtxt,pwconfirmtxt)) {
                    UserInform userInform = new UserInform(usr,nametxt,emailtxt,gender,5,pwtxt);
                    String jsoninfo = JSONUtl.UserToJson(userInform);
                        Log.d(TAG,"socket");
                        if(mBound&& mService!=null)
                        {
                            try {
                                mService.sendMSG(jsoninfo);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                }
                else{
                    Log.d(TAG,"HEHE");
                }
            }
        });
        cancel = (Button)findViewById(R.id.cancel_signup);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
            }
        });
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
                            String txt = "";
                            int result = object.getInt("result");
                            switch (result) {
                                case 0: {
                                    txt = getString(R.string.signup_user_exists);
                                    break;
                                }
                                case 1: {
                                    txt = getString(R.string.signup_success);
                                    break;
                                }
                                default:
                                    break;
                            }
                            Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
                            toast.show();
                            if (result == 1) {
                                Intent tologin = new Intent(getApplicationContext(), Login.class);
                                tologin.putExtra(msg_name, usr);
                                startActivity(tologin);
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

    public boolean CheckInfo(String usr,String name, String email,String pwtxt,String pwconfirmtxt)
    {
        if(usr.equals(""))
        {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.signup_empty_username),Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else if(name.equals(""))
        {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.signup_empty_name),Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else if(gender==-1)
        {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.signup_empty_gender),Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else if(email.equals(""))
        {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.signup_empty_email),Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else if(pwtxt.equals(""))
        {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.signup_empty_password),Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else if(!pwconfirmtxt.equals(pwtxt))
        {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.signup_pass_notmatch),Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else return true;
    }

    public void GenderCheckboxClicked(View view)
    {
        boolean checked = ((CheckBox)view).isChecked();
        switch (view.getId())
        {
            case R.id.gender_male:
                CheckBox female = (CheckBox)findViewById(R.id.gender_female);
                if(checked)
                {
                   gender = 0;
                    female.setChecked(false);
                }
                else
                {
                    gender = 1;
                    female.setChecked(true);
                }
                break;
            case R.id.gender_female:
                CheckBox male = (CheckBox)findViewById(R.id.gender_male);
                if(checked)
                {
                    gender = 1;
                    male.setChecked(false);
                }
                else
                {
                    gender = 0;
                    male.setChecked(true);
                }
                break;
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

}
