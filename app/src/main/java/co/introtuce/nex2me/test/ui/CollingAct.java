package co.introtuce.nex2me.test.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import co.introtuce.nex2me.test.CoolingFragment;
import co.introtuce.nex2me.test.R;

public class CollingAct extends AppCompatActivity {

    String m_graph_name;
    int cool_time;
    Handler handler;
    TextView endtest, testNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colling);

        m_graph_name = getIntent().getStringExtra("graph_name");
        cool_time = getIntent().getIntExtra("c_time", 0);
        endtest = findViewById(R.id.btn_test_1);
        testNo = findViewById(R.id.tv_test_no);
        String testNum = getIntent().getStringExtra("test_no");

        if(testNum.equalsIgnoreCase("4"))
        {
            testNo.setText(" All tests are running");
        }else {
            testNo.setText("Test " + testNum + " is running");

        }

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
        }, 2000);

    }
}