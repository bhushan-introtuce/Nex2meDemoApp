package co.introtuce.nex2me.test.rtc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.kinda.alert.KAlertDialog;

import co.introtuce.nex2me.test.R;

public class Nex2meGrowthActivity extends AppCompatActivity {
    public static interface ActiveAlertEventListner{
        public void ok();
        public void cancell();
    }



    public void initToolbar(int toolbarId)
    {
        Toolbar myToolbar = (Toolbar) findViewById(toolbarId);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        assert myToolbar != null;
    }

    private ActiveAlertEventListner activeAlertEventListner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public ActiveAlertEventListner getActiveAlertEventListner() {
        return activeAlertEventListner;
    }

    public void setActiveAlertEventListner(ActiveAlertEventListner activeAlertEventListner) {
        this.activeAlertEventListner = activeAlertEventListner;
    }

    private Dialog dialog;
    protected void showProgress(String msg){
       /* progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(msg);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);*/
        //   progressBar=ProgressDialog.show(this,"","Please Wait",false);
        // setProgress(20);
//        progressBar.show();

        dialog=new Dialog(this);
        dialog.setContentView(R.layout.custom_progress_dialog);
        TextView textView = dialog.findViewById(R.id.msg);
        textView.setText(msg);
        //this.setStyle(R.style.AppModelStyle, R.style.AppBottomSheetDialogTheme);
        dialog.setCancelable(false);
        dialog.show();

    }
    protected void hideProgress(){

        //   progressBar.dismiss();
        try{
            dialog.dismiss();
        }catch (Exception e){

        }
    }

    protected void showActiveAlert(String contentMain,String subtext,
                                   String okLabel,String cancellText){
        new KAlertDialog(this, KAlertDialog.WARNING_TYPE)
                .setTitleText(contentMain)
                .setContentText(subtext)
                .setConfirmText(okLabel)
                .setCancelText(cancellText)
                .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        if(activeAlertEventListner!=null){
                            activeAlertEventListner.ok();
                        }
                        //onDeleteFile();
                    }

                })

                .setCancelClickListener(new KAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        if(activeAlertEventListner!=null){
                            activeAlertEventListner.cancell();
                        }
                    }
                })
                .show();
    }

    protected void showActiveAlert(String contentMain,String subtext,
                                   String okLabel){
        new KAlertDialog(this, KAlertDialog.WARNING_TYPE)
                .setTitleText(contentMain)
                .setContentText(subtext)
                .setConfirmText(okLabel)
                .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        if(activeAlertEventListner!=null){
                            activeAlertEventListner.ok();
                        }
                        //onDeleteFile();
                    }
                })
                .show();
    }

    protected void showErrorAlert(String contentMain,String subtext,
                                  String okLabel){
        new KAlertDialog(this, KAlertDialog.ERROR_TYPE)
                .setTitleText(contentMain)
                .setContentText(subtext)
                .setConfirmText(okLabel)
                .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        if(activeAlertEventListner!=null){
                            activeAlertEventListner.ok();
                        }
                        //onDeleteFile();
                    }
                })

                .show();
    }

    public void onBack(View view) {
        finish();
    }

}
