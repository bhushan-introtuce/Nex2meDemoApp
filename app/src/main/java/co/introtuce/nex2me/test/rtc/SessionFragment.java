package co.introtuce.nex2me.test.rtc;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import co.introtuce.nex2me.test.R;
import co.introtuce.nex2me.test.VideoDisplayActivity;
import co.introtuce.nex2me.test.fileManager.SaveLocal;
import co.introtuce.nex2me.test.helper.BitmapToVideoEncoder;
import co.introtuce.nex2me.test.network.ApiClient;
import co.introtuce.nex2me.test.network.ApiInterface;
import co.introtuce.nex2me.test.network.SessionResponse;
import co.introtuce.nex2me.test.rtc.custom.CustomVideoCapturer;
import co.introtuce.nex2me.test.rtc.custom.FrameListner;
import co.introtuce.nex2me.test.rtc.custom.InvertedColorsVideoRenderer;
import co.introtuce.nex2me.test.rtc.custom.MediapipeController;
import co.introtuce.nex2me.test.rtc.custom.Nex2meSegmenter;
import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.webrtc.ContextUtils.getApplicationContext;

public class SessionFragment extends Fragment implements EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        Publisher.PublisherListener,
        Subscriber.VideoListener{
    private static final String TAG = "SessionFragment";

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private String sessionid,token,meetingid;
    private boolean segmode=false;
    private SessionLocalCallbacks sessionLocalCallbacks;
    //Mediapipe dependancies
    Nex2meSegmenter mediapipeController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private AutoFitTextureView mediapipeView;
    private ProgressBar progressBar;
    private ImageButton switchCam,disconnect,broadcast;
    TextView meetingidtext,brodText;
    private ProgressBar recprogress;
    private ImageButton recbutton;
    private ImageView recOutput;
    private Button switchSeg;
    private boolean switchb = false;

    private RelativeLayout mPublisherViewContainer;
    private LinearLayout mSubscriberViewContainer;
    public String getSessionid() {
        return sessionid;
    }

    public SessionLocalCallbacks getSessionLocalCallbacks() {
        return sessionLocalCallbacks;
    }

    public void setSessionLocalCallbacks(SessionLocalCallbacks sessionLocalCallbacks) {
        this.sessionLocalCallbacks = sessionLocalCallbacks;
    }

    public boolean isSegmode() {
        return segmode;
    }

    public void setSegmode(boolean segmode) {
        this.segmode = segmode;
    }

    public String getMeetingid() {
        return meetingid;
    }

    public void setMeetingid(String meetingid) {
        this.meetingid = meetingid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.session_fragment, container, false);

        mPublisherViewContainer = view.findViewById(R.id.publisherview);
        mSubscriberViewContainer = view.findViewById(R.id.subscriberview);
        progressBar=view.findViewById(R.id.progressBar);
        switchCam=view.findViewById(R.id.switchCam);
        switchCam.setOnClickListener(switchCamListner);
        disconnect=view.findViewById(R.id.disconnect);
        disconnect.setOnClickListener(disconnectListner);
        meetingidtext=view.findViewById(R.id.meeting_id_text);
        broadcast=view.findViewById(R.id.broadcast);
        broadcast.setOnClickListener(broadcastListner);
        brodText=view.findViewById(R.id.brodText);
        recprogress = view.findViewById(R.id.recprogress);
        recprogress.setVisibility(View.GONE);
        recbutton = view.findViewById(R.id.recbutton);
        recbutton.setOnClickListener(recListner);
        recOutput=view.findViewById(R.id.recOutput);
        recOutput.setVisibility(View.GONE);
        recOutput.setOnClickListener(videoClickListner);
        switchSeg=view.findViewById(R.id.swicth);
        switchSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchb = !switchb;
            }
        });
        if(meetingid!=null){
            meetingidtext.setText(meetingid);
        }
        initializaMediapipe(view);
        requestPermissions();
        return view;
    }
    private Bitmap original;
    private void initializaMediapipe(View view){
        mediapipeView = view.findViewById(R.id.video_view);
        mediapipeView.setVisibility(View.VISIBLE);
        original = BitmapFactory.decodeResource(getResources(),R.drawable.blank_img);
        incoming = BitmapFactory.decodeResource(getResources(),R.drawable.blank_img);
        mediapipeController = new MediapipeController(getActivity());
        mediapipeController.initMediapipe(mediapipeView);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        if (mSession == null) {
            return;
        }
        mSession.onPause();
        //stopRecordRendererThread();
        mediapipeController.pause();
        /*if (isFinishing()) {
            disconnectSession();
        }*/
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        captureLogs();
        super.onResume();

        if (mSession == null) {
            return;
        }
        mSession.onResume();
        resumeMediapipe();
        recprogress.setVisibility(View.GONE);
        recOutput.setVisibility(View.GONE);
    }
    private void resumeMediapipe(){
        mediapipeController.resume();
        startSegmentation();
    }
    private void startSegmentation(){
        try {

            mediapipeController.startGraph();
        }catch (Exception e){
            Log.d("MediaController",e.toString());
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        saveLog("device_log");
        disconnectSession();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(getActivity(), perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            mSession = new Session.Builder(getActivity(), AppRTCCOnfig.API_KEY, sessionid).build();
            mSession.setSessionListener(this);
            mSession.connect(token);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    private CustomVideoCapturer customVideoCapturer;
    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());
        progressBar.setVisibility(View.GONE);
        InvertedColorsVideoRenderer renderer = new InvertedColorsVideoRenderer(getContext());
        if(isSegmode()){
            renderer.setFrameListner(foreGroudListner);
        }
        else{
            renderer.setFrameListner(backgroundListner);
        }
        customVideoCapturer = new CustomVideoCapturer(getActivity(), Publisher.CameraCaptureResolution.MEDIUM, Publisher.CameraCaptureFrameRate.FPS_30);
        mPublisher = new Publisher.Builder(getActivity())
                .name("publisher")
                .capturer(customVideoCapturer)
                .renderer(renderer).build();
        mPublisher.setPublisherListener(this);

        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) (mPublisher.getView())).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }
    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: disconnected from session " + session.getSessionId());

        mSession = null;
    }
    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());
        Toast.makeText(getContext(), "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        //finish();
        if(sessionLocalCallbacks!=null){
            sessionLocalCallbacks.onFinishRequest();
        }
    }
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());
        if (mSubscriber != null) {
            return;
        }

        subscribeToStream(stream);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

        if (mSubscriber == null) {
            return;
        }

        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber.destroy();
            mSubscriber = null;
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");

        Toast.makeText(getContext(), "AppRTC ICE Failure(1007). See the logcat please.", Toast.LENGTH_LONG).show();
        //finish();
        if(sessionLocalCallbacks!=null){
            sessionLocalCallbacks.onFinishRequest();
        }
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mSubscriberViewContainer.addView(mSubscriber.getView());
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }

    private void subscribeToStream(Stream stream) {
        InvertedColorsVideoRenderer renderer = new InvertedColorsVideoRenderer(getContext());
        if(isSegmode()){
            renderer.setFrameListner(backgroundListner);
        }
        else{
            renderer.setFrameListner(foreGroudListner);
        }

        mSubscriber = new Subscriber.Builder(getActivity(), stream)
                .renderer(renderer)
                .build();

        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);
    }

    private void disconnectSession() {
        if (mSession == null) {
            return;
        }
        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSession.unsubscribe(mSubscriber);
            mSubscriber.destroy();
            mSubscriber = null;
        }
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
    }
    private Bitmap incoming;
    private FrameListner foreGroudListner =  new FrameListner() {
        @Override
        public void onFrame(Bitmap frame) {
            original=frame;
            if(!isSegmode()){
                return;
            }

            if(switchb){
                mediapipeController.onBackGroundFrame(incoming);
                mediapipeController.onForgroundFrame(original);
            }
            else{
                mediapipeController.onBackGroundFrame(original);
                mediapipeController.onForgroundFrame(incoming);
            }


        }
    };
    private FrameListner backgroundListner = new FrameListner() {
        @Override
        public void onFrame(Bitmap frame) {
            incoming=frame;
            if(isSegmode()){
                return;
            }
            if(switchb){
                mediapipeController.onBackGroundFrame(original);
                mediapipeController.onForgroundFrame(incoming);
            }
            else {
                mediapipeController.onBackGroundFrame(incoming);
                mediapipeController.onForgroundFrame(original);
            }

            Log.d("FRAME_DEBUG","on bg frame");
        }
    };
    private View.OnClickListener switchCamListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(customVideoCapturer!=null){
                customVideoCapturer.cycleCamera();
            }
        }
    };
    private View.OnClickListener disconnectListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                endCall();
        }
    };
    private void endCall(){
        onEndCall(meetingid);
        if(sessionLocalCallbacks!=null){
            sessionLocalCallbacks.onFinishRequest();
        }
    }
    private View.OnClickListener broadcastListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isBrodcasting){
                isBrodcasting=true;
                startBroadcast(meetingid);
            }
        }
    };
    boolean isBrodcasting = false;
    private void startBroadcast(String meetingid){
        brodText.setText("Waiting....");
        ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
        Call<SessionResponse> call = service.startBroadcast(meetingid);
        call.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if(response.code() == 200){
                    if(response.body().getStatus()==200){
                        brodText.setText("Broadcasting");
                        isBrodcasting=true;
                    }
                    else if(response.body().getStatus() == 403){
                        Toast.makeText(getContext(),response.body().getMessage(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,response.body().getMessage());
                        isBrodcasting=false;
                        brodText.setText("Try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                brodText.setText("Try again.");
                Log.d(TAG,t.getLocalizedMessage());
                isBrodcasting=false;
            }
        });

    }

    private void onEndCall(String meetingid){
        ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
        Call<SessionResponse> call = service.stopBroadcast(meetingid);
        call.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if(response.code() == 200){
                    if(response.body().getStatus()==200){
                        Log.d(TAG,"200: "+response.body().getMessage());
                    }
                    else if (response.body().getStatus()==403){
                        Log.d(TAG,"403: "+response.body().getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Log.d(TAG,t.getLocalizedMessage());
            }
        });
    }

    private int outputWidth=420,outputHeight = 640;
    public int getWidth(){
        return outputWidth;
    }
    public int getHeight(){
        return getHeight();
    }
    private File mOutputFile;
    private Uri contentUri;
    private BitmapToVideoEncoder bitmapToVideoEncoder;
    private void startRecording(){
        Log.d("REC_TEST","startRecording()");
        Log.d("REC_TEST","startRecording()");
        bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
            @Override
            public void onEncodingComplete(File outputFile) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),  "Recording complete!", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
        Log.d("REC_TEST","creating output file. ");
        mOutputFile = createVideoOutputFile();
        Log.d("REC_TEST","Output file created. encode is "+bitmapToVideoEncoder);
        Log.d("REC_TEST","Output file created. encode is "+bitmapToVideoEncoder.isEncodingStarted());
        //bitmapToVideoEncoder.startEncoding(getWidth(), getHeight(), mOutputFile,true);
        bitmapToVideoEncoder.startEncoding(mediapipeView.getWidth(),mediapipeView.getHeight(),mOutputFile);
        Log.d("REC_TEST","after startEncoding ");
        startRecordingThread();
    }
    private void stopRecording(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bitmapToVideoEncoder!=null){
                    bitmapToVideoEncoder.stopEncoding();
                    contentUri = FileProvider.getUriForFile(getContext(),
                            "co.introtuce.nex2me.demo.fileprovider", mOutputFile);
                    Log.d("REC_TEST","Output file size "+mOutputFile.length());
                    isRecording=false;
                    onCompleteRecording(contentUri);
                }
            }
        });

    }
    private void createVideo(List<Bitmap> frames){
        BitmapToVideoEncoder bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
            @Override
            public void onEncodingComplete(File outputFile) {
                Toast.makeText(getContext(),  "Encoding complete!", Toast.LENGTH_LONG).show();
            }
        });
        bitmapToVideoEncoder.startEncoding(getWidth(), getHeight(), new File("some_path"));
        //bitmapToVideoEncoder.queueFrame(bitmap1);
        //bitmapToVideoEncoder.queueFrame(bitmap2);
        //bitmapToVideoEncoder.queueFrame(bitmap3);
        //bitmapToVideoEncoder.queueFrame(bitmap4);
        //bitmapToVideoEncoder.queueFrame(bitmap5);
        bitmapToVideoEncoder.stopEncoding();
    }
    private File createVideoOutputFile() {
        File tempFile = null;
        try {
            File dirCheck = new File(
                    getContext().getFilesDir().getCanonicalPath() + "/" + "captures");
            if (!dirCheck.exists()) {
                dirCheck.mkdirs();
            }

            String filename = new Date().getTime() + "";
            tempFile = new File(
                    getContext().getFilesDir().getCanonicalPath() + "/" + "captures" + "/"
                            + filename + ".mp4");
        } catch (IOException ioex) {
            Log.e(TAG, "Couldn't create output file", ioex);
        }
        return tempFile;
    }
    boolean isRecording=false;
    long startTime=0l;
    public void startRecordingThread()
    {
        Log.d("REC_TEST","startRecording IN thread");
        if(isRecording){
            return;
        }
        isRecording = true;
        recprogress.setProgress(0);
        first=null;
        recprogress.setVisibility(View.VISIBLE);
        recprogress.setMax(20);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                startTime=System.currentTimeMillis();
                Log.d("REC_TEST","Time "+((System.currentTimeMillis()-startTime)/1000));
                while ((System.currentTimeMillis()-startTime)/1000<20){
                    Log.d("REC_TEST","startRecordingThread "+((System.currentTimeMillis()-startTime)/1000));
                    if(bitmapToVideoEncoder!=null){
                        addBitmapToQueue();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recprogress.setProgress((int) (System.currentTimeMillis()-startTime)/1000);
                        }
                    });
                    try {
                        Thread.sleep(1000/30);
                    }catch (Exception e){}
                }
                stopRecording();
            }
        });
        t.start();
    }
    Bitmap bmp;
    public void addBitmapToQueue(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bitmapToVideoEncoder!=null){
                    bmp = mediapipeView.getBitmap();
                    bitmapToVideoEncoder.queueFrame(bmp);
                    if(first==null){
                        first=bmp;
                    }
                }
            }
        });
    }

    private void onCompleteRecording(Uri contentUri){

       recOutput.setVisibility(View.VISIBLE);
       recOutput.setImageBitmap(first);
       Bitmap play = BitmapFactory.decodeResource(getResources(),R.drawable.ic_baseline_videocam_24);
       recbutton.setImageBitmap(play);

    }
    private Bitmap first;
    private View.OnClickListener recListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("REC_TEST","recListner");
            if(isRecording){
                return;
            }
            Log.d("REC_TEST","recListner");
            Bitmap pause = BitmapFactory.decodeResource(getResources(),R.drawable.ic_baseline_pause_24);
            recbutton.setImageBitmap(pause);
            startRecording();
        }
    };
    private View.OnClickListener videoClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            lagacyFileChooser();
        }
    };
    private void lagacyFileChooser(){
        try{
            if(contentUri!=null){
                SaveLocal.saveVideo(getApplicationContext(),contentUri);
                String path =  SaveLocal.copyFileFromUri(getApplicationContext(),contentUri);
                if(path.equals("")){
                    Toast.makeText(getApplicationContext(),"Something went wrng..",Toast.LENGTH_LONG).show();
                    return;
                }
                VideoDisplayActivity.thmb = first;
                VideoDisplayActivity.contentUri = contentUri;
                Intent intent=new Intent(getApplicationContext(),VideoDisplayActivity.class);
                intent.putExtra("PATH_FILE",path);
                startActivity(intent);
            }

        }catch (Exception e){
            Log.d(VideoDisplayActivity.TAG,"At ImInAct: "+e.toString());
        }

    }

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
                try{
                    stringBuilder.append("\n\nRuntime : \n\n");
                    stringBuilder.append(((MediapipeController)mediapipeController).getRuntimeLog());
                }catch (Exception e){
                    e.printStackTrace();
                }

                stringBuilder.append("\n\n GPU INFO \n");
                stringBuilder.append("NA, Try with live video to capture gpu info.");
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