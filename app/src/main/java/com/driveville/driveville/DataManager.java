package com.driveville.driveville;

import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by arunesh on 11/8/14.
 */
public class DataManager {

    private static final String TAG = DataManager.class.getCanonicalName();

    private String mUserId;
    private String mCarId;
    private String mUserName;
    private DataListener mDataListener;

    public static final double MAX_SPEED = 327.67;
    public static final double MAX_ACCEL = 100;
    public static final double MAX_STEER = 1000;
    public static final double MAX_ENGINE = 12800;

    public DataManager() {

    }

    public DataManager(String userId, String carId, String userName) {
        mUserId = userId;
        mCarId = carId;
        mUserName = userName;
    }

    public void updateUserInfo(String userId, String carId, String userName) {
        mUserId = userId;
        mCarId = carId;
        mUserName = userName;
        if (mDataListener != null) {
            mDataListener.userInfo(mUserId, mCarId, mUserName);
        }
    }

    public double computeAvg(JSONArray array) {
        try {
            double sum = 0.0;
            for (int i = 0; i < array.length(); i++) {
                sum += (double) array.getInt(i);
            }
            sum = sum / array.length();
            return sum;
        } catch (JSONException e) {
            Log.i(TAG, "Unable to parse JSONArray : " + e);
        }
        return 0.0;
    }

    public double computeDev(JSONArray array, double avg) {
        try {
            double tmp = 0.0;
            double stddev = 0.0;
            for (int i = 0; i < array.length(); i++) {
                tmp = (double) array.getInt(i);
                stddev += (tmp - avg) * (tmp - avg);
            }
            stddev /= array.length();
            stddev = Math.sqrt(stddev);
            return stddev;
        } catch (JSONException e) {
            Log.i(TAG, "Unable to parse JSONArray : " + e);
        }
        return 0.0;
    }

    public void updateRealTimeInfo(JSONArray speed, JSONArray alat, JSONArray alng,
                              JSONArray steer_angle, JSONArray accel_rate, JSONArray engine,
                              JSONArray yaw_rate) {
        Double avgSpeed = 0.0, devSpeed = 0.0, avgALat, devALat, avgALng, devALng, avgSteer, devSteer;
        Double avgAccel, devAccel, avgEngine, devEngine, avgYaw, devYaw;
        avgSpeed = computeAvg(speed);
        devSpeed = computeDev(speed, avgSpeed);

        avgALat = computeAvg(alat);
        devALat = computeDev(alat, avgALat);

        avgALng = computeAvg(alng);
        devALng = computeDev(alng, avgALng);

        avgSteer = computeAvg(steer_angle);
        devSteer = computeDev(steer_angle, avgSteer);
        Log.i(TAG, "Avg Steer = " + avgSteer + " Std dev steer = " + devSteer);

        avgAccel = computeAvg(accel_rate);
        devAccel  = computeDev(accel_rate, avgAccel);
        Log.i(TAG, "Avg Accel = " + avgAccel + " Std dev accel = " + devAccel);

        avgEngine = computeAvg(engine);
        devEngine = computeDev(engine, avgEngine);
        Log.i(TAG, "Avg Engine = " + avgEngine + " Std dev engine = " + devEngine);

        avgYaw = computeAvg(yaw_rate);
        devYaw = computeDev(yaw_rate, avgYaw);
        Log.i(TAG, "Avg yaw = " + avgYaw + " Std dev yaw = " + devYaw);

        Log.i(TAG, "Avg Speed = " + avgSpeed + " Std dev speed = " + devSpeed);

        double miles = milesSavedLost(devEngine, avgEngine, devAccel, avgAccel, devSteer, avgSteer);
        if (avgSpeed < 10) {
            miles = 0.0;
        }
        Log.i(TAG, "Total miles saved/lost: " + miles);

        double nAccel = normalizedAccel(avgAccel, devAccel);
        boolean braking = normalizedBraking(speed);
        double nSpeed = normalizedSpeed(avgSpeed, devSpeed);
        mDataListener.realTimeSpeed(miles, nSpeed, nAccel, braking);
    }

    public double getMiles(double avg, double dev) {
        double miles = 0.0;
        double ratio = Math.abs(dev/avg);
        if (ratio > 0.8) return -1.2;
        if (ratio > 0.6) return -0.75;
        if (ratio > 0.4) return -0.5;
        if (ratio < 0.25) return 1.0;
        if (ratio < 0.15) return 0.5;
        if (ratio < 0.2) return 0.0;
        return 0.0;
    }

    public double milesSavedLost(double devEngine, double avgEngine, double devAccel, double avgAccel,
                              double devSteer, double avgSteer) {
        double miles = 0;
        miles += getMiles(avgEngine, devEngine);
        Log.i(TAG, "Engine miles: " + miles);
        double tmp = getMiles(avgAccel, devAccel);
        Log.i(TAG, "Accel miles: " + tmp);
        miles += tmp;
        tmp = getMiles(avgSteer, devSteer) / 0.5;
        Log.i(TAG, "steer miles: " + tmp);
        miles += tmp;
        miles = miles / 3.0;
        return miles;
    }

    public double normalizedAccel(double avgAccel, double devAccel) {
        return avgAccel / MAX_ACCEL;
    }

    public boolean normalizedBraking(JSONArray speed) {
        try {
        return (speed.getInt(0) - speed.getInt(speed.length() - 1)) > 0;
        } catch (JSONException e) {
            Log.i(TAG, "Unable to parse JSONArray : " + e);
        }
        return false;
    }

    public double normalizedSpeed(double avgSpeed, double devSpeed) {
        return avgSpeed / MAX_SPEED;
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    public static interface DataListener {
        void userInfo(String userId, String carId, String userName);
        void realTimeSpeed(double miles, double speed, double accel, boolean braking);
    }

    public String getUserId() {
        return mUserId;
    }

    public String getCarId() {
        return mCarId;
    }

    public String getUserName() {
        return mUserName;
    }
}
