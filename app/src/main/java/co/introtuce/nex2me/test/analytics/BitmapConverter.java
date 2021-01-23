package co.introtuce.nex2me.test.analytics;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;



import com.google.mediapipe.components.TextureFrameConsumer;
import com.google.mediapipe.components.TextureFrameProducer;
import com.google.mediapipe.framework.AppTextureFrame;
import com.google.mediapipe.glutil.GlThread;
import com.google.mediapipe.glutil.ShaderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import javax.microedition.khronos.egl.EGLContext;

import co.introtuce.nex2me.test.rtc.custom.CustomFrameProcessor;

public class BitmapConverter implements TextureFrameProducer, CustomFrameAvailableListner  {

    private static final String TAG = "BitmapConverter";
    private static final int DEFAULT_NUM_BUFFERS = 4;
    private static final String THREAD_NAME = "BitmapConverter";

    private RenderThread thread;
    @Override
    public void setConsumer(TextureFrameConsumer next) {
        thread.setConsumer(next);
    }

    public void addConsumer(TextureFrameConsumer consumer) {
        thread.addConsumer(consumer);
    }

    public void removeConsumer(TextureFrameConsumer consumer) {
        thread.removeConsumer(consumer);
    }

    public BitmapConverter(EGLContext parentContext, int numBuffers){
        thread = new RenderThread(parentContext, numBuffers);
        thread.setName(THREAD_NAME);
        thread.start();
        try {
            thread.waitUntilReady();
        } catch (InterruptedException ie) {
            // Someone interrupted our thread. This is not supposed to happen: we own
            // the thread, and we are not going to interrupt it. Therefore, it is not
            // reasonable for this constructor to throw an InterruptedException
            // (which is a checked exception). If it should somehow happen that the
            // thread is interrupted, let's set the interrupted flag again, log the
            // error, and throw a RuntimeException.
            Thread.currentThread().interrupt();
            Log.e(TAG, "thread was unexpectedly interrupted: " + ie.getMessage());
            throw new RuntimeException(ie);
        }
    }
    public void setTimestampOffsetNanos(long offsetInNanos) {
        thread.setTimestampOffsetNanos(offsetInNanos);
    }
    public BitmapConverter(EGLContext parentContext) {
        this(parentContext, DEFAULT_NUM_BUFFERS);
    }

    public void close() {
        if (thread == null) {
            return;
        }
        //thread.getHandler().post(() -> thread.setSurfaceTexture(null, 0, 0));
        thread.quitSafely();
        try {
            thread.join();
        } catch (InterruptedException ie) {
            // Set the interrupted flag again, log the error, and throw a RuntimeException.
            Thread.currentThread().interrupt();
            Log.e(TAG, "thread was unexpectedly interrupted: " + ie.getMessage());
            throw new RuntimeException(ie);
        }
    }

    @Override
    public void onFrame(Bitmap bitmap) {
        if(thread.isAlive()){
            thread.onFrame(bitmap);
        }

    }

    @Override
    public void onBGFrame(Bitmap bitmap) {

        if(thread.isAlive()){
            thread.onBGFrame(bitmap);
        }
    }


    private static class RenderThread extends GlThread implements CustomFrameAvailableListner{
        private static final long NANOS_PER_MICRO = 1000; // Nanoseconds in one microsecond.
        private final List<TextureFrameConsumer> consumers;
        private List<AppTextureFrame> outputFrames = null;
        private int outputFrameIndex = -1;
        private long nextFrameTimestampOffset = 0;
        private long timestampOffsetNanos = 0;
        private long previousTimestamp = 0;
        private Bitmap bitmap, bgBitmap;
        private boolean previousTimestampValid = false;
        protected int destinationWidth = 0;
        protected int destinationHeight = 0;
        public RenderThread(Object parentContext, int numBuffers) {
            super(parentContext);
            outputFrames = new ArrayList<>();
            outputFrames.addAll(Collections.nCopies(numBuffers, null));
            consumers = new ArrayList<>();
        }
        public void setConsumer(TextureFrameConsumer consumer) {
            synchronized (consumers) {
                consumers.clear();
                consumers.add(consumer);
            }
        }

        public void addConsumer(TextureFrameConsumer consumer) {
            synchronized (consumers) {
                consumers.add(consumer);
            }
        }

