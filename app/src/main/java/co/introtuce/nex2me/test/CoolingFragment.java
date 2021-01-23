package co.introtuce.nex2me.test;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CoolingFragment extends Fragment {

    String m_graph_name;
    int cool_time;
    Handler handler;

    CollingEventListioner collingEventListioner;


    public String getM_graph_name() {
        return m_graph_name;
    }

    public void setM_graph_name(String m_graph_name) {
        this.m_graph_name = m_graph_name;
    }

    public int getCool_time() {
        return cool_time;
    }

    public void setCool_time(int cool_time) {
        this.cool_time = cool_time;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public CollingEventListioner getCollingEventListioner() {
        return collingEventListioner;
    }

    public void setCollingEventListioner(CollingEventListioner collingEventListioner) {
        this.collingEventListioner = collingEventListioner;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coolng, container, false);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                collingEventListioner.onCollingEnd(m_graph_name, CoolingFragment.this);
            }
        },  2000);
//
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        handler = null;
        collingEventListioner = null;
    }
}
