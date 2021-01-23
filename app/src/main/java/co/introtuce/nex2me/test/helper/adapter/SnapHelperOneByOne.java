package co.introtuce.nex2me.test.helper.adapter;


import android.view.View;

import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SnapHelperOneByOne extends LinearSnapHelper {
    private RecyclerView mRecyclerView;


    public RecyclerView getmRecyclerView() {
        return mRecyclerView;
    }

    public void setmRecyclerView(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY){

        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return RecyclerView.NO_POSITION;
        }

        final View currentView = findSnapView(layoutManager);

        if( currentView == null ){
            return RecyclerView.NO_POSITION;
        }

        final int currentPosition = layoutManager.getPosition(currentView);

        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }
        int [] snapDistance=calculateDistanceToFinalSnap(layoutManager,findSnapView(layoutManager));
        if(snapDistance[0]!=0 || snapDistance[1]!=0){
            if(mRecyclerView!=null){
                mRecyclerView.smoothScrollBy(snapDistance[0],snapDistance[1]);
            }
        }
        return currentPosition;
    }
}