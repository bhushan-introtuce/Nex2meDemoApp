package co.introtuce.nex2me.test.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import co.introtuce.nex2me.test.CoolingFragment;
import co.introtuce.nex2me.test.R;

public class CollingAct extends AppCompatActivity {

    String m_graph_name;
    int cool_time;
    long cooltime_in_milisec;
    Handler handler;
    TextView endtest, testNo;
    private TextView time_remaning;

    //For preferences
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "co.introtuce.nex2me.test";
    private Thread thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colling);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPreferences = getSharedPreferences(

                sharedPrefFile, MODE_PRIVATE);

        m_graph_name = getIntent().getStringExtra("graph_name");
        cool_time = getIntent().getIntExtra("c_time", 0);
        endtest = findViewById(R.id.btn_test_1);
        testNo = findViewById(R.id.tv_test_no);
        time_remaning = findViewById(R.id.tv_time_rem);
        if (cool_time == 0) {
            cooltime_in_milisec = 1000;
        } else {
            cooltime_in_milisec = cool_time * 60 * 1000;
        }
        String testNum = getIntent().getStringExtra("test_no");

        if (testNum.equalsIgnoreCase("4")) {
            testNo.setText(" All tests are running");
        } else {
            testNo.setText("Test " + testNum + " is running");

        }
        startCountDown(cool_time);

        endtest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                setResult(RESULT_CANCELED, data);
                finish();
            }
        });

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent data = new Intent();
                data.putExtra("graph_name", m_graph_name);
                data.putExtra("c_time", cool_time);

                // data.putExtra("homekey","homename");
                setResult(RESULT_OK, data);
                finish();
            }
        }, cooltime_in_milisec);

    }

    private void startCountDown(int time) {

        long c_time = System.currentTimeMillis();
        long t_time = (long) time * 60000;

        long f_time = c_time + t_time;

//        if (time_remaning.getVisibility() == View.GONE) {
//            time_remaning.setVisibility(View.VISIBLE);
//            time_remaning.setText(time + " minutes remaining");
//        }

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                long d_time = f_time - System.currentTimeMillis();

                                long minutes = (d_time / 1000) / 60;
                                long seconds = (d_time / 1000) % 60;

                                time_remaning.setText("Cooling Device for "+minutes + " minutes " + seconds + " seconds ");
                                if (minutes == 0 && seconds == 1) {
                                    thread.interrupt();
                                    //onEndTest();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Log.d("Exception>>", e.toString());
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(thread!=null)
            {
                if(thread.isAlive())
                    thread.interrupt();

            }

        }catch (Exception e)
        {

        }
        System.gc();

    }
}