package com.driveville.driveville;

import android.content.Entity;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arunesh on 11/8/14.
 */
public class ServerManager extends HandlerThread {
    private static final String TAG = "ServerManager";
    private static final String DEV_KEY = "60a62edb2da1";
    private static final String URL = "https://api-jp-t-itc.com/";
    private static final String USER_ID = "ITCUS_USERID_076";
    private static final String INFO_ID = "[VehSt10]";
    private static final String REQ_GET_USER_INFO = "GetUserInfo";
    private static final String REQ_GET_CAR_TYPE = "GetVehicleModelList";
    private static final String REQ_GET_VEHICLE_SPEC = "GetVehicleSpec";
    private static final String REQ_GET_VEHICLE_DATA = "GetVehicleData";
    private static final String REQ_GET_VEHICLE_INFO_HS = "GetVehicleInfo";

    private static final String KEY_USER_ID = "userid";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_VEHICLE_ID = "vid";
    private static final String KEY_VEHICLE_INFO = "vehicleinfo";
    private static final String KEY_SPEED = "Spd";
    private static final String KEY_ALAT = "ALat";
    private static final String KEY_ALNG = "ALgt";
    private static final String KEY_YAW_RATE = "YawRate";
    private static final String KEY_ACCEL_RATE = "AccrPedlRat";
    private static final String KEY_STEER_ANGLE = "SteerAg";
    private static final String KEY_ENGINE = "EngN";
    public static final int MSG_GET_USER_INFO = 100;
    public static final int MSG_GET_VEHICLE_INFO_HS = 200;
    private static final int HS_DELAY_MS = 300;

    private HttpClient mHttpClient = new DefaultHttpClient();
    public Handler mHandler;
    public DataManager mDataManager;
    private boolean mListenRealTime = false;

    public ServerManager(DataManager dataManager) {
        super("DriveVille HandlerThread");
        mDataManager = dataManager;
    }

    public void getUserInfoHttpPost() {
        HttpPost httppost = new HttpPost(URL + REQ_GET_USER_INFO);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("developerkey", DEV_KEY));
            nameValuePairs.add(new BasicNameValuePair("responseformat", "json"));
            nameValuePairs.add(new BasicNameValuePair(KEY_USER_ID, USER_ID));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = mHttpClient.execute(httppost);
            String resultString = EntityUtils.toString(response.getEntity());
            Log.i(TAG, "GetUserInfo: " + resultString);
            JSONObject json = new JSONObject(resultString);
            JSONArray vehicleInfo = json.getJSONArray(KEY_VEHICLE_INFO);
            json = vehicleInfo.getJSONObject(0);
            mDataManager.updateUserInfo(json.getString(KEY_USER_ID),
                    json.getString(KEY_VEHICLE_ID), json.getString(KEY_USER_NAME));

        } catch (ClientProtocolException e) {
            Log.i(TAG, "ClientProtocolException:" + e);
        } catch (IOException e) {
            Log.i(TAG, "IOException: " + e);
        } catch (JSONException e) {
            Log.i(TAG, "JSONException: " + e);
        }
    }

    public void getCarType() {
        HttpPost httppost = new HttpPost(URL + REQ_GET_USER_INFO);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("developerkey", DEV_KEY));
            nameValuePairs.add(new BasicNameValuePair("responseformat", "json"));
            nameValuePairs.add(new BasicNameValuePair("userid", USER_ID));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = mHttpClient.execute(httppost);
            Log.i(TAG, "GetUserInfo: " + EntityUtils.toString(response.getEntity()));

        } catch (ClientProtocolException e) {
            Log.i(TAG, "ClientProtocolException:" + e);
        } catch (IOException e) {
            Log.i(TAG, "IOException: " + e);
        }
    }


    public void getVehilcleInfoHs() {
        HttpPost httppost = new HttpPost(URL + REQ_GET_VEHICLE_INFO_HS);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("developerkey", DEV_KEY));
            nameValuePairs.add(new BasicNameValuePair("responseformat", "json"));
            nameValuePairs.add(new BasicNameValuePair("userid", USER_ID));
            nameValuePairs.add(new BasicNameValuePair("infoids", INFO_ID));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = mHttpClient.execute(httppost);
            String resultString = EntityUtils.toString(response.getEntity());
            Log.i(TAG, "GetVehicleInfoHs: " + resultString);
            JSONObject json = new JSONObject(resultString);
            JSONObject vehicleInfo = json.getJSONObject(KEY_VEHICLE_INFO);
            JSONArray speed = vehicleInfo.getJSONArray(KEY_SPEED);
            JSONArray alat = vehicleInfo.getJSONArray(KEY_ALAT);
            JSONArray alng = vehicleInfo.getJSONArray(KEY_ALNG);
            JSONArray steer_angle = vehicleInfo.getJSONArray(KEY_STEER_ANGLE);
            JSONArray accel_rate = vehicleInfo.getJSONArray(KEY_ACCEL_RATE);
            JSONArray engine = vehicleInfo.getJSONArray(KEY_ENGINE);
            JSONArray yawrate = vehicleInfo.getJSONArray(KEY_YAW_RATE);
            Log.i(TAG, "Speed = " + speed.length() + " " + speed.get(0) + " " + speed.get(1));
            mDataManager.updateRealTimeInfo(speed, alat, alng, steer_angle, accel_rate, engine,
                    yawrate);
            if (mListenRealTime) {
                mHandler.sendEmptyMessageDelayed(MSG_GET_VEHICLE_INFO_HS, HS_DELAY_MS);
            }

        } catch (ClientProtocolException e) {
            Log.i(TAG, "ClientProtocolException:" + e);
        } catch (IOException e) {
            Log.i(TAG, "IOException: " + e);
        } catch (JSONException e) {
            Log.i(TAG, "JSONException: " + e);
        }
    }


    @Override
    protected void onLooperPrepared() {
        mHandler = new ServerHandler(getLooper(), this);
        mHandler.sendEmptyMessage(MSG_GET_USER_INFO);
        //mHandler.sendEmptyMessageDelayed(MSG_GET_VEHICLE_INFO_HS, 2000);
    }


    public static class ServerHandler extends Handler {

        private ServerManager mServerManager;
        ServerHandler(Looper looper, ServerManager serverManager) {
            super(looper);
            mServerManager = serverManager;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_GET_USER_INFO:
                    mServerManager.getUserInfoHttpPost();
                    break;
                case MSG_GET_VEHICLE_INFO_HS:
                    mServerManager.getVehilcleInfoHs();
                    break;
            }
        }
    }

    public void stopRealTimeListening() {
        Log.i(TAG, "Trying to stop real-time listening.");
        mHandler.removeMessages(MSG_GET_VEHICLE_INFO_HS);
    }

    public void startRealTimeListening() {
        Log.i(TAG, "Starting real-time listening.");
        mHandler.sendEmptyMessageDelayed(MSG_GET_VEHICLE_INFO_HS, HS_DELAY_MS);
    }

    public void toggleRealTimeListening() {
        if (mListenRealTime) {
            mListenRealTime = false;
            stopRealTimeListening();
        } else {
            mListenRealTime = true;
            startRealTimeListening();
        }
    }
}
