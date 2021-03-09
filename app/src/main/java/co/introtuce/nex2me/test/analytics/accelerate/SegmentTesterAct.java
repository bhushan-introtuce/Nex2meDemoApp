package co.introtuce.nex2me.test.analytics.accelerate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.emrekose.recordbutton.OnRecordListener;
import com.emrekose.recordbutton.RecordButton;
import com.google.mediapipe.components.CameraHelper;

import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketCallback;
import com.google.mediapipe.glutil.EglManager;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.watermark.androidwm_light.bean.WatermarkImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.annotation.Nullable;

import co.introtuce.nex2me.test.R;
import co.introtuce.nex2me.test.VideoDisplayActivity;
import co.introtuce.nex2me.test.analytics.FrameEncoder;
import co.introtuce.nex2me.test.analytics.sensors.Shaker;
import co.introtuce.nex2me.test.analytics.sensors.ShakerService;
import co.introtuce.nex2me.test.fileManager.SaveLocal;
import co.introtuce.nex2me.test.fileManager.StaticFileStorage;
import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;
import co.introtuce.nex2me.test.ui.videoviews.MyGL2SurfaceView;
import co.introtuce.nex2me.test.ui.videoviews.MyGLSurfaceView;
import co.introtuce.nex2me.test.ui.videoviews.RecordableSurfaceView;

import static co.introtuce.nex2me.test.VideoDisplayActivity.OPERATION;
import static co.introtuce.nex2me.test.VideoDisplayActivity.S_DELETE;
import static co.introtuce.nex2me.test.VideoDisplayActivity.S_SAVE;


