package co.introtuce.nex2me.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.emrekose.recordbutton.OnRecordListener;
import com.emrekose.recordbutton.RecordButton;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.glutil.EglManager;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.watermark.androidwm_light.bean.WatermarkImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import co.introtuce.nex2me.test.analytics.BGFrameAvailableListner;
import co.introtuce.nex2me.test.analytics.CustomFrameAvailableListner;
import co.introtuce.nex2me.test.analytics.FrameListner;
import co.introtuce.nex2me.test.analytics.MpExternalTextureConverter;
import co.introtuce.nex2me.test.fileManager.SaveLocal;
import co.introtuce.nex2me.test.fileManager.StaticFileStorage;
import co.introtuce.nex2me.test.helper.VideoPlayerHelper;
import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;
import co.introtuce.nex2me.test.ui.videoviews.MyGLSurfaceView;
import co.introtuce.nex2me.test.ui.videoviews.RecordableSurfaceView;

import static co.introtuce.nex2me.test.VideoDisplayActivity.OPERATION;
import static co.introtuce.nex2me.test.VideoDisplayActivity.S_DELETE;
import static co.introtuce.nex2me.test.VideoDisplayActivity.S_SAVE;


public class ImInActivity extends AppCompatActivity {
    private static final String TAG = "AndroidCameraApi";
    private ImageButton takePictureButton;
    private AutoFitTextureView textureView,bgTexture;
    private ImageView testView;
    RecordButton recordButton;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private boolean isBack=false;
    private SimpleExoPlayerView playerView;
    private VideoPlayerHelper helper;
    private TextureView videoTexture;//Dummy TextureView
    private int videoIndex = 0;
    private boolean isClassifying=true;


    //Mediapipe spesific

