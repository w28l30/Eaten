package com.example.w28l30.foursquareexample.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketService extends Service {
    public String ipaddress;
    private String preference = "PREFERENCES";
    public String portnum;
    private static final String TAG = "MyService";
    private boolean isConnect=false;
    public static Socket socket; //这里定义了一个静态socket 供其他activity使用
    private final IBinder myBinder = new LocalBinder();

    @Override
    public void onCreate(){
        Log.v(TAG, "onCreate");
    }
    @Override
    public IBinder onBind(Intent intent) {

        // TODO Auto-generated method stub
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            System.out.println("I am in Localbinder ");
            return SocketService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        new Thread() {
            @Override
            public void run() {
                if(!isConnect)
                    initSocket();
                Log.d(TAG,"receive msg is called");
                BufferedReader in = null;
                String result ="";
                while(!socket.isClosed()&&socket.isConnected()&&!socket.isOutputShutdown())
                {
                    try {
                        in = new BufferedReader(new InputStreamReader(socket
                                .getInputStream()));
                        result = in.readLine();
                        if(!result.equals(""))
                        {
                            Log.d(TAG,result);
                            Log.d(TAG,"send a broadcast");
                            Bundle bundle = new Bundle();
                            bundle.putString("Result", result);
                            Intent broad = new Intent("android.intent.action.MAIN");
                            broad.putExtra("bundle",bundle);
                            sendBroadcast(broad);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                Log.d(TAG,"socket is closed");
            }
        }.start();
        return START_STICKY;
    }


    private void initSocket() {
        try {
            Log.v(TAG, "initSocket");
            SharedPreferences sharedpreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);
            ipaddress = sharedpreferences.getString("IPADDRESS",null);
            portnum = sharedpreferences.getString("PORTNUM",null);
            socket = new Socket(ipaddress, Integer.parseInt(portnum));
            isConnect=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMSG(String msg) throws IOException {
        Log.d(TAG,"sendMSG is called");

        if (!socket.isClosed()) {
            if (socket.isConnected()) {
                if (!socket.isOutputShutdown()) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                            socket.getOutputStream())), true);
                    out.println(msg);
                }
            }
        }
    }

}
