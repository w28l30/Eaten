package com.example.w28l30.foursquareexample.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bigkoo.pickerview.OptionsPickerView;
import com.example.w28l30.foursquareexample.AppController;
import com.example.w28l30.foursquareexample.Model.Dinner;
import com.example.w28l30.foursquareexample.Model.JSONUtl;
import com.example.w28l30.foursquareexample.R;
import com.example.w28l30.foursquareexample.Service.SocketService;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SetDinnerActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "SetDinnerActivity";
    private ArrayList<String> options1Items = new ArrayList<String>();

    private String num;
    private String time;
    private String restaurant;
    private String address;
    private String memo;
    private String username;
    private String latitude;
    private String longitude;
    private String thumbnailUrl;
    private String phoneNum;
    private String date;
    private String hrMinSec;

    private TextView tvTime, tvDate, tvName, tvAddress, tvPhone, tvNum;
    private EditText edMemo;
    private Button btnSubmit;

    OptionsPickerView pwOptions;
    Intent intent;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    NetworkImageView thumbNailPicker;

    private Toolbar mToolbar;

    //added by jxw bindservice args
    SocketService mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_view_test);

        mToolbar = (Toolbar) findViewById(R.id.toolbarPicker);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_action_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //added by jxw, bind main activity to service
        Intent service = new Intent(getApplicationContext(), SocketService.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);

        intent = getIntent();
        restaurant = intent.getStringExtra(getString(R.string.restaurant));
        address = intent.getStringExtra(getString(R.string.address));
        username = intent.getStringExtra(getString(R.string.username));
        latitude = intent.getStringExtra(getString(R.string.latitude));
        longitude = intent.getStringExtra(getString(R.string.longitude));
        thumbnailUrl = intent.getStringExtra(getString(R.string.thumbnailUrl));
        phoneNum = intent.getStringExtra(getString(R.string.phoneNum));
        initializeView();
        downloadImage();
    }

    private void downloadImage() {
        thumbNailPicker = (NetworkImageView) findViewById(R.id.thumbnailPicker);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        thumbNailPicker.setImageUrl(thumbnailUrl, imageLoader);

    }

    private void initializeView() {
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvAddress = (TextView) findViewById(R.id.tvShowRestaurantAddress);
        tvName = (TextView) findViewById(R.id.tvShowRestaurantName);
        tvPhone = (TextView) findViewById(R.id.tvShowContact);
        tvNum = (TextView) findViewById(R.id.tvNumOfPeople);
        edMemo = (EditText) findViewById(R.id.edMemo);

        tvName.setText(restaurant);
        tvAddress.setText(address);
        tvPhone.setText(phoneNum);

        Calendar cal = Calendar.getInstance();
        date = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
        hrMinSec = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
        num = "2";

        tvDate.setText(date);
        tvTime.setText(hrMinSec);
        tvNum.setText(num);

        btnSubmit = (Button) findViewById(R.id.btnCreate);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                create();
                finish();
            }
        });


        //弹出时间选择器
        LinearLayout setTime = (LinearLayout) findViewById(R.id.setTime);
        setTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                pwTime.showAtLocation(tvTime, Gravity.BOTTOM, 0, 0, new Date());
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        SetDinnerActivity.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.setThemeDark(true);
                tpd.vibrate(true);
                tpd.dismissOnPause(true);
                tpd.enableSeconds(true);
                tpd.setMinTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
                tpd.setAccentColor(Color.parseColor("#FF80AB"));
                tpd.setTitle("select a certain time");

                tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d("TimePicker", "Dialog was cancelled");
                    }
                });
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });

        //选项选择器
        pwOptions = new OptionsPickerView(this);

        //选项1
        options1Items.add("2");
        options1Items.add("3");
        options1Items.add("4");
        options1Items.add("5");
        options1Items.add("6");
        options1Items.add("7");
        options1Items.add("8");


        //三级联动效果
        pwOptions.setPicker(options1Items);
        //设置选择的三级单位
        pwOptions.setLabels("people");
        //设置默认选中的三级项目
        pwOptions.setSelectOptions(0);
        //监听确定选择按钮
        pwOptions.setCyclic(false);
        pwOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                num = options1Items.get(options1);
                tvNum.setText(num);
            }
        });

        //点击弹出选项选择器
        LinearLayout setNum = (LinearLayout) findViewById(R.id.setNum);
        setNum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pwOptions.show();
            }
        });

        LinearLayout setDate = (LinearLayout) findViewById(R.id.setDate);
        setDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        SetDinnerActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.vibrate(true);
                dpd.dismissOnPause(true);
                dpd.showYearPickerFirst(true);
                dpd.setMinDate(Calendar.getInstance());
                dpd.setAccentColor(Color.parseColor("#FF80AB"));

                dpd.setTitle("Select a certain date");
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
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
        super.onResume();
        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");

        if (tpd != null) tpd.setOnTimeSetListener(this);
        if (dpd != null) dpd.setOnDateSetListener(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picker_view_test, menu);
        return true;
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

//    public static String getTime(Date date) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        return format.format(date);
//    }

    private void create() {
        memo = edMemo.getText().toString();
        restaurant = replacepie(restaurant);
        memo = replacepie(memo);
        Dinner dinner = new Dinner(restaurant, address, Integer.parseInt(num), date + " " + hrMinSec, memo, username, latitude, longitude);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONUtl.DinnerToJson(dinner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mBound && mService != null) {
            try {
                mService.sendMSG(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //dinner.save();
    }

    private String replacepie(String word) {
        char guess = '\'';
        int index = word.indexOf(guess);
        while (index >= 0) {
            System.out.println(index);
            word = word.substring(0, index) + '\\' + word.substring(index, word.length());
            index = word.indexOf(guess, index + 2);
        }
        return word;
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        hrMinSec = hourString + ":" + minuteString + ":" + secondString;
        tvTime.setText(hrMinSec);
    }

    public String getTime() {
        return time;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        date = year +"-"+(monthOfYear+1)+"-"+dayOfMonth;
        tvDate.setText(date);
    }
}
