package co.introtuce.nex2me.test;

import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.an.deviceinfo.device.model.Battery;
import com.an.deviceinfo.device.model.Device;
import com.an.deviceinfo.device.model.Memory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketCallback;
import com.google.mediapipe.glutil.EglManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import co.introtuce.nex2me.test.analytics.accelerate.AcExternalTextureConverter;
import co.introtuce.nex2me.test.analytics.accelerate.CustomFrameProcessor;
import co.introtuce.nex2me.test.analytics.accelerate.MyCameraXHelper;
import co.introtuce.nex2me.test.analytics.accelerate.MySurfaceTexture;
import co.introtuce.nex2me.test.fileManager.SaveLocal;
import co.introtuce.nex2me.test.ui.videoviews.MyGL2SurfaceView;

import static android.content.Context.MODE_PRIVATE;


public class ModelTestFragment extends Fragment {

    private SurfaceTexture surfaceTexture;
    private CameraHelper cameraHelper;
    MediaPlayer player;
    private SurfaceTexture previewFrameTexture;
    private static CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;

    private MyGL2SurfaceView newSurfaceView;

    public static String BINARY_GRAPH_NAME = "hairsegmentationgpu_new.binarypb";
    //private static final String BINARY_GRAPH_NAME = "hairsegmentationgpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String bGVideoInputStream = "bg_video";
    private static final String x_center = "x_center";
    private static final String y_center = "y_center";
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    public static int TIME;
    public static int C_TIME;

    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private CustomFrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private AcExternalTextureConverter converter;
    private View parent;
    private long runtime;

    String hls = "";

    private TextView textView, sec_remaning;

    FirebaseDatabase database;
    DatabaseReference myRef;
    String modalId;
    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "co.introtuce.nex2me.test";
    long maxr = 0, minr = 1000000, avgr, ffr;

    ArrayList<String> runtimes = new ArrayList<String>();

    ProgressBar progressBar;
    private long c_time;
    private long f_time;
    private Handler handler;

    public static String TEST_NO_ID = " ";
    private boolean isLast = false;
    int counter = 0;

    ModelEventListioner modelEventListioner;


    public ModelEventListioner getModelEventListioner() {
        return modelEventListioner;
    }

    public void setModelEventListioner(ModelEventListioner modelEventListioner) {
        this.modelEventListioner = modelEventListioner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model_test, container, false);
        parent = view;
        textView = view.findViewById(R.id.modelName);
        progressBar = view.findViewById(R.id.progressBar5);
        sec_remaning = view.findViewById(R.id.sec_remaning);
        handler = new Handler();
        database = FirebaseDatabase.getInstance();
        mPreferences = getActivity().getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);

        String uid = mPreferences.getString("u_id", " ");

        myRef = database.getReference(uid);
        modalId = "Model_id " + UUID.randomUUID().toString();

        String modal_number = getmodaalNumber(BINARY_GRAPH_NAME.substring(0, BINARY_GRAPH_NAME.length() - 9));

        textView.setText(modal_number + "/6" + " deep learning models are running");

        try {
            Log.d(TAG, "Initialising graph  " + BINARY_GRAPH_NAME);
            //initMediapipe();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Graph initialization error.");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                modelEventListioner.onModelEnds(BINARY_GRAPH_NAME.substring(0, BINARY_GRAPH_NAME.length() - 9), C_TIME);
            }
        },   6000);
//TIME*60*
        return view;

    }

    private String getmodaalNumber(String binaryGraphName) {
        switch (binaryGraphName) {
            case "small_fp16":
                return 1 + "";
            case "small_fp32":
                return 2 + "";
            case "medium_fp16":
                return 3 + "";
            case "medium_fp32":
                return 4 + "";
            case "large_fp16":
                return 5 + "";
            case "large_fp32":
                return 6 + "";
            default:
                return 0 + "";
        }
    }

    private String getoldGraphName(String binaryGraphName) {
        switch (binaryGraphName) {

            case "small_fp32":
                return "small_fp16";
            case "medium_fp16":
                return "small_fp32";
            case "medium_fp32":
                return "medium_fp16";
            case "large_fp16":
                return "medium_fp32";
            case "large_fp32":
                return "large_fp16";
            case "dummy":
                return "large_fp32";
            default:
                return 0 + "";

        }
    }


