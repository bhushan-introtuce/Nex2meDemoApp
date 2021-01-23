package co.introtuce.nex2me.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import co.introtuce.nex2me.test.rtc.CreateMeetingActivity;
import co.introtuce.nex2me.test.rtc.JoinSessionActivity;

public class RTCHomeActivty extends AppCompatActivity implements View.OnClickListener {

    private TextView liveNow,create,join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_t_c_home_activty);
        inflateUI();
    }
    private void inflateUI(){
        liveNow=findViewById(R.id.live_now);
        create=findViewById(R.id.create);
        join=findViewById(R.id.join);
        liveNow.setOnClickListener(this);
        create.setOnClickListener(this);
        join.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.live_now:
                openMainActivity();
                break;
            case R.id.create:
                openCreateActivity();
                break;
            case R.id.join:
                openJoinActivity();
                break;
        }
    }
    private void openMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    private void openCreateActivity(){
        Intent intent = new Intent(this, CreateMeetingActivity.class);
        startActivity(intent);
    }
    private void openJoinActivity(){
        Intent intent = new Intent(this, JoinSessionActivity.class);
        startActivity(intent);
    }
}