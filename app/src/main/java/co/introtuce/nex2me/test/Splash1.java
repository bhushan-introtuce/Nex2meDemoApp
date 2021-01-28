package co.introtuce.nex2me.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.an.deviceinfo.device.model.Device;

import java.util.UUID;

public class Splash1 extends AppCompatActivity {

    //For preferences
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "co.introtuce.nex2me.test";

    TextView tvnext ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash1);
        tvnext = findViewById(R.id.tv_11);

        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);

        if (mPreferences.getBoolean("firstrun", true)) {
            updateUUid();
//            final Handler handler = new Handler();
//
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //Write whatever to want to do after delay specified (1 sec)
//                    Log.d("Handler", "Running Handler");
//                    startActivity(new Intent(Splash1.this,Splash2.class));
//                    finish();
//                }
//            }, 7000);
           // mPreferences.edit().putBoolean("firstrun", false).commit();
        }else {
            startActivity(new Intent(Splash1.this,ModelTestActivity.class));
            finish();
        }

        tvnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Splash1.this,Splash2.class));
                finish();
            }
        });
    }

    private void updateUUid() {
        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);
        Device device = new Device(getApplicationContext());
        String devicestr = " ";
        String brand = " ";

        if(device.getDevice()!=null&&device.getBuildBrand()!=null)
        {
            try {

                for(int i = 0;i<device.getDevice().length();i++)
                {
                    if(device.getModel().charAt(i)=='.')
                    {
                        break;

                    }else {
                        devicestr += device.getModel().charAt(i);
                    }

                }

                for(int i = 0;i<device.getBuildBrand().length();i++)
                {
                    if(device.getBuildBrand().charAt(i)=='.')
                    {
                        break;

                    }else {
                        brand += device.getBuildBrand().charAt(i);
                    }

                }

            }catch (Exception e)
            {
                Log.d("Exception>>",e.toString());
            }

        }


        String uid = UUID.randomUUID().toString()+">>"+brand+
                ">>"+devicestr;
        mPreferences.edit().putString("u_id",uid).commit();
        mPreferences.edit().putBoolean("intrupted",false).commit();

    }
}