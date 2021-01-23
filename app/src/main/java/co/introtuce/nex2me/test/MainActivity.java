package co.introtuce.nex2me.test;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import co.introtuce.nex2me.test.analytics.accelerate.SegmentTesterAct;
import co.introtuce.nex2me.test.fileManager.StaticFileStorage;
import co.introtuce.nex2me.test.fileManager.downloadManager.DownloadEventListner;
import co.introtuce.nex2me.test.fileManager.downloadManager.DownloadManager;
import co.introtuce.nex2me.test.fileManager.downloadManager.VideoDownloader;
import co.introtuce.nex2me.test.helper.ErrorListner;
import co.introtuce.nex2me.test.helper.PlayForListner;
import co.introtuce.nex2me.test.helper.VideoPlayerHelper;
import co.introtuce.nex2me.test.helper.adapter.SelectionListner;
import co.introtuce.nex2me.test.helper.adapter.SnapHelperOneByOne;
import co.introtuce.nex2me.test.helper.adapter.VideoListAdapter;
import co.introtuce.nex2me.test.network.ApiClient;
import co.introtuce.nex2me.test.network.ApiInterface;
import co.introtuce.nex2me.test.network.Nex2meBroadcast;
import co.introtuce.nex2me.test.rtc.Nex2meGrowthActivity;
import co.introtuce.nex2me.test.ui.VerticalSpaceItemDecoration;
import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Nex2meGrowthActivity implements DownloadEventListner , EasyPermissions.PermissionCallbacks {

    ProgressBar progressBar;
    public static final String TAG = "MainActivity";
    DownloadManager downloadManager;
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private VideoListAdapter adapter;

    //UIElements
    protected AutoFitTextureView textureView;
    private RecyclerView recyclerView;
    private TextureView videoTexture;
    private List<Nex2meBroadcast> broadcasts = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        textureView = findViewById(R.id.textureV);
        //textureView.setSurfaceTextureListener(listener);
        loadBroadcasts();
    }


    @Override
    public void onStartDownload() {
        Log.d(TAG,"Starting download");
    }

    @Override
    public void onEndDownload() {
        Log.d(TAG,"Ending download download "+downloadManager.getOutfileUri());
        if(current_index!=downloadingIndex){
            return;
        }
        downloadingIndex = -1;
        playWithCheck(current_index);

    }

    @Override
    public void updateUI(int unit) {

        Log.d(TAG,"Update : "+unit);

    }

    public void afterPermissionGranted(){
        Log.d(TAG,"afterPermissionGranted()");



       setupRecycerView();


    }


    public void setupRecycerView(){
        recyclerView = findViewById(R.id.recyclerview);
        adapter=new VideoListAdapter(this);
        adapter.setBroadcasts(broadcasts);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        LinearSnapHelper linearSnapHelper = new SnapHelperOneByOne();
        ((SnapHelperOneByOne) linearSnapHelper).setmRecyclerView(recyclerView);
        linearSnapHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(20));
        recyclerView.setOnScrollListener(listener);
        adapter.setPlayForListner(playForListner);
        adapter.setItemSelectionListner(itemSelectListner);
        recyclerView.setAdapter(adapter);
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
              adapter.onItemVisible(0);
          }
      },1000);


    }


    public void downloadFile(int index){
        downloadingIndex = index;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        String uri = StaticFileStorage.video_urls[index];
        String fileName = StaticFileStorage.fileName[index];
        downloadManager = new VideoDownloader(this,uri,this,fileName);

        downloadManager.setProgressBar(progressBar);
        downloadManager.start();

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        Log.d(TAG,"requestPermissions()");
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            afterPermissionGranted();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
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

    private SimpleExoPlayerView playerView;
    private VideoPlayerHelper helper;
    public void setupPlayer(int index){

        progressBar.setVisibility(View.GONE);
        playerView = findViewById(R.id.video_player);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        helper=new VideoPlayerHelper(this,playerView,player,textureView);
        Log.d("BROADCAST_t","source "+broadcasts.size());
        if(index<broadcasts.size()){
            helper.setSource(broadcasts.get(index).getHls());
            Log.d("BROADCAST_t"," "+broadcasts.get(index).getHls());
        }
        else{
            helper.setSource(StaticFileStorage.getFilePath(index-broadcasts.size()));
            Log.d("BROADCAST_t"," "+StaticFileStorage.getFilePath(index-broadcasts.size()));
        }

        helper.setErrorListner(errorListner);





    }

    public void playWithCheck(int index){
         Log.d(TAG,"File status: "+StaticFileStorage.isFileExist(index));
         if(index<broadcasts.size()){
             playVideo(index);
             return;
         }

        if(!StaticFileStorage.isFileExist(index-broadcasts.size())){
            downloadFile(index);
            return;
        }
       playVideo(index);
    }

    public void playVideo(int index){
        setupPlayer(index);
        if(helper!=null){
            if(index<broadcasts.size()){
                helper.prepHls(broadcasts.get(index).getHls());
            }
            else {
                helper.prep(StaticFileStorage.getFilePath(index-broadcasts.size()));
            }

            videoTexture=(TextureView)playerView.getVideoSurfaceView();
        }
        else {
            setupPlayer(index);
            if(index<broadcasts.size()){
                helper.prep(broadcasts.get(index).getHls());
            }
            else {
                helper.prep(StaticFileStorage.getFilePath(index-broadcasts.size()));
            }
            videoTexture=(TextureView)playerView.getVideoSurfaceView();
        }
        render_b = true;
    }

    private boolean extrator = true;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (true){
                threadLock.lock();
                if(textureView!=null && currentView!=null && render_b && downloadingIndex!=current_index && extrator) {
                    long start = System.currentTimeMillis();

                    final Bitmap bp = textureView.getBitmap();
                    if(bp == null){
                        continue;
                    }
                    long end = System.currentTimeMillis();
                    Log.d(TAG,"Time : "+(end-start)+", "+bp.getWidth());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //imageView.setImageBitmap(bp);
                            currentView.setImageBitmap(bp);
                        }
                    });
                    try{
                        Thread.sleep(1000/30);
                    }catch (Exception e){

                    }

                }
                threadLock.unlock();
            }
        }
    });


    RecyclerView.OnScrollListener listener=new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                int firstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int lastVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                Log.d("SCROLL_STATE","Fist "+firstVisiblePosition+"");
                Log.d("SCROLL_STATE","Second "+lastVisiblePosition+"");
                if(adapter!=null){
                    adapter.onItemVisible(lastVisiblePosition);
                }

                // Now you can easily get all rows b/w first and last item
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    private int current_index = -1;
    private PlayForListner playForListner=new PlayForListner() {
        @Override
        public void onItemVisible(ImageView imageView, int index) {

            if(current_index == index){
                return;
            }
            current_index = index;
           playRecyclerViewVideo(imageView,index);

        }
    };


    private ImageView currentView;
    private boolean render_b = false;
    private void playRecyclerViewVideo(ImageView imageView, int index){
        if(current_index != index){
            return;
        }
        releasePreviousPlayer();
        playWithCheck(index);
        currentView = imageView;
        startThread();
        resumeThread();



    }
    private ReentrantLock threadLock=new ReentrantLock();
    private void releasePreviousPlayer(){
        try{

            if(hasThreadStarted){
                puaseThread();
            }
            if(helper!=null){
                helper.releasePlayer();
            }
            render_b = false;


        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }
    private void puaseThread(){
        try{
            threadLock.lock();
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }

    }
    private boolean hasThreadStarted = false;
    private void startThread(){
        try{
            if(hasThreadStarted){
                return;
            }
            thread.start();
            hasThreadStarted = true;
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }
    private void resumeThread(){
        try{
            threadLock.unlock();
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(helper!=null){
            helper.puase();
        }
        if(hasThreadStarted){
            puaseThread();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        extrator = true;
        if(helper!=null){
            helper.resume();
        }
        if(hasThreadStarted){
            resumeThread();
        }
        if(set_act==1){
            onBack(current_index);
        }
    }


    int downloadingIndex = -1;
    private ErrorListner errorListner = new ErrorListner() {
        @Override
        public void onExoError(ExoPlaybackException error, String source) {
            Log.d("BROADCAST_t",error.getLocalizedMessage()+" Fail");
            if(error.type == ExoPlaybackException.TYPE_SOURCE){
                if(StaticFileStorage.getFilePath(StaticFileStorage.video_urls[current_index]).equals(source));
                downloadFile(current_index);
            }
        }
    };

    //OnSelect Event
    private SelectionListner itemSelectListner = new SelectionListner() {
        @Override
        public void onItemSelect(int index) {
            if(downloadingIndex!=index)
            imin(index);
            else{
                Toast.makeText(getApplicationContext(),"Please file is downloading..",Toast.LENGTH_LONG).show();
            }
        }
    };

    private int set_act = 0;
    private void imin(int index){
        extrator = false;
        Intent intent = new Intent(this, SegmentTesterAct.class);
        if(index<broadcasts.size()){
            intent.putExtra("VIDEO_HLS",broadcasts.get(index).getHls());
        }
        else{
            intent.putExtra("VIDEO_INDEX",index-broadcasts.size());
        }
        startActivity(intent);
        set_act=1;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("ONBACK","On Back coming");
    }

    public void onBack(int current_index){
        Log.d("ONBACK","On Back coming");
        if(adapter!=null){
            adapter.setBack(current_index);
            set_act=0;
        }
    }
    private void loadBroadcasts(){
        showProgress("Please wait");
        ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Nex2meBroadcast>> call = service.getBroadcasts();
        call.enqueue(new Callback<List<Nex2meBroadcast>>() {
            @Override
            public void onResponse(Call<List<Nex2meBroadcast>> call, Response<List<Nex2meBroadcast>> response) {
                hideProgress();
                if(response.code() == 200){
                    broadcasts = response.body();
                    Log.d("BROADCAST_t","size "+broadcasts.size());
                }
                else{
                    Log.d("BROADCAST_t","code "+response.code());
                }
                postBroadcast();
            }

            @Override
            public void onFailure(Call<List<Nex2meBroadcast>> call, Throwable t) {
                hideProgress();
                postBroadcast();
                Log.d("BROADCAST_t","fail "+t);
            }
        });
    }
    private void postBroadcast(){
        if(broadcasts == null){
            broadcasts=new ArrayList<>();
        }
        requestPermissions();
    }
}