public class SegmentTesterAct extends AppCompatActivity {
    private Context context;
    private SurfaceTexture surfaceTexture;
    private CameraHelper cameraHelper;
    MediaPlayer player;
    private SurfaceTexture previewFrameTexture;
    private static CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    FrameEncoder encoder;
    private static final String BINARY_GRAPH_NAME = "hairsegmentationgpu.binarypb";
    //private static final String BINARY_GRAPH_NAME = "hairsegmentationgpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String bGVideoInputStream = "bg_video";
    private static final String x_center = "x_center";
    private static final String y_center = "y_center";
    private static final boolean FLIP_FRAMES_VERTICALLY = true;
    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        try {
            System.loadLibrary("opencv_java3");
        } catch (java.lang.UnsatisfiedLinkError e) {
            // Some example apps (e.g. template matching) require OpenCV 4.
            System.loadLibrary("opencv_java4");
        }
    }
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;

    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private CustomFrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private AcExternalTextureConverter converter;

    private AutoFitTextureView videoTexture;
    private TextView runtimetextView;
    private Button switchSeg;

    private boolean mIsRecording;
    private Object content_state;
    private File mOutputFile;
    private Uri contentUri;
    String hls = "";
    private int videoIndex = 0;
    private boolean isClassifying=true;
    private long oldTime=System.currentTimeMillis();

    private MyGL2SurfaceView newSurfaceView;
    private long runtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment_tester);
        this.context = this;
        videoTexture = findViewById(R.id.preview);
        switchSeg=findViewById(R.id.switchSeg);
        switchSeg.setOnClickListener(swithListner);
        videoIndex = getIntent().getIntExtra("VIDEO_INDEX",0);
        try {
            hls = getIntent().getStringExtra("VIDEO_HLS");
        }catch (Exception e){
            e.printStackTrace();
        }
        //setupPreviewDisplayView(videoTexture);
        runtimetextView=findViewById(R.id.runtime);
        //setupPreviewDisplayView(videoTexture);
        previewDisplayView = new SurfaceView(this);
        //setupPreviewDisplayView();
        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(this);

        eglManager = new EglManager(null);
        processor =
                new CustomFrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME,bGVideoInputStream,x_center,y_center);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);
        processor.setSwitchseg(true);
        processor.getGraph().addPacketCallback(OUTPUT_VIDEO_STREAM_NAME, new PacketCallback() {
            @Override
            public void process(Packet packet) {
                long curTime = System.currentTimeMillis();
                runtime = curTime-oldTime;
                oldTime=curTime;

                total_runtime = (total_runtime+runtime)/2;//(total_runtime+(cur_time-oldTime))/2;
                if(stringBuilder != null){
                    stringBuilder.append("\nRuntime : "+(runtime));
                }

                if(processor.isSwitchseg()){
                    //runtime=runtime-20;
                }
                Log.d("RUNTIME",runtime+"ms");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runtimetextView.setText("Runtime : "+runtime+"ms");
                    }
                });
            }
        });
        //FrameProcessor cf = (FrameProcessor)processor;
        //cf.setbGVideoInputStream(bGVideoInputStream);
        PermissionHelper.checkAndRequestCameraPermissions(this);

        renderUi();
    }


    private void startAgain(){
        isPause=false;
        Intent intent = new Intent(this,SegmentTesterAct.class);
        intent.putExtra("VIDEO_INDEX",videoIndex);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        captureLogs();
        if(isPause){
            //startAgain();
        }
        try {
            newSetupDisplay();
        }catch (Exception e){
            Log.d("EXCEPTION_E","Main exception "+e.toString());
        }

        play();
        converter = new AcExternalTextureConverter(eglManager.getContext());
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        converter.setFgTimeStamp(processor.fgTimestamp);
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera();
        }
        if(act_state==STATE_SEGUE){
            onSegDone();
        }
        Log.d(SENSOR_TAG,"onResume");
        //newSurfaceView.resume();
        startSensorService();

    }

   void initNewRecorder(){
       try {
           mOutputFile = createVideoOutputFile();
           android.graphics.Point size = new android.graphics.Point();
           getWindowManager().getDefaultDisplay().getRealSize(size);
           newSurfaceView.initRecorder(mOutputFile, size.x, size.y, new MediaRecorder.OnErrorListener() {
               @Override
               public void onError(MediaRecorder mr, int what, int extra) {
                   Log.d(TAG, "OnError " + what + " ");
               }
           }, new MediaRecorder.OnInfoListener() {
               @Override
               public void onInfo(MediaRecorder mr, int what, int extra) {
                   Log.d(TAG, "OnInfo " +  + what);
               }
           });
           Log.d(TAG,"Size "+size.x+", "+size.y);
       } catch (IOException ioex) {
           Log.e(TAG, "Couldn't re-init recording", ioex);
       }
    }

    private void play(){
        try{
            mediaPlay();
        }catch (Exception e){
            Log.d("FFF",e.toString());
        }
    }
    private boolean isPause = false;
    @Override
    protected void onPause() {
        super.onPause();
        stop();
        converter.close();
        isPause = true;
        newSurfaceView.pause();
        stopSensorService();
        videoTexture.setVisibility(View.GONE);
        newSurfaceView.setVisibility(View.GONE);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //converter.close();
        saveLog("device_log");
        Log.d(SENSOR_TAG,"OnDestroy");
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void mediaPlay(){
        String videoUri = "";
        if(hls!=null && !hls.isEmpty()){
            videoUri=hls;
        }
        else {
            videoUri = StaticFileStorage.getFilePath(videoIndex);
        }
        surfaceTexture = new MySurfaceTexture(42);
        Uri filUri = Uri.parse(videoUri);
        player = MediaPlayer.create(this,filUri);
        player.setSurface(new Surface(surfaceTexture));
        player.setLooping(true);
        player.start();
    }
    private void stop(){
        try{
            player.stop();
        }catch (Exception e){
            Log.d("FFF",e.toString());
        }
    }
    private void togglePlayer(){
        try{
            if(player.isPlaying()){
                player.pause();
            } else {
                player.start();
            }
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }

    public static final String TAG="SegmentTesterAct";
    private SurfaceTexture.OnFrameAvailableListener frameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.d(TAG,"new Frame available");
        }
    };

    private void newSetupDisplay(){
        Log.d("SURFACE_CREATION","Creation start");
        newSurfaceView = new MyGL2SurfaceView(this);
        newSurfaceView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.removeAllViews();
        viewGroup.addView(newSurfaceView);
        MyGL2SurfaceView.CustomSurfaceListner surfaceListner = new MyGL2SurfaceView.CustomSurfaceListner() {
            @Override
            public void onSurfaceChanged(int width, int height) {
                Log.d("SURFACE_CREATION","Created surface");
                Log.d(TAG,"Setting "+width+" , "+height);
                initNewRecorder();
                mHeight=height;
                mWidth=width;
                Size viewSize = new Size(width, height);
                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                converter.setbGSurfaceTextureAndAttachToGLContext(
                        surfaceTexture,displaySize.getWidth(),displaySize.getHeight());
                converter.setSurfaceTextureAndAttachToGLContext(
                        previewFrameTexture, displaySize.getWidth(), displaySize.getHeight());
                //initRecorder();
            }
            @Override
            public void onSurfaceDestroyed() {
                processor.getVideoSurfaceOutput().setSurface(null);
            }
            @Override
            public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
                Log.d("ExternalTextureRend","Setting up");
                Surface surface = new Surface(surfaceTexture);
                processor.getVideoSurfaceOutput().setSurface(surface);
            }
        };
        newSurfaceView.setCustomSurfaceListner(surfaceListner);

    }

    private int mWidth=0,mHeight=0;
    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);
        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }
                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                // (Re-)Compute the ideal size of the camera-preview display (the area that the
                                // camera-preview frames get rendered onto, potentially with scaling and rotation)
                                // based on the size of the SurfaceView that contains the display.
                                mHeight=height;
                                mWidth=width;
                                Size viewSize = new Size(width, height);
                                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);

                                Log.d(TAG,"MP_>start");
                                // Connect the converter to the camera-preview frames as its input (via
                                // previewFrameTexture), and configure the output width and height as the computed
                                // display size.

                                converter.setbGSurfaceTextureAndAttachToGLContext(
                                        surfaceTexture,displaySize.getWidth(),displaySize.getHeight());
                                converter.setSurfaceTextureAndAttachToGLContext(
                                        previewFrameTexture, displaySize.getWidth(), displaySize.getHeight());

                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }
