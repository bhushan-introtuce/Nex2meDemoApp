package co.introtuce.nex2me.test.analytics.sensors;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import co.introtuce.nex2me.test.R;
import co.introtuce.nex2me.test.analytics.accelerate.SegmentTesterAct;


/**
 * Created by akiel on 3/15/17.
 */

public class ShakerService extends Service {

    public static final String TAG = "ShakerService";
    private Shaker mShaker;
    //private DevicePolicyManager mDevicePolicyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        //Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(100);
        //mDevicePolicyManager = (DevicePolicyManager)getSystemService(
          //      Context.DEVICE_POLICY_SERVICE);
        Log.d(TAG,"Starting service : onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("servicio", "iniciando");
        Log.d(TAG,"iniciando");
        mShaker = new Shaker(this);
        mShaker.setOnShakeListener(new Shaker.OnShakeListener() {

            @Override
            public void onShake() {
                Toast.makeText(getApplicationContext(),"here", Toast.LENGTH_SHORT);
                Log.d(TAG,"OnShake");
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyit();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mShaker.pause();
        super.onDestroy();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        Log.d(TAG,"Notification channel is creating");
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);

        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);

        return channelId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyit() {
        /*
         * Este método asegura que el servicio permanece en el área de notificación
         * */
        Log.d(TAG,"Notification starting");
        Intent i = new Intent(this, SegmentTesterAct.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        String notificationChannel = createNotificationChannel("shakeitoff", "shakeitoff");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannel);

        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.nextome).setTicker("shake").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.intro)).build();
        startForeground(1337, notification);
    }

}
