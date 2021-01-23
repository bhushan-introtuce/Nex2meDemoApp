package co.introtuce.nex2me.test.rtc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import co.introtuce.nex2me.test.R;
import co.introtuce.nex2me.test.network.ApiClient;
import co.introtuce.nex2me.test.network.ApiInterface;
import co.introtuce.nex2me.test.network.SessionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinSessionActivity extends Nex2meGrowthActivity implements View.OnClickListener {

    private static final String TAG = "JoinSessionActivity";
    private EditText editText;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_session);
        inflateUI();
    }
    private void inflateUI(){
        button = findViewById(R.id.join);
        editText=findViewById(R.id.meeting_id);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String meetingId = editText.getText().toString().trim();
        if(meetingId.isEmpty()){
            editText.setError("Enter meeting id.");
            editText.requestFocus();
            return;
        }
        if(!validateMeetingId(meetingId)){
            editText.setError("Meeting id should be at 5 characters.");
            editText.requestFocus();
            return;
        }
        procceed(meetingId);
    }

    private void procceed(String meetingId){
        showProgress("Please wait...");
        ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
        Call<SessionResponse> call = service.joinSession(meetingId);
        call.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                hideProgress();
                if(response.code()==200){
                    if(response.body().getStatus()==200){
                        onSessionCreate(response.body().getSessionid(),response.body().getToken(),meetingId);
                    }
                    else if(response.body().getStatus()==403){
                        showMessage(response.body().getMessage());
                    }
                }
                else{
                    Log.d(TAG,"Response code "+response.code());
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                hideProgress();
                if(t instanceof IOException){
                    Toast.makeText(getApplicationContext(),"Please check internet connection.",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Something went wrong.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void onSessionCreate(String session,String token,String meetingid){
        Log.d(TAG,"Session_ID "+session);
        Log.d(TAG,"Token: "+token);
        Intent intent = new Intent(this,SecondaryActivity.class);
        intent.putExtra(SecondaryActivity.TOKEN_ID,token);
        intent.putExtra(SecondaryActivity.SESSION_ID,session);
        intent.putExtra(SecondaryActivity.MEETING_ID,meetingid);
        startActivity(intent);
    }
    private void showMessage(String msg){
        showActiveAlert("Nex2me Alert",msg,"Ok");
    }
    private boolean validateMeetingId(String id){
        return id.length()>6;
    }
}