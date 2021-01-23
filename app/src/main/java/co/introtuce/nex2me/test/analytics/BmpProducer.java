package co.introtuce.nex2me.test.analytics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import co.introtuce.nex2me.test.R;

public class BmpProducer extends Thread {

    CustomFrameAvailableListner customFrameAvailableListner;
    public int height = 513,width = 513;
    Bitmap bmp,bg;

    BmpProducer(Context context){
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.blank_img);
        bmp = Bitmap.createScaledBitmap(bmp,480,640,true);
        bg=BitmapFactory.decodeResource(context.getResources(), R.drawable.blank_img);
        bg=Bitmap.createScaledBitmap(bg,480,640,true);
        height = bmp.getHeight();
        width = bmp.getWidth();
        start();
    }

    public void setCustomFrameAvailableListner(CustomFrameAvailableListner customFrameAvailableListner){
        this.customFrameAvailableListner = customFrameAvailableListner;
    }


    public static final String TAG="BmpProducer";

    @Override
    public void run() {
        super.run();
        while ((true)){
            if(bg == null)
                continue;
            if(bmp==null || customFrameAvailableListner == null)
                continue;
            //Log.d(TAG,"Writing frame");
            customFrameAvailableListner.onBGFrame(bg);
            customFrameAvailableListner.onFrame(bmp);

            /*OTMainActivity.imageView.post(new Runnable() {
                @Override
                public void run() {
                    OTMainActivity.imageView.setImageBitmap(bg);
                }
            });*/
            try{
                Thread.sleep(100);
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
        }
    }
}