    private static final String BINARY_GRAPH_NAME = "segmentationgraph.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static  final String bGVideoInputStream = "bg_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }
    private SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;


    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private MpExternalTextureConverter converter;
    private boolean mIsRecording;
    private Object content_state;
    private File mOutputFile;
    private Uri contentUri;

    private Button captr;
    private FrameLayout recorderSurface;
    private CircularImageView video_img;
    private ImageButton back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_in);
        textureView =  findViewById(R.id.texture);
        testView = findViewById(R.id.testView);
        recordButton = (RecordButton) findViewById(R.id.recordBtn);


        videoIndex = getIntent().getIntExtra("VIDEO_INDEX",0);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        video_img = findViewById(R.id.video_img);
        video_img.setVisibility(View.GONE);
        video_img.setOnClickListener(fileChooser);
        captr = findViewById(R.id.captt);
        captr.setOnClickListener(clickListener);
        bgTexture = findViewById(R.id.mp4texture);
        recorderSurface = findViewById(R.id.recorderSurface);
        takePictureButton = (ImageButton) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takePicture();
                switchCamera();
            }
        });

        previewDisplayView = new SurfaceView(this);
        previewDisplayView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //setupPreviewDisplayView();

        setupPreview();

        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(this);

        eglManager = new EglManager(null);


        processor =
                new FrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        FrameProcessor cf = (FrameProcessor)processor;
        //cf.setbGVideoInputStream(bGVideoInputStream);



        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);

        recordingSetup();

        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

    }

    private void onBack(){
        try{
            finish();
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
            playVideo(videoIndex);
            startService();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(ImInActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startService(){
        //startModelRunningService(this);
    }
    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(ImInActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(ImInActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            if(cameraId == null){
                cameraId = manager.getCameraIdList()[1];
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ImInActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
            fetchFrame();
            /*if(previewDisplayView!=null){
                previewDisplayView.setVisibility(View.VISIBLE);
            }*/

            if(outputTextureView!=null){
                outputTextureView.setVisibility(View.VISIBLE);
                initRecorder();
            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(ImInActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

        if(helper!=null){
            helper.resume();
        }

        if(converter==null){
            initConverter();
        }
        if(act_state==STATE_SEGUE){
            onSegDone();
        }

    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        if(helper!=null){
            helper.puase();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
        destroyFetcher();
    }

    public void switchCamera() {
        isBack=!isBack;
        if (cameraId.equals(CameraCharacteristics.LENS_FACING_FRONT+"")) {
            cameraId = CameraCharacteristics.LENS_FACING_BACK+"";
            closeCamera();
            //reopenCamera();
            Log.d("CAMERA_DEBUG","Location at LENS_FACING");
            openCamera();

            //  switchCameraButton.setImageResource(R.drawable.ic_camera_front);

        } else if (cameraId.equals(CameraCharacteristics.LENS_FACING_BACK+"")) {
            cameraId = CameraCharacteristics.LENS_FACING_FRONT+"";
            closeCamera();
            //reopenCamera();
            // switchCameraButton.setImageResource(R.drawable.ic_camera_back);
            Log.d("CAMERA_DEBUG","Location at BACK_FACING");
            openCamera();
        }
    }

    FrameListner foreground = new FrameListner() {
        @Override
        public void onFrame(final Bitmap frame) {

           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //testView.setImageBitmap(frame);

                }
            });*/
           if(frameAvailableListner!=null){
               frameAvailableListner.onFrame(frame);
           }

        }

        @Override
        public byte getFrameInfo() {
            return 0;
        }
    };

    private FrameListner backGroundListner = new FrameListner() {
        @Override
        public void onFrame(Bitmap frame) {
            if(bgFrameAvailableListner!=null){
                bgFrameAvailableListner.onBGFrame(frame);
            }
        }

        @Override
        public byte getFrameInfo() {
            return 0;
        }
    };

    private boolean isThread=false;
    private  mThread fetcher;
    private void fetchFrame(){
        Log.d("NEW_MODEL_RUNNING","starting -1");
        //startModelRunningService(getActivity());
        if(fetcher==null) {
            fetcher = new mThread(textureView);
            Log.d("BITMAP_EXCEPTION_IN", "starting");
            fetcher.setFrameListner(foreground);
            fetcher.setVideoTexture(bgTexture);
            fetcher.setBackgroundListner(backGroundListner);
            fetcher.startThread();
            new Thread(new Runnable() {
                @Override
                public void run() {
                   /* while(true){
                    SaveBitmap.save(getContext(),Camera2BasicFragment.cm,"cameraFrame",true);

                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){

                    }
                    }*/
                }
            }).start();

        }
        else{
            Log.d("BITMAP_EXCEPTION_IN", "Resuming");
            fetcher.resume();
        }

    }



    private Bitmap bgFrame;
    static class mThread implements Runnable{

        AutoFitTextureView mTextureview;
        TextureView videoTexture;
        Bitmap bitmap;
        ImageView imageView;
        FrameListner frameListner;
        FrameListner backgroundListner;
        private boolean isExit = false;

        public FrameListner getFrameListner() {
            return frameListner;
        }

        public TextureView getVideoTexture() {
            return videoTexture;
        }

        public void setVideoTexture(TextureView videoTexture) {
            this.videoTexture = videoTexture;
        }

        public void setBackgroundListner(FrameListner backgroundListner) {
            this.backgroundListner = backgroundListner;
        }

        public void setFrameListner(FrameListner frameListner) {
            this.frameListner = frameListner;
        }

        private boolean isRunning=false;
        public mThread(AutoFitTextureView mTextureView,ImageView img) {
            this.mTextureview=mTextureView;
            this.imageView=img;
        }
        public mThread(AutoFitTextureView mTextureview){
            this.mTextureview=mTextureview;
        }

        @Override
        public void run() {
            try{
                while (!isExit){
                    try {
                        Log.d("BITMAP_EXCEPTION_IN", "fetching");
                        if (!isRunning) {
                            synchronized (this) {
                                this.wait();
                            }
                        }
                        bitmap = mTextureview.getBitmap();
                        Log.d("BITMAP_EXCEPTION_IN", "" + bitmap.getWidth() + "," + bitmap.getHeight());
                        //Camera2BasicFragment.cm=Bitmap.createBitmap(bitmap);

                        //Bitmap bmp = Bitmap.createScaledBitmap(bitmap, 197, 197, true);


                        //Camera2BasicFragment.bg=fetchVideoFrame();
                        if(frameListner!=null){
                            //Bitmap output=ImgProc.crop_overlay(Camera2BasicFragment.mask,Camera2BasicFragment.bg,Camera2BasicFragment.cm,true);
                            //frameListner.onFrame(output);
                            frameListner.onFrame(bitmap);
                        }


                        if(backgroundListner!=null){
                            backgroundListner.onFrame(fetchVideoFrame());
                        }

                        Thread.sleep(1000/30);
                    }catch (Exception e1){
                        Log.d("BITMAP_EXCEPTION_IN",e1.toString());
                    }
                }

                Log.d(TAG,"Thread:QuiteSafly");
            }catch (Exception e){
                Log.d("BITMAP_EXCEPTION_IN",e.toString());
            }
        }

        private Bitmap fetchVideoFrame(){
            try{
                Bitmap bmp=videoTexture.getBitmap();

                //int start=bmp.getWidth()/(2*480);
                //  bmp=Bitmap.createBitmap(bmp,start,0,513,912);
                Log.d("INCOMING_FRAME",""+bmp.getWidth()+", "+bmp.getHeight());

                return bmp;

            }catch (Exception e){
                Log.d("BITMAP_EXCEPTION_IN",""+e.toString());
                return Bitmap.createBitmap(513,912, Bitmap.Config.ARGB_8888);
            }
        }

        private void resume(){
            if(!isRunning){
                isRunning=true;
                synchronized (this) {
                    this.notify();
                }
            }
        }
        public void pause(){
            if(isRunning){
                isRunning=false;
            }
        }
        public void stopThread(){
            try{
                isExit=true;
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
        }

        private Thread thread;
        public void startThread() {
            if(!isRunning) {
                isRunning=true;
                new Thread(this).start();
            }
        }
    }





    //Play MP4 Video


    public void setupPlayer(int index){


        playerView = findViewById(R.id.video_player);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        helper=new VideoPlayerHelper(this,playerView,player,bgTexture);
        helper.setSource(StaticFileStorage.getFilePath(index));
        //helper.setErrorListner(errorListner);





    }

    public void playWithCheck(int index){
        Log.d(TAG,"File status: "+StaticFileStorage.isFileExist(index));
        if(!StaticFileStorage.isFileExist(index)){
            //downloadFile(index);
            return;
        }


        playVideo(index);
    }

    public void playVideo(int index){

        setupPlayer(index);

        if(helper!=null){
            helper.prep(StaticFileStorage.getFilePath(index));
            videoTexture=(TextureView)playerView.getVideoSurfaceView();
        }
        else {
            setupPlayer(index);
            helper.prep(StaticFileStorage.getFilePath(index));
            videoTexture=(TextureView)playerView.getVideoSurfaceView();
        }

        //render_b = true;

    }

    private void stopFetchingFrame(){
        //stopModelRunningService(getActivity());
        fetcher.pause();
    }

    private void destroyFetcher(){
        fetcher.stopThread();
    }




    //todo: Mediapipe Implementation

    private CustomFrameAvailableListner frameAvailableListner;
    private BGFrameAvailableListner bgFrameAvailableListner;
    private AutoFitTextureView outputTextureView;
    private void setupPreview(){
        outputTextureView = findViewById(R.id.preview);
        outputTextureView.setVisibility(View.GONE);

        outputTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Surface surface1 = new Surface(surface);
                processor.getVideoSurfaceOutput().setSurface(surface1);
                frameAvailableListner = converter;
                bgFrameAvailableListner =converter;


            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });



    }



    public void initConverter(){

        converter = new MpExternalTextureConverter(eglManager.getContext());
        //converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        Log.d(TAG,"convertor created "+processor);

    }


    //todo: Recording implimentation
    private void recordingSetup(){

        recordButton.setRecordListener(new OnRecordListener() {
            @Override
            public void onRecord() {

                if(!mIsRecording){
                    startRecording();
                    //mIsRecording = true;
                }

            }

            @Override
            public void onRecordCancel() {
                Log.e(TAG, "onRecordCancel: ");
               // startRec();
                if(mIsRecording){
                    //mIsRecording=false;
                    stopRecording();
                }

            }

            @Override
            public void onRecordFinish() {

                if(mIsRecording){
                    //mIsRecording = false;
                    stopRecording();
                }




            }
        });

    }

    public static final int CONTENT_STATE_IDLE=1;
    public static final int CONTENT_STATE_VIDEO_RECORDING=2;
    public static final int CONTENT_STATE_VIDEO_STOPED=4;
    public static final int CONTENT_STATE_IMAGE_CAPTRUING=3;


    private void onVideoStopped() {
        share(contentUri);
    }


    private File createVideoOutputFile() {

        File tempFile = null;
        try {
            File dirCheck = new File(
                    this.getFilesDir().getCanonicalPath() + "/" + "captures");

            if (!dirCheck.exists()) {
                dirCheck.mkdirs();
            }

            String filename = new Date().getTime() + "";
            tempFile = new File(
                    this.getFilesDir().getCanonicalPath() + "/" + "captures" + "/"
                            + filename + ".mp4");
        } catch (IOException ioex) {
            Log.e(TAG, "Couldn't create output file", ioex);
        }


        return tempFile;

    }



    private void share(Uri contentUri) {
        Log.d(TAG,"Share invoke");

        try{
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("video/mp4");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_TEXT,"Hey please check *Nex2me* application " + "https://play.google.com/store/apps/details?id=" +getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Share with"));

        }catch (Exception e){
            Log.d(TAG," at share: "+e.toString());
        }



    }




    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(outputTextureView!=null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bmp = outputTextureView.getBitmap();
                        if(bmp==null){
                            Log.d(TAG,"Error: Clound not get output image");
                            return;
                        }
                        try{
                            mOutputFile = createImageOutputFile();
                            FileOutputStream fos=new FileOutputStream(mOutputFile);
                            bmp.compress(Bitmap.CompressFormat.PNG,100,fos);
                            contentUri = FileProvider.getUriForFile(getApplicationContext(),
                                    "co.introtuce.nex2me.demo.fileprovider", mOutputFile);
                            SaveLocal.saveVideo(getApplicationContext(),contentUri);
                            SaveLocal.copyImageFileFromUri(getApplicationContext(),contentUri);
                            Toast.makeText(getApplicationContext(),"Image Saved ",Toast.LENGTH_LONG).show();

                        }catch (Exception e){
                            Log.d(TAG,e.toString());
                        }

                    }
                }).start();

            }
        }
    };

    private Bitmap thumbnail;
    private void captureThumbnail(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                     thumbnail = outputTextureView.getBitmap();
            }
        }).start();
    }

    private File createImageOutputFile() {

        File tempFile = null;
        try {
            File dirCheck = new File(
                    getFilesDir().getCanonicalPath() + "/" + "captures");

            if (!dirCheck.exists()) {
                dirCheck.mkdirs();
            }

            String filename = new Date().getTime() + "";
            tempFile = new File(
                    getFilesDir().getCanonicalPath() + "/" + "captures" + "/"
                            + filename + ".png");
        } catch (IOException ioex) {
            Log.e(TAG, "Couldn't create output file", ioex);
        }

        return tempFile;

    }



    //Recording Helper Properties
    private RecordableSurfaceView mGLView;
    MyGLSurfaceView mglView;


    private String RECORD_TAG="RECORD_TAG";
    public void initRecorder(){
        Log.d(RECORD_TAG,"Initializing");
        if(recorderSurface==null){
            return;
        }
        mglView = new MyGLSurfaceView(this);
        mglView.setSourceTexture(outputTextureView);
        mGLView = mglView;
        mGLView.setZOrderMediaOverlay(true);
        recorderSurface.addView(mGLView);
        initWaterMark();
        Log.d(RECORD_TAG,"Initialization done");
    }


    private void initWaterMark(){
        WatermarkImage watermarkImage = new WatermarkImage(this, R.drawable.nextome)
                .setPositionX(0.68)
                .setPositionY(0.9)
                .setSize(0.3)
                .setImageAlpha(255);
        mglView.setWaterMark(watermarkImage);
    }

    private void startRecording(){


        captureThumbnail();
        mglView.resumeRenderer();
        mglView.resume();

        Log.d(RECORD_TAG,"Recording started");
        try {
            mOutputFile = createVideoOutputFile();
            android.graphics.Point size = new android.graphics.Point();

            mGLView.initRecorder(mOutputFile, size.x, size.y, null, null);
        } catch (IOException ioex) {
            Log.e(RECORD_TAG, "Couldn't re-init recording", ioex);
        }

        Log.e(RECORD_TAG, "Starting now");
        startRec();

    }
    private void stopRecording(){
        mglView.pauseRender();
        mglView.pause();
        Log.d(RECORD_TAG,"Recording stopped");
        startRec();
    }



    public void startRec(){

        Log.d(RECORD_TAG,"TRIGGER_");
        if (mIsRecording) {
            content_state=CONTENT_STATE_VIDEO_STOPED;

            mGLView.stopRecording();
            // fine.setVisibility(View.GONE);
            //onVideoStopped();

            contentUri = FileProvider.getUriForFile(this,
                    "co.introtuce.nex2me.demo.fileprovider", mOutputFile);

                //share(contentUri);
            onCompleteRecording(contentUri);

            mIsRecording = false;

            mIsRecording = false;
            mOutputFile = createVideoOutputFile();

            try {
                int screenWidth = mGLView.getWidth();
                int screenHeight = mGLView.getHeight();
                mGLView.initRecorder(mOutputFile, (int) screenWidth, (int) screenHeight, null,
                        null);
            } catch (IOException ioex) {
                Log.e(RECORD_TAG, "Couldn't re-init recording", ioex);
            }
            //  item.setTitle("Record");
            // start.setText("Start");
        } else {
            mIsRecording = true;
            content_state=CONTENT_STATE_VIDEO_RECORDING;
            mGLView.startRecording();
            Log.v(RECORD_TAG, "Recording Started");
        }

    }

    private void onCompleteRecording(Uri contentUri){

        if(thumbnail!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("VIDEO_OUTPUT","Output video is: ");
                    video_img.setImageBitmap(thumbnail);
                    video_img.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    private int act_state=0;
    public static final int STATE_SEGUE=1;
    public static final int STATE_UNSEGUE=2;
    String path="";
    private View.OnClickListener fileChooser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try{
                if(contentUri!=null){
                    SaveLocal.saveVideo(getApplicationContext(),contentUri);
                     path =  SaveLocal.copyFileFromUri(getApplicationContext(),contentUri);
                    if(path.equals("")){
                        Toast.makeText(getApplicationContext(),"Something went wrng..",Toast.LENGTH_LONG).show();
                        return;
                    }
                    VideoDisplayActivity.thmb = thumbnail;
                    VideoDisplayActivity.contentUri = contentUri;
                    Intent intent=new Intent(getApplicationContext(),VideoDisplayActivity.class);
                    intent.putExtra("PATH_FILE",path);
                    startActivity(intent);
                    act_state = STATE_SEGUE;

                }

            }catch (Exception e){
                Log.d(VideoDisplayActivity.TAG,"At ImInAct: "+e.toString());
            }


        }
    };

    private void onSegDone(){
        Log.d("SEG_ACT","onSegDone");
        if(OPERATION==S_DELETE){
            try{
                File file = new File(path);
                if(file.exists()){
                    file.delete();
                }
            }catch (Exception e){
                Log.d(TAG,"Deleting file : "+e.toString());
            }
        }
        if(OPERATION!=S_SAVE){
           video_img.setVisibility(View.GONE);

        }


    }


}