/*
 if(previewFrameTexture==null){
                        previewFrameTexture = surfaceTexture;
                        previewDisplayView.setVisibility(View.VISIBLE);
                    }
                    else {
                        //Path from On Resume

                        previewFrameTexture = surfaceTexture;
                        Size viewSize = new Size(mWidth, mHeight);
                        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                        converter.setSurfaceTextureAndAttachToGLContext(
                                previewFrameTexture, displaySize.getWidth(), displaySize.getHeight());
                    }
 */
    private ImageButton swicthi;
    private FrameLayout recorderSurface;
    private CircularImageView video_img;
    private ImageButton back;
    RecordButton recordButton;
    private void renderUi(){
        video_img = findViewById(R.id.video_img);
        video_img.setVisibility(View.GONE);
        video_img.setOnClickListener(fileChooser);
        recorderSurface = findViewById(R.id.recorderSurface);
        recordButton = (RecordButton) findViewById(R.id.recordBtn);
        recordingSetup();
        swicthi = findViewById(R.id.switchCam);
        swicthi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
    }
    private void switchCamera(){
        cameraHelper = new MyCameraXHelper();
        cameraHelper.setOnCameraStartedListener(new CameraHelper.OnCameraStartedListener() {
            @Override
            public void onCameraStarted(@Nullable SurfaceTexture surfaceTexture) {
                Log.d("MyCameraXHelper","Started Camera "+surfaceTexture);
                previewFrameTexture = surfaceTexture;
                converter.setSurfaceTextureAndAttachToGLContext(surfaceTexture,mWidth,mHeight);
            }
        });
        if(CAMERA_FACING == CameraHelper.CameraFacing.FRONT){
            CAMERA_FACING = CameraHelper.CameraFacing.BACK;
        }
        else if(CAMERA_FACING == CameraHelper.CameraFacing.BACK){
            CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
        }
        cameraHelper.startCamera(this, CAMERA_FACING, /*surfaceTexture=*/ previewFrameTexture);
    }
    private void startCamera() {
        cameraHelper = new MyCameraXHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    previewFrameTexture = surfaceTexture;
                    // Make the display view visible to start showing the preview. This triggers the
                    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
                    videoTexture.setVisibility(View.VISIBLE);
                    Log.d("SURFACE_CREATION","Visibling now");
                    newSurfaceView.setVisibility(View.VISIBLE);
                    newSurfaceView.resume();
                    Log.d(TAG,"Camera Started");
                });
        cameraHelper.startCamera(this, CAMERA_FACING, /*surfaceTexture=*/ null);
    }
    private void setupPreviewDisplayView(AutoFitTextureView textureView){
        textureView.setVisibility(View.GONE);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG,"onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)");
                Surface surface1 = new Surface(surface);
                mHeight=height;
                mWidth=width;
                processor.getVideoSurfaceOutput().setSurface(surface1);
                //converter.setSurfaceTextureAndAttachToGLContext(surfaceTexture,width,height);
                //converter.setbGSurfaceTextureAndAttachToGLContext(previewFrameTexture,width,height);
                converter.setbGSurfaceTextureAndAttachToGLContext(surfaceTexture,width,height);
                converter.setSurfaceTextureAndAttachToGLContext(previewFrameTexture,width,height);
                initRecorder();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(TAG,"onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)");
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG,"oonSurfaceTextureDestroyed(SurfaceTexture surface)");
                processor.getVideoSurfaceOutput().setSurface(null);
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //Log.d(TAG,"onSurfaceTextureUpdated(SurfaceTexture surface)");
            }
        });
    }
    private Bitmap thumbnail;
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
                    intent.putExtra("CONTENT_URI",contentUri);
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
        mglView.setSourceTexture(videoTexture);
        mGLView = mglView;
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mGLView.setZOrderMediaOverlay(true);
        recorderSurface.addView(mGLView);
        initWaterMark();
        Log.d(RECORD_TAG,"Initialization done");
        encoder=new FrameEncoder(videoTexture,context);
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
        /*captureThumbnail();
        mglView.resumeRenderer();
        mglView.resume();

        Log.d(RECORD_TAG,"Recording started");
        try {
            mOutputFile = createVideoOutputFile();
            android.graphics.Point size = new android.graphics.Point();

            mGLView.initRecorder(mOutputFile, size.x, size.y, null, null);
        } catch (IOException ioex) {
            Log.e(RECORD_TAG, "Couldn't re-init recording", ioex);
        }*/

        Log.e(RECORD_TAG, "Starting now");
        startRec();

    }
    private void stopRecording(){
        //mglView.pauseRender();
        //mglView.pause();
        Log.d(RECORD_TAG,"Recording stopped");
        startRec();
    }



    public void startRec(){

        if(mIsRecording){
            newSurfaceView.stopRecording();


            try {

                contentUri = FileProvider.getUriForFile(this,
                        "co.introtuce.nex2me.demo.fileprovider", mOutputFile);

                //share(contentUri);
                onCompleteRecording(contentUri);

            }catch (Exception e)
            {
                //Log.d("Recording_debug>>",e.toString());
            }

            mIsRecording = false;

            mIsRecording = false;
            Log.d(TAG,"File size "+mOutputFile.length()/1024);
            mOutputFile = createVideoOutputFile();
            try {
                android.graphics.Point size = new android.graphics.Point();
                getWindowManager().getDefaultDisplay().getRealSize(size);
                newSurfaceView.initRecorder(mOutputFile, size.x, size.y, new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        Log.d(TAG, "OnError " + what + " extra "+extra);
                        if(what==MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN){
                            Log.d(TAG,"Error MEDIA_RECORDER_ERROR_UNKNOWN ");
                        }
                        else if(what==MediaRecorder.MEDIA_ERROR_SERVER_DIED){
                            Log.d(TAG,"Error MEDIA_ERROR_SERVER_DIED ");
                        }

                    }
                }, new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        Log.d(TAG, "OnInfo " +  + what);
                    }
                });
                //share(contentUri);
                Log.d(TAG,"Size "+size.x+", "+size.y);
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG,e.toString());
            }

        }
        else {
                newSurfaceView.startRecording();
                mIsRecording=true;
        }
        /*Log.d(RECORD_TAG,"TRIGGER_");
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
        }*/
        //encoder.toggle();

    }
    private void share(Uri contentUri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }

    private void onCompleteRecording(Uri contentUri){
        if(thumbnail!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("VIDEO_OUTPUT","Output video is: ");
                    video_img.setImageBitmap(thumbnail);
                    video_img.setVisibility(View.VISIBLE);
                    Log.d("VIDEO_OUTPUT","Size "+mOutputFile.length());
                }
            });
        }
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



   /* private void share(Uri contentUri) {
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



    }*/

    private void captureThumbnail(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                thumbnail = videoTexture.getBitmap();
            }
        }).start();
    }

    private void onBack(){
        try{
            finish();
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }



    //TODO: Application Sensor Service

    public static final String SENSOR_TAG = "ShakerService";
    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (ShakerService.class.getName().equals(
                    service.service.getClassName())) {
                Log.i(SENSOR_TAG, "Service running");
                return true;
            }
        }
        Log.i(SENSOR_TAG, "Service not running");
        return false;
    }

    public void startSensorService(){
        try {
            if (isMyServiceRunning(getApplicationContext())) {
                //stopService(new Intent(this, ShakerService.class));
                // Do Nothing Service Already Started.

            } else {
                Log.d(SENSOR_TAG,"Starting service");
                Shaker.xShake = 0.0f;
                Shaker.yShake = 0.0f;
                Shaker.zShake = 0.0f;
                startService(new Intent(this, ShakerService.class));
            }
        }catch (Exception e){
            Log.d(SENSOR_TAG,e.toString());
        }
    }

    public void stopSensorService(){
        try {
            if (isMyServiceRunning(getApplicationContext())) {
                Log.d(SENSOR_TAG,"Stoping service..");
                Shaker.xShake = 0.0f;
                Shaker.yShake = 0.0f;
                Shaker.zShake = 0.0f;
                stopService(new Intent(this, ShakerService.class));
            } else {
                //startService(new Intent(this, ShakerService.class));
                //Do Nothing Service is not running
            }
        }catch (Exception e){
            Log.d(SENSOR_TAG,e.toString());
        }
    }


    private View.OnClickListener swithListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(processor!=null){
                processor.setSwitchseg(!processor.isSwitchseg());
            }
        }
    };

    ProcessBuilder processBuilder;
    String Holder = "";
    String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
    InputStream inputStream;
    Process process ;
    byte[] byteArry ;
    private StringBuilder stringBuilder;
    private void newCpuMeter(){
        byteArry = new byte[1024];
        try{
            processBuilder = new ProcessBuilder(DATA);
            process = processBuilder.start();
            inputStream = process.getInputStream();
            while(inputStream.read(byteArry) != -1){
                Holder = Holder + new String(byteArry);
            }
            stringBuilder.append(Holder);
            inputStream.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        //cpu.setText(Holder);
    }

    private void saveLog(String fileName){
        try{
            if(stringBuilder!=null){
                //stringBuilder.append("\nAVG Runtime : "+total_runtime);
                stringBuilder.append("\n\n GPU INFO \n");
                stringBuilder.append(newSurfaceView.getGPUInfo());
                SaveLocal.saveLogFile(fileName,new String(stringBuilder));
            }


        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }


    boolean captureLog = false;
    int total = 0;
    int count = 0;
    long total_runtime=0l,count_runtime = 0l;
    private void captureLogs(){
        if(captureLog){
            return;
        }
        captureLog = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stringBuilder = new StringBuilder();
                    newCpuMeter();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("LOG_EXP","exception "+e.toString());
                }
            }
        }).start();
    }
}
