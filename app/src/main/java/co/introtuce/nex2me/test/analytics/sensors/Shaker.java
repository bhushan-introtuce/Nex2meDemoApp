package co.introtuce.nex2me.test.analytics.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Shaker implements SensorListener , SensorEventListener {
    public static float xShake = 0.0f;
    public static float yShake = 0.0f;
    public static float zShake = 0.0f;
    public static final float NOISE = 0.01f;
    public static final float OLDMIN = -10f;
    public static final float OLDMAX = 10;
    public static final float NEWMIN = 0;
    public static final float NEWMAX = 1;
    private static final int BASE_FORCE_THRESHOLD = 350;
    private static final int TIME_THRESHOLD = 200;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 1000;
    private static final int SHAKE_COUNT = 3;
    private SensorManager mSensorMgr;
    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;
    private SharedPreferences preferences;
    public Shaker(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences("shakeitoff", context.MODE_PRIVATE);
        resume();
    }
    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }
    public void resume(){
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            mSensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void onAccuracyChanged(int sensor, int accuracy){

    }

    public void pause() {
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            mSensorMgr = null;
        }
    }

    public void onSensorChanged(int sensor, float[] values) {
        if (sensor != SensorManager.SENSOR_ACCELEROMETER) return;
        long now = System.currentTimeMillis();
        //values[SensorManager.DATA_X] = -values[SensorManager.]
        boolean preX =false;
        boolean preY =false;
        float xDiff = (mLastX - values[SensorManager.DATA_X]);
        float yDiff = (mLastY - values[SensorManager.DATA_Y]);
        float zDiff = (mLastZ - values[SensorManager.DATA_Z]);
        float xPixel = (xDiff * 3.7795275591f);
        float yPixel = (yDiff * 3.7795275591f);
        if(xPixel<15){
            xShake = 0f;//-xPixel/100f;
            mLastX = values[SensorManager.DATA_X];
            Log.d(ShakerService.TAG, "X; changing "+xShake);
        }
        else{
            xShake=0;
            mLastX = values[SensorManager.DATA_X];
        }
        if(yPixel<15){
            yShake = 0f;//-yPixel/100f;
            mLastY = values[SensorManager.DATA_Y];
        }
        else{
            yShake = 0;
            mLastY = values[SensorManager.DATA_Y];
        }

        /*if((xDiff <= 0.1f && xDiff>=-0.1f)){
            xShake = 0f;//(((xDiff-OLDMIN)*(NEWMAX-NEWMIN))/(OLDMAX-OLDMIN)+NEWMIN);//(xDiff);
            //Log.d(ShakerService.TAG, "X; "+values[SensorManager.DATA_X] + " Difference : "+xDiff);
            preX=true;
            //zShake = zDiff;
        }
        if(((yDiff >=0.02f && yDiff < 0.2f) || (yDiff <= -0.02f && yDiff > -0.2f)) ){
            yShake = 0f;//yDiff/100f;//(((yDiff-OLDMIN)*(NEWMAX-NEWMIN))/(OLDMAX-OLDMIN)+NEWMIN);
            Log.d(ShakerService.TAG,"Y_Diff: "+yDiff + " yShake: "+yShake);
            preY = true;
           //Log.d(ShakerService.TAG,"Y: "+values[SensorManager.DATA_Y]);
        }
        mLastX = (preX && mLastX != 0.0f)?mLastX:values[SensorManager.DATA_X];
        mLastY = (preY && mLastY != 0.0f)?mLastY:values[SensorManager.DATA_Y];
            //mLastX = values[SensorManager.DATA_X];
            //mLastY = values[SensorManager.DATA_Y];
            mLastZ = values[SensorManager.DATA_Z];*/
        /*if ((now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }
        if ((now - mLastTime) > TIME_THRESHOLD) {
            long diff = now - mLastTime;
            float speed = Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000;
            int sensitivity = preferences.getInt("sensitivity", 2);
            int FORCE_THRESHOLD = BASE_FORCE_THRESHOLD;
            switch (sensitivity) {
                case 0:
                    FORCE_THRESHOLD = BASE_FORCE_THRESHOLD * 4;
                    break;
                case 1:
                    FORCE_THRESHOLD = (int) Math.round(BASE_FORCE_THRESHOLD * 2);
                    break;
            }
            if (speed > FORCE_THRESHOLD) {
                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener != null) {
                        mShakeListener.onShake();
                    }
                }
                mLastForce = now;
            }
            mLastTime = now;
            str = str + " X: "+(speed);// + ", Diff: "+(mLastY-values[SensorManager.RAW_DATA_Y]) ;//+
                   // ", Y: "+(mLastY - values[SensorManager.DATA_Y]) +
                    //", Z: "+(mLastZ - values[SensorManager.DATA_Z]);
            float disff = mLastY-values[SensorManager.RAW_DATA_Y];
            if(disff >= 0.1f || disff <= -0.1f){
               // Log.d("VALUES_AT_DIFF"," Diff Found : "+disff);
            }
            mLastX = values[SensorManager.DATA_X];
            mLastY = values[SensorManager.DATA_Y];
            mLastZ = values[SensorManager.DATA_Z];
            Log.d(ShakerService.TAG,""+str);
        }*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnShakeListener {
        public void onShake();
    }

}