        public void removeConsumer(TextureFrameConsumer consumer) {
            synchronized (consumers) {
                consumers.remove(consumer);
            }
        }

        @Override
        public void prepareGl() {
            super.prepareGl();

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            //renderer.setup();
        }

        @Override
        public void releaseGl() {
            for (int i = 0; i < outputFrames.size(); ++i) {
                teardownDestination(i);
            }
            //renderer.release();
            super.releaseGl(); // This releases the EGL context, so must do it after any GL calls.
        }

        public void setTimestampOffsetNanos(long offsetInNanos) {
            timestampOffsetNanos = offsetInNanos;
        }

        private void teardownDestination(int index) {
            if (outputFrames.get(index) != null) {
                Log.d(TAG," teardownDestination(int index) : setinuse "+outputFrames.get(index).getInUse());
                waitUntilReleased(outputFrames.get(index));
                GLES20.glDeleteTextures(1, new int[] {outputFrames.get(index).getTextureName()}, 0);
                outputFrames.set(index, null);
            }
        }

        private void setupDestination(int index, int destinationTextureId) {
            Log.d(TAG,"setupDestination  teardownDestination "+index);
            teardownDestination(index);
            Log.d(TAG,"setupDestination  teardownDestination done");
            outputFrames.set(
                    index, new AppTextureFrame(destinationTextureId, destinationWidth, destinationHeight));

        }

        @Override
        public void onFrame(Bitmap bitmap) {
            this.bitmap = bitmap;

            handler.post(() -> renderNext());
        }

        @Override
        public void onBGFrame(Bitmap bitmap) {

            //this.bgBitmap = Bitmap.createScaledBitmap(bitmap,(destinationWidth!=0,destinationHeight, false);
            this.bgBitmap = bitmap;
        }

        protected void renderNext() {
            Log.d(TAG,"RenderNext() ");
            if (bitmap == null) {
                return;
            }
            try {
                Log.d(TAG,"RenderNext() waiting ");
                synchronized (consumers) {
                    Log.d(TAG,"RenderNext() started");
                    boolean frameUpdated = false;
                    if(bgBitmap == null){
                        Log.d(TAG,"BG_STARTUS IS empty");
                        bgBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    }
                    else{
                        bgBitmap = Bitmap.createScaledBitmap(bgBitmap,bitmap.getWidth(),bitmap.getHeight(),true);
                    }
                    Log.d(TAG,"Consumer for ");
                    for (TextureFrameConsumer consumer : consumers) {
                        Log.d(TAG,"RenderNext() starting nextOutputFrame(bitmap);");
                        AppTextureFrame outputFrame = nextOutputFrame(bitmap);
                        Log.d(TAG,"RenderNext() starting nextBGOutputFrame(bitmap);");
                        AppTextureFrame bgOutputframe = nextBGOutputFrame(bgBitmap);
                        Log.d(TAG,"RenderNext() starting update");
                        updateOutputFrame(outputFrame);
                        updateOutputFrame(bgOutputframe,outputFrame.getTimestamp());
                        frameUpdated = true;
                        Log.d(TAG,"Frame updated ");
                        if (consumer != null) {
                            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                                Log.v(
                                        TAG,
                                        String.format(
                                                "Locking tex: %d width: %d height: %d",
                                                outputFrame.getTextureName(),
                                                outputFrame.getWidth(),
                                                outputFrame.getHeight()));
                            }
                            outputFrame.setInUse();
                            bgOutputframe.setInUse();
                            Log.d(TAG,"Frame sending to consumer");
                            CustomFrameProcessor processor = (CustomFrameProcessor)consumer;
                            processor.onNewFrame(outputFrame,bgOutputframe);

                        }
                    }
                    Log.d(TAG,"RenderNext() after for loop ");
                    if (!frameUpdated) {  // Need to update the frame even if there are no consumers.
                        AppTextureFrame outputFrame = nextOutputFrame(bitmap);
                        AppTextureFrame bgOutputFrame = nextBGOutputFrame(bgBitmap);
                        updateOutputFrame(outputFrame);
                        updateOutputFrame(bgOutputFrame,outputFrame.getTimestamp());
                        //updateOutpuFrame(bgOutputFrame,outputFrame.getTimestamp());


                    }
                }
            } finally {
                //bitmap.recycle();
                Log.d(TAG,"RenderNext() Finally ");
            }
        }


