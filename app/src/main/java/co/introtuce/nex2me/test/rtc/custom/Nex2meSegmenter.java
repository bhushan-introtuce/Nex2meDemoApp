package co.introtuce.nex2me.test.rtc.custom;

import android.graphics.Bitmap;

import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;


public interface Nex2meSegmenter {
        public void initMediapipe(AutoFitTextureView videoTexture);
        public void resume();
        public void pause();
        public void setFrameReceiveMode(boolean frameReceiveMode);
        public void onForgroundFrame(Bitmap bitmap);
        public void onBackGroundFrame(Bitmap bitmap);
        public void startGraph() throws NoOutputTextureDefineException;

        static class NoOutputTextureDefineException extends Exception{
                public NoOutputTextureDefineException(String message){
                        super(message);
                }
        }
}
