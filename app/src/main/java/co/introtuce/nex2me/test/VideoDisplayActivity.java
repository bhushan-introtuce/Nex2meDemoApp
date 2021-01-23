package co.introtuce.nex2me.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.kinda.alert.KAlertDialog;

import java.io.File;

import co.introtuce.nex2me.test.helper.VideoPlayerHelper;
import co.introtuce.nex2me.test.helper.animation.AnimationHelper;
import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;
import me.zhanghai.android.materialplaypausedrawable.MaterialPlayPauseButton;
import me.zhanghai.android.materialplaypausedrawable.MaterialPlayPauseDrawable;

public class VideoDisplayActivity extends AppCompatActivity {

    public static final int S_SAVE=1;
    public static final int S_SHARE=2;
    public static final int S_DELETE=3;
    public static int OPERATION=0;
    SimpleExoPlayerView playerView;
    ImageView thumbnail;
    MaterialPlayPauseButton playPauseButton;
    private VideoPlayerHelper helper;
    private AutoFitTextureView mTexture,customSurface;
    private String videoPath;
    public static Bitmap thmb;
    public static Uri contentUri;
    private Uri newCUri;
    private ImageView save,share,delete;

    public static final String TAG= "TAG_LL";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_display);
        try{
            thumbnail = findViewById(R.id.thumnail);
            playPauseButton = findViewById(R.id.play_pause);
            playPauseButton.setState(MaterialPlayPauseDrawable.State.Pause);
            mTexture = findViewById(R.id.mTexture);
            mTexture.setOnClickListener(screenTouch);
            playPauseButton.setOnClickListener(playPauseListner);
            playPauseButton.setState(MaterialPlayPauseDrawable.State.Play);
            save=findViewById(R.id.save);
            share=findViewById(R.id.share);
            delete = findViewById(R.id.delete);
            save.setOnClickListener(eventListner);
            delete.setOnClickListener(eventListner);
            share.setOnClickListener(eventListner);
            customSurface = findViewById(R.id.playSpace);
            if(thmb!=null){
                thumbnail.setImageBitmap(thmb);
            }
            videoPath = getIntent().getStringExtra("PATH_FILE");

            if(videoPath==null){
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_LONG).show();
                finish();
            }
            //setupPlayer(videoPath);
            customSurface.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setupPlayer(videoPath,surface);
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

        }catch (Exception e){
            Log.d(TAG,e.toString());
        }



    }




    AnimationHelper animationHelper = new AnimationHelper();
    private View.OnClickListener playPauseListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(playPauseButton.getState() == MaterialPlayPauseDrawable.State.Pause){
                stopVideo();
                playPauseButton.setState(MaterialPlayPauseDrawable.State.Play);
            }
            else if(playPauseButton.getState() == MaterialPlayPauseDrawable.State.Play){
                playVideo();
                playPauseButton.setState(MaterialPlayPauseDrawable.State.Pause);
            }
        }
    };

    MediaPlayer player;
    boolean isPrep=false;
    public void setupPlayer(String path, SurfaceTexture surfaceTexture){
        Uri filUri = Uri.parse(path);
        player = MediaPlayer.create(this,filUri);
        player.setSurface(new Surface(surfaceTexture));
        player.setLooping(true);
        player.prepareAsync();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrep=true;
            }
        });

    }

    public void setupPlayer(String path){

        File file = new File(path);
        Log.d("VIDEO_OUTPUT","Size "+file.length());
        playerView = findViewById(R.id.video_player);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        helper=new VideoPlayerHelper(this,playerView,player,mTexture);

        helper.setSource(path);
        helper.prep(path);
        helper.puase();

    }

    public void playVideo(){

        /*if(helper!=null){
            playerView.setVisibility(View.VISIBLE);
            helper.resume();
        }*/
        if(isPrep){
            player.start();
        }
    }

    public void stopVideo(){
        /*if(helper!=null){
            helper.puase();

        }*/
        if(isPrep){
            player.pause();
        }
    }


    private View.OnClickListener screenTouch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG,"Screen tapped");
        }
    };


    private View.OnClickListener eventListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.save:
                    onSave();
                    break;
                case R.id.delete:
                    deleteRequest();
                    break;
                case R.id.share:
                    share(contentUri);
                    break;

            }

        }
    };

    private void deleteRequest(){
        new KAlertDialog(this, KAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this file!")
                .setConfirmText("Yes,delete it!")
                .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        onDeleteFile();
                    }
                })
                .show();
    }

    private void onDeleteFile(){

        OPERATION = S_DELETE;
        finish();

    }

    private void onSave(){
        new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE)
                .setTitleText("Video has saved")
                .setContentText("ZYour video has been save to your memory..!")
                .setConfirmText("ok!")
                .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        onSaveCompleted();
                    }
                })
                .show();
    }

    private void onSaveCompleted(){
        OPERATION=S_SAVE;
        try {
            addVideo(new File(videoPath));
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
        finish();
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
            OPERATION=S_SHARE;

        }catch (Exception e){
            Log.d(TAG," at share: "+e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        OPERATION=0;
    }


    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public Uri addVideo(File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, "Nex2me");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }



}