    /**
     * NOTE: must be invoked on GL thread
     */
    private AppTextureFrame nextOutputFrame(Bitmap bitmap) {
        int textureName = ShaderUtil.createRgbaTexture(bitmap);
        outputFrameIndex = (outputFrameIndex + 1) % outputFrames.size();
        destinationHeight = bitmap.getHeight();
        destinationWidth = bitmap.getWidth();
        setupDestination(outputFrameIndex, textureName);
        AppTextureFrame outputFrame = outputFrames.get(outputFrameIndex);
        Log.d(TAG,"Waiting for release "+outputFrame.getTextureName());
        waitUntilReleased(outputFrame);
        return outputFrame;
    }

        /**
         * NOTE: must be invoked on GL thread
         */
        private AppTextureFrame nextBGOutputFrame(Bitmap bitmap) {
            Log.d(TAG,"inide nextBGOutputFrame");
            int textureName = ShaderUtil.createRgbaTexture(bitmap);
            Log.d(TAG,"inide create bg frame "+textureName);
            outputFrameIndex = (outputFrameIndex + 1) % outputFrames.size();
            destinationHeight = bitmap.getHeight();
            destinationWidth = bitmap.getWidth();
            Log.d(TAG," Calling setupDestination(outputFrameIndex, textureName)");
            setupDestination(outputFrameIndex, textureName);
            AppTextureFrame outputFrame = outputFrames.get(outputFrameIndex);
            Log.d(TAG,"Waiting for release "+outputFrame.getTextureName());
            waitUntilReleased(outputFrame);
            return outputFrame;
        }

    private long timestamp=1l;
    private void updateOutputFrame(AppTextureFrame outputFrame) {
        // Populate frame timestamp with surface texture timestamp after render() as renderer
        // ensures that surface texture has the up-to-date timestamp. (Also adjust
        // |nextFrameTimestampOffset| to ensure that timestamps increase monotonically.)
        timestamp = timestamp+1;
        long textureTimestamp =
                (timestamp + timestampOffsetNanos) / NANOS_PER_MICRO;
        if (previousTimestampValid
                && textureTimestamp + nextFrameTimestampOffset <= previousTimestamp) {
            nextFrameTimestampOffset = previousTimestamp + 1 - textureTimestamp;
        }
        outputFrame.setTimestamp(textureTimestamp + nextFrameTimestampOffset);
        previousTimestamp = outputFrame.getTimestamp();
        previousTimestampValid = true;
    }

        private void updateOutputFrame(AppTextureFrame outputFrame, long timestamp) {
            // Populate frame timestamp with surface texture timestamp after render() as renderer
            // ensures that surface texture has the up-to-date timestamp. (Also adjust
            // |nextFrameTimestampOffset| to ensure that timestamps increase monotonically.)
            timestamp = timestamp+1;
            long textureTimestamp =
                    (timestamp + timestampOffsetNanos) / NANOS_PER_MICRO;
            if (previousTimestampValid
                    && textureTimestamp + nextFrameTimestampOffset <= previousTimestamp) {
                nextFrameTimestampOffset = previousTimestamp + 1 - textureTimestamp;
            }
            outputFrame.setTimestamp(textureTimestamp + nextFrameTimestampOffset);
            previousTimestamp = outputFrame.getTimestamp();
            previousTimestampValid = true;
        }

    private void waitUntilReleased(AppTextureFrame frame) {
        try {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(
                        TAG,
                        String.format(
                                "Waiting for tex: %d width: %d height: %d",
                                frame.getTextureName(), frame.getWidth(), frame.getHeight()));
            }
            frame.waitUntilReleased();
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(
                        TAG,
                        String.format(
                                "Finished waiting for tex: %d width: %d height: %d",
                                frame.getTextureName(), frame.getWidth(), frame.getHeight()));
            }
        } catch (InterruptedException ie) {
            // Someone interrupted our thread. This is not supposed to happen: we own
            // the thread, and we are not going to interrupt it. If it should somehow
            // happen that the thread is interrupted, let's set the interrupted flag
            // again, log the error, and throw a RuntimeException.
            Thread.currentThread().interrupt();
            Log.e(TAG, "thread was unexpectedly interrupted: " + ie.getMessage());
            throw new RuntimeException(ie);
        }
    }
}

}
