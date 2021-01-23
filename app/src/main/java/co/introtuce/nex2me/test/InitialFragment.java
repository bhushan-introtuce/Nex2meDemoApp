package co.introtuce.nex2me.test;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;


public class InitialFragment extends Fragment {



    private TextView msg;
    private String customeMessage="";

    ImageView ivInfo;
    private Dialog alertBox;


    public String getCustomeMessage() {
        return customeMessage;
    }

    public void setCustomeMessage(String customeMessage) {
        this.customeMessage = customeMessage;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_initial, container, false);
       msg=view.findViewById(R.id.messageText);
       ivInfo = view.findViewById(R.id.iv_info);

       ivInfo.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               showSuggestions();
           }
       });


       msg.setText(customeMessage);
        return view;
    }

    private void showSuggestions() {
        alertBox = new Dialog(getContext());
        alertBox.setContentView(R.layout.custom_message_box);
        TextView tvOk = alertBox.findViewById(R.id.tv_ok_alert);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        alertBox.getWindow().setLayout(width - 20, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertBox.show();
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBox.dismiss();

            }
        });
    }
}