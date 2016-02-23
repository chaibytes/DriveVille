package com.driveville.driveville;

import com.driveville.driveville.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.res.Configuration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements DataManager.DataListener {
    private static final String TAG = "DriveVille";
    private TextToSpeech tts;



    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION | SystemUiHider.FLAG_FULLSCREEN;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private ServerManager mServerManager;
    private DataManager mDataManager;
    private Handler mHandler;
    double mSpeed = 0.0, mAccel = 0.0, mMiles = 0.0;
    boolean mBraking = false;
    double mTotalMilesSaved = 0.0;

    boolean mTtsReady = false;
    boolean mFiftyMiles = false;
    boolean mHundredMiles = false;

    boolean mDisableVoice = false;

    private static final int MSG_ENABLE_VOICE = 1004;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        // final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        mHandler = new MainHandler();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTtsReady = true;
                }
                else {
                    Log.i(TAG, "TTS failed.");
                }
            }
        });

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
       // mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        //mSystemUiHider.setup();
        //mSystemUiHider
          //      .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
           //         int mControlsHeight;
             //       int mShortAnimTime;

               //     @Override
                //    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                 //   public void onVisibilityChange(boolean visible) {
                   //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                     //       Log.i(TAG, "Hiding...");
                      //      if (mControlsHeight == 0) {
                       //         mControlsHeight = controlsView.getHeight();
                        //    }
                       //     if (mShortAnimTime == 0) {
                        //        mShortAnimTime = getResources().getInteger(
                         //               android.R.integer.config_shortAnimTime);
                          //  }
                          //  controlsView.animate()
                           //         .translationY(visible ? 0 : mControlsHeight)
                            //        .setDuration(mShortAnimTime);
                       // } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                         //   controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                       // }

                       // if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                         //   delayedHide(AUTO_HIDE_DELAY_MILLIS);
                       // }
                   // }
               // });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartStopListener.onClick(view);
                Log.i(TAG, "Ignoring OnClick for now.");
               // mSystemUiHider.hide();
                if (TOGGLE_ON_CLICK) {
                    // mSystemUiHider.toggle();
                } else {
                    // mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        // findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        // delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final View decorView = getWindow().getDecorView();
        final View userPicView = findViewById(R.id.user_pic);
        userPicView.setOnClickListener(mStartStopListener);
        setInvisible(decorView);
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Log.i(TAG, "Visibility changed: " + visibility);
                        if (visibility == 0) {
                            setInvisible(decorView);
                        }
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            setInvisible(decorView);

                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });

        if (mServerManager == null) {
            mDataManager = new DataManager();
            mDataManager.setDataListener(this);
            mServerManager = new ServerManager(mDataManager);
            mServerManager.start();
        }
    }

    private static void setInvisible(View decorView) {
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void userInfo(String userId, String carId, String userName) {
        mHandler.sendEmptyMessage(ServerManager.MSG_GET_USER_INFO);
    }

    @Override
    public void realTimeSpeed(double miles, double speed, double accel, boolean braking) {
        mSpeed = speed;
        mAccel = accel;
        Log.i(TAG, "Setting accel to : " + accel);
        mMiles = miles;
        mBraking = braking;
        mHandler.sendEmptyMessage(ServerManager.MSG_GET_VEHICLE_INFO_HS);

        if (mDisableVoice) {
            return;
        }

        if ((speed * DataManager.MAX_SPEED) > 100.0) {
            if (tts != null && mTtsReady) {
                tts.speak("You are going too fast.", TextToSpeech.QUEUE_ADD, null);
            }
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_VOICE, 5000);
            mDisableVoice = true;
            return;
        } else if (accel * DataManager.MAX_ACCEL > 15.0) {
            if (tts != null && mTtsReady) {
                tts.speak("Aggressive driving warning.", TextToSpeech.QUEUE_ADD, null);
            }
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_VOICE, 5000);
            mDisableVoice = true;
            return;
        }

        if (mMiles > 1.0) {
            if (tts != null && mTtsReady) {
                tts.speak("Good job you are earning miles.", TextToSpeech.QUEUE_ADD, null);
            }
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_VOICE, 5000);
            mDisableVoice = true;
            return;
        }

        if (mTotalMilesSaved > 50 && !mFiftyMiles) {
            if (tts != null && mTtsReady) {
                mFiftyMiles = true;
                tts.speak("Congratulations on reaching 50 miles.", TextToSpeech.QUEUE_ADD, null);
            }
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_VOICE, 5000);
            mDisableVoice = true;
            return;
        }

        if (mTotalMilesSaved > 100 && !mHundredMiles) {
            if (tts != null && mTtsReady) {
                mHundredMiles = true;
                tts.speak("Congratulations on reaching 100 miles.", TextToSpeech.QUEUE_ADD, null);
            }
        }
        mHandler.sendEmptyMessageDelayed(MSG_ENABLE_VOICE, 5000);
        mDisableVoice = true;
    }

    private final class MainHandler extends Handler {
        @Override
        public void handleMessage(Message inputMessage) {
            switch(inputMessage.what) {
                case ServerManager.MSG_GET_USER_INFO:
                    TextView tv = (TextView)findViewById(R.id.user_name);
                    tv.setText("Hello " + mDataManager.getUserName());
                    break;
                case MSG_ENABLE_VOICE:
                    mDisableVoice = false;
                    break;
                case ServerManager.MSG_GET_VEHICLE_INFO_HS:
                    TextView tv1 = (TextView)findViewById(R.id.miles_text);
                    mTotalMilesSaved += mMiles;
                    tv1.setText(String.valueOf((int)mTotalMilesSaved));

                    // Acceleration
                    View v2 = (View)findViewById(R.id.accel);
                    ViewGroup.LayoutParams params = v2.getLayoutParams();
                    params.width = (int)(mAccel * 3000);
                    Log.i(TAG, "mAccel = " + mAccel);
                    Log.i(TAG, "Setting params.width to " + params.width);
                    v2.setLayoutParams(params);
                    if (mBraking) {
                        v2.setBackgroundColor(Color.RED);
                    } else {
                        v2.setBackgroundColor(Color.GREEN);
                    }


                    // Speed.
                    View v3 = (View)findViewById(R.id.speed_bar);
                    ViewGroup.LayoutParams params3 = v3.getLayoutParams();
                    params3.width = (int)(mSpeed * 1500);
                    Log.i(TAG, "mSpeed = " + mSpeed);
                    Log.i(TAG, "Setting params.width to " + params3.width);
                    v3.setLayoutParams(params3);
                    ViewGroup vg = (ViewGroup)findViewById(R.id.rellayout);
                    vg.invalidate();

                    // Overall
                    View v4 = (View)findViewById(R.id.overall_bar);
                    ViewGroup.LayoutParams params4 = v4.getLayoutParams();
                    params4.width = Math.abs((int)(mMiles * 100));
                    v4.setLayoutParams(params4);
                    if (mMiles > 0) {
                        v4.setBackgroundColor(Color.YELLOW);
                    } else {
                        v4.setBackgroundColor(Color.DKGRAY);
                    }
                    v4.invalidate();
                    break;
            }
        }
    }

    View.OnClickListener mStartStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (tts != null && mTtsReady) {
                tts.speak("Welcome to Drive Ville", TextToSpeech.QUEUE_ADD, null);
            }
            mServerManager.toggleRealTimeListening();
        }
    };

}