//    private void updateMessage() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                while (f_time>c_time)
//                {
//
//                    c_time = System.currentTimeMillis();
//                    updateMess(f_time-c_time/1000+" Seconds Remaning " );
//
////            handler.postDelayed(new Runnable() {
////                @Override
////                public void run() {
////                    c_time = System.currentTimeMillis();
////                    sec_remaning.setText(f_time-c_time/1000+" Seconds Remaning " );
////                }
////            },1000);
//
//                }
//            }
//        },300);
//    }


    private void updateMess(int time, int c_time) {
        String Total = ((time / 1000) / 60) + ((c_time / 1000) / 60) + " ";
        sec_remaning.setText(total + " Minutes");
    }

    private long oldTime = System.currentTimeMillis();

    private void initMediapipe() {
        AndroidAssetUtil.initializeNativeAssetManager(getContext());

        eglManager = new EglManager(null);
        processor =
                new CustomFrameProcessor(
                        getContext(),
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME, bGVideoInputStream, x_center, y_center);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);
        processor.setSwitchseg(true);
        processor.getGraph().addPacketCallback(OUTPUT_VIDEO_STREAM_NAME, new PacketCallback() {
            @Override
            public void process(Packet packet) {
                //Log.d(TAG,"New Output frame");
                long curTime = System.currentTimeMillis();
                runtime = curTime - oldTime;
                oldTime = curTime;

                total_runtime = (total_runtime + runtime) / 2;//(total_runtime+(cur_time-oldTime))/2;
                if (stringBuilder != null) {
                    stringBuilder.append("\nRuntime : " + (runtime));
                }

                newTotal = newTotal + runtime;

                if (runtimes.size() == 0) {
                    //It si First frame
                    ffr = runtime;

                }

                if (maxr < runtime && ffr != runtime) {
                    maxr = runtime;
                }

                if (minr > runtime && ffr != runtime) {
                    minr = runtime;
                }

//                Battery battery = new Battery(getActivity());
//                //Device device = new Device(getActivity());
//                Memory memory = new Memory(getActivity());
//                MainLog logs = new MainLog();
//
//                logs.setCPU_Info(CPUInfoStr);
//                //logs.setDevice(device);
//                logs.setMemory(memory);
//                logs.setBattery(battery);
//                logs.setRunTime(runtime + " ");

                runtimes.add(String.valueOf(runtime));

                Log.d("RUNTIME", runtime + "ms");
                //sec_remaning.setText(" "+runtime+" ");

            }
        });

        PermissionHelper.checkAndRequestCameraPermissions(getActivity());
        //FrameProcessor cf = (FrameProcessor)processor;
        //cf.setbGVideoInputStream(bGVideoInputStream);

    }

    private int mWidth = 0, mHeight = 0;
    public static final String TAG = "ModelTestAct";

    private void newSetupDisplay(View view) {
        Log.d("SURFACE_CREATION", "Creation start");
        newSurfaceView = new MyGL2SurfaceView(getActivity());
        newSurfaceView.setVisibility(View.GONE);
        ViewGroup viewGroup = view.findViewById(R.id.preview_display_layout);
        viewGroup.removeAllViews();
        viewGroup.addView(newSurfaceView);
        MyGL2SurfaceView.CustomSurfaceListner surfaceListner = new MyGL2SurfaceView.CustomSurfaceListner() {
            @Override
            public void onSurfaceChanged(int width, int height) {
                Log.d("SURFACE_CREATION", "Created surface");
                Log.d(TAG, "Setting " + width + " , " + height);

                mHeight = height;
                mWidth = width;

                Size viewSize = new Size(width, height);
                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                converter.setbGSurfaceTextureAndAttachToGLContext(
                        surfaceTexture, displaySize.getWidth(), displaySize.getHeight());
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
                Log.d("ExternalTextureRend", "Setting up");
                Surface surface = new Surface(surfaceTexture);
                processor.getVideoSurfaceOutput().setSurface(surface);

            }
        };
        newSurfaceView.setCustomSurfaceListner(surfaceListner);

    }

    @Override
    public void onResume() {
        Log.d("debug>>","Model Test Fragment Resumed ..");
        super.onResume();
        initMediapipe();
        captureLogs();
        try {
            newSetupDisplay(parent);
        } catch (Exception e) {
            Log.d("EXCEPTION_E", "Main exception " + e.toString());
        }

        play();
        converter = new AcExternalTextureConverter(eglManager.getContext());
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        converter.setFgTimeStamp(processor.fgTimestamp);
        if (PermissionHelper.cameraPermissionsGranted(getActivity())) {
            startCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
        converter.close();
        handler.removeCallbacksAndMessages(null);
        isPause = true;

        //processor.getGraph().cancelGraph();
        processor.close();

        newSurfaceView.pause();

        newSurfaceView.setVisibility(View.GONE);
        //reset or release all surface Views And Resources
       // releaseviews();
        saveLog(BINARY_GRAPH_NAME);
//        super.onDestroyView();
//        super.onDestroy();

    }

    private void releaseviews() {
        handler = null;
        modelEventListioner = null;
        newSurfaceView = null;
        previewFrameTexture = null;
        processor = null;
        eglManager = null;
        parent = null;
        converter = null;
        System.gc();

    }

    private void play() {
        try {
            mediaPlay();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private boolean isPause = false;

    private void mediaPlay() throws IOException {
        //String uriPath = "android.resource://yourapplicationpackage/raw/videofilenamewithoutextension";
        AssetFileDescriptor afd = getActivity().getAssets().openFd("download.mp4");
        surfaceTexture = new MySurfaceTexture(42);
        player = new MediaPlayer();
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());//MediaPlayer.create(getActivity(),filUri);
        player.setSurface(new Surface(surfaceTexture));
        player.setLooping(true);
        player.setVolume(0f, 0f);
        player.prepare();
        player.start();
        Log.d(TAG, "Media player sucess");
    }

    private void stop() {
        try {
            player.stop();
            player.release();
        } catch (Exception e) {
            Log.d("FFF", e.toString());
        }
    }

    private void startCamera() {
        cameraHelper = new MyCameraXHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    previewFrameTexture = surfaceTexture;
                    // Make the display view visible to start showing the preview. This triggers the
                    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
                    Log.d("SURFACE_CREATION", "Visibling now");
                    newSurfaceView.setVisibility(View.VISIBLE);
                    newSurfaceView.resume();
                    Log.d(TAG, "Camera Started");
                });
        cameraHelper.startCamera(getActivity(), CAMERA_FACING, /*surfaceTexture=*/ null);
    }


    private void saveLog(String fileName) {
        try {
            if (stringBuilder != null) {
                //stringBuilder.append("\nAVG Runtime : "+total_runtime);
                stringBuilder.append("\n\n GPU INFO \n");
                stringBuilder.append(newSurfaceView.getGPUInfo());
                captureAdvanceLog("After model ends");
                SaveLocal.saveLogFile(fileName, new String(stringBuilder));
            }


        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    ProcessBuilder processBuilder;
    String Holder = "";
    String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
    InputStream inputStream;
    Process process;
    byte[] byteArry;
    private StringBuilder stringBuilder;

    String CPUInfoStr = " ";

    private void newCpuMeter() {
        byteArry = new byte[1024];
        try {
            processBuilder = new ProcessBuilder(DATA);
            process = processBuilder.start();
            inputStream = process.getInputStream();
            while (inputStream.read(byteArry) != -1) {
                Holder = Holder + new String(byteArry);
            }
            stringBuilder.append(Holder);
            CPUInfoStr = Holder.toString();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //cpu.setText(Holder);
    }

    boolean captureLog = false;
    int total = 0;
    int count = 0;

    long total_runtime = 0l, count_runtime = 0l;
    long newTotal = 0;

    private void captureLogs() {
        if (captureLog) {
            return;
        }
        captureLog = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stringBuilder = new StringBuilder();
                    newCpuMeter();
                    captureAdvanceLog("Before model start");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("LOG_EXP", "exception " + e.toString());
                }
            }
        }).start();
    }

    private void captureAdvanceLog(String title) {
//        stringBuilder.append("\n"+title);
//
//        //Battery
        Battery battery = new Battery(getActivity());
//        stringBuilder.append("\n Battery Percent :"+battery.getBatteryPercent());
//        stringBuilder.append("\n Battery Temperature :"+battery.getBatteryTemperature());
//
//
//        // Device
        Device device = new Device(getActivity());
//        stringBuilder.append("\n Device Model :"+device.getModel());
//        stringBuilder.append("\n Device Info :"+device.getDevice());
//        stringBuilder.append("\n Device brand :"+device.getBuildBrand());


        // Memory
        Memory memory = new Memory(getActivity());
        stringBuilder.append("\n Memory TOTAL RAM :" + memory.getTotalRAM());

        SuperLog finalLog = new SuperLog();

        finalLog.setStatus(title);
        finalLog.setDevice(device);
        finalLog.setMemory(memory);
        finalLog.setBattery(battery);
        finalLog.setCup_info(CPUInfoStr);
        finalLog.setRuns(runtimes);
        finalLog.setFirst_runtime(ffr);
        finalLog.setAverage_runtime(getAvgTime(newTotal, runtimes.size(), ffr));
        finalLog.setMin_runtime(minr);
        finalLog.setMax_runtime(maxr);


        Log.d("Sized>>", runtimes.size() + " ");
        if (title.equalsIgnoreCase("Before model start")) {

        } else {

            String graphName = BINARY_GRAPH_NAME.substring(0, BINARY_GRAPH_NAME.length() - 9);

            // count ++;

//            if (graphName.equalsIgnoreCase("large_fp16")) {
//                graphName = count+" ";
//
//            }

//            String graphName = "default";
//
//            try {
//                graphName = graph_names.get(0);
//
//            }catch (Exception e)
//            {
//                Log.d("Exception>>",e.toString());
//            }


            myRef.child(TEST_NO_ID).child(graphName)
                    .setValue(finalLog).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("Success", "Logs Saved");
                    //Toast.makeText(getContext(), "One Model Tested", Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Exception>>", e.toString());
                }
            });
        }


    }

    private long getAvgTime(long total_runtime, int size, long ffr) {

        long avg;

        if (size > 2) {
            avg = (long) ((total_runtime - ffr) / (size - 1));
            Log.d("Total_time>>", total_runtime + " ");
            Log.d("size>>", size + " ");

        } else {
            avg = 0;
        }

        return avg;
    }


}