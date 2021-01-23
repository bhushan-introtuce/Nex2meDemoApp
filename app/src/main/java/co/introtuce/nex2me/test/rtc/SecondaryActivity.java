package co.introtuce.nex2me.test.rtc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import co.introtuce.nex2me.test.R;

public class SecondaryActivity extends AppCompatActivity implements SessionLocalCallbacks{
    public static final String SESSION_ID="SESSION_ID";
    public static final String TOKEN_ID="TOKEN_ID";
    public static final  String MEETING_ID="MEETING_ID";
    private FrameLayout container;
    private String sessionid,token,meetingid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);
        container=findViewById(R.id.container);
        if(!validate()){
            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_LONG).show();
            finish();
        }
        openSessionFragment();
    }
    private boolean validate(){
        try{
            sessionid=getIntent().getStringExtra(SESSION_ID);
            token=getIntent().getStringExtra(TOKEN_ID);
            meetingid=getIntent().getStringExtra(MEETING_ID);
            if(sessionid==null || sessionid.isEmpty() || token==null || token.isEmpty()){
                return false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void onFinishRequest() {
        finish();
    }
    private void openSessionFragment(){
        SessionFragment fragment = new SessionFragment();
        fragment.setSessionid(sessionid);
        fragment.setToken(token);
        fragment.setSegmode(true);
        fragment.setMeetingid(meetingid);
        fragment.setSessionLocalCallbacks(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment).addToBackStack(null);
        transaction.commit();
    }
}