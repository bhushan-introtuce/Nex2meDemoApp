package co.introtuce.nex2me.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TestDataAdapter extends RecyclerView.Adapter<TestDataAdapter.TestDataAdapterViewHolder> {
    Context context;
    ArrayList<MegaSuperLog> dataList = new ArrayList<>();

    public TestDataAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<MegaSuperLog> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<MegaSuperLog> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public TestDataAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.test_item, parent, false);
        TestDataAdapterViewHolder holder = new TestDataAdapterViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TestDataAdapterViewHolder holder, int position) {

        try {
            if(dataList.get(position).isFrom_all())
            {
                holder.textView.setText("All Tests >> "+dataList.get(position).getTestId().split(">>")[0] + " " +
                        dataList.get(position).getTestId().split(">>")[1]);
            }else {
                holder.textView.setText(dataList.get(position).getTestId().split(">>")[0] + " " +
                        dataList.get(position).getTestId().split(">>")[1]);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showsummary(dataList.get(position));
            }
        });
    }

    private void showsummary(MegaSuperLog superLog) {


        Dialog alertBox = new Dialog(context);
        alertBox.setContentView(R.layout.summary_box);
        TextView data = alertBox.findViewById(R.id.basic_data);
        TextView tv1 = alertBox.findViewById(R.id.tv_m1);
        TextView tv2 = alertBox.findViewById(R.id.tv_m2);
        TextView tv3 = alertBox.findViewById(R.id.tv_m3);
        TextView tv4 = alertBox.findViewById(R.id.tv_m4);
        TextView tv5 = alertBox.findViewById(R.id.tv_m5);
        TextView tv6 = alertBox.findViewById(R.id.tv_m6);


        Button ok = alertBox.findViewById(R.id.btn_ok);


        try {
            String datastr = " Device : " + superLog.getSmall_fp16().getDevice().getModel() +
                    " \n Battery % : " + superLog.getSmall_fp16().getBattery().getBatteryPercent()
                    + " \n Android Version " + superLog.getSmall_fp16().getDevice().getOsVersion()
                    + "\n Battery Temperature : " + superLog.getSmall_fp16().getBattery().getBatteryTemperature() + " C";

            data.setText(datastr);

            if (superLog.getSmall_fp16() != null)
                tv1.setText("Avg. RunTime " + superLog.getSmall_fp16().getAverage_runtime() + " ms");
            if (superLog.getSmall_fp32() != null)
                tv2.setText("Avg. RunTime " + superLog.getSmall_fp32().getAverage_runtime() + " ms");
            if (superLog.getMedium_fp16() != null)
                tv3.setText("Avg. RunTime " + superLog.getMedium_fp16().getAverage_runtime() + " ms");
            if (superLog.getMedium_fp32() != null)
                tv4.setText("Avg. RunTime " + superLog.getMedium_fp32().getAverage_runtime() + " ms");
            if (superLog.getLarge_fp16() != null)
                tv5.setText("Avg. RunTime " + superLog.getLarge_fp16().getAverage_runtime() + " ms");
            if (superLog.getLarge_fp32() != null)
                tv6.setText("Avg. RunTime " + superLog.getLarge_fp32().getAverage_runtime() + " ms");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Exception>>", e.toString());
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        Activity activity = (Activity) context;
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        alertBox.getWindow().setLayout(width - 5, ViewGroup.LayoutParams.MATCH_PARENT);
        alertBox.show();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertBox.dismiss();


            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class TestDataAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TestDataAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_test_name);
        }
    }
}
