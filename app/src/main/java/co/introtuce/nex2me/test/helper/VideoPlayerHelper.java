package co.introtuce.nex2me.test.helper;

import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.exoplayer2.video.VideoRendererEventListener;


import java.io.IOException;

import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;

public class VideoPlayerHelper implements VideoRendererEventListener, VideoPlayerListner {

    public static final String TAG="VideoPlayerHelper";

    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private AutoFitTextureView textureView;
    private Context context;
    private String source;
    private ErrorListner errorListner;


    public VideoPlayerHelper(Context context, PlayerView simpleExoPlayerView, SimpleExoPlayer player , AutoFitTextureView textureView) {
        this.simpleExoPlayerView = simpleExoPlayerView;
        this.player = player;
        this.textureView = textureView;
        this.context = context;
        this.source = source;
    }

    public VideoPlayerHelper(Context context, PlayerView simpleExoPlayerView, SimpleExoPlayer player) {
        this.simpleExoPlayerView = simpleExoPlayerView;
        this.player = player;
        this.context=context;
    }

    public void setErrorListner(ErrorListner errorListner) {
        this.errorListner = errorListner;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public PlayerView getSimpleExoPlayerView() {
        return simpleExoPlayerView;
    }

    public void setSimpleExoPlayerView(PlayerView simpleExoPlayerView) {
        this.simpleExoPlayerView = simpleExoPlayerView;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(SimpleExoPlayer player) {
        this.player = player;
    }


    public SimpleExoPlayer prep(final String localPath){


        Uri mp4VideoUri = Uri.parse(source);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        final int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        simpleExoPlayerView.setUseController(false);//set to true or false to see controllers
        simpleExoPlayerView.requestFocus();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayer2example"), bandwidthMeter);
        //MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);

        Uri uri = Uri.parse(localPath);


        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            Log.d("FILE_ERROR",e.getLocalizedMessage());
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource videoSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        // Prepare the player with the source.

        player.prepare(videoSource);

        if(textureView!=null){
         //   SurfaceTexture surfaceTexture=textureView.getSurfaceTexture();
           // Surface surface=new Surface(surfaceTexture);
            //player.setVideoSurface(surface);

            player.setVideoTextureView(textureView);

        }
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                float viewWidth = textureView.getWidth();
                float viewHeight = textureView.getHeight();
                float scaleX = 1.0f;
                float scaleY = 1.0f;
                if (width > viewWidth && height > viewHeight) {
                    scaleX = width / viewWidth;
                    scaleY = height / viewHeight;
                } else if (width < viewWidth && height < viewHeight) {
                    scaleY = viewWidth / width;
                    scaleX = viewHeight / height;
                } else if (viewWidth > width) {
                    scaleY = (viewWidth / width) / (viewHeight / height);
                } else if (viewHeight > height) {
                    scaleX = (viewHeight / height) / (viewWidth / width);
                }
                int pivotPointX = (int) (viewWidth / 2);
                int pivotPointY = (int) (viewHeight / 2);
                Matrix matrix = new Matrix();
                matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);
                textureView.setTransform(matrix);
            }


            @Override
            public void onRenderedFirstFrame() {

            }
        });

        player.addListener(new ExoPlayer.EventListener() {


            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged... ");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState + "|||isDrawingCacheEnabled():" + simpleExoPlayerView.isDrawingCacheEnabled());
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                //player.stop();
                //player.prepare(loopingSource);
                //player.setPlayWhenReady(true);
                if(errorListner!=null){
                    errorListner.onExoError(error,localPath);
                }

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(this);
        //startfetching();
        return player;
    }
    public SimpleExoPlayer prepHls(final String hls){



        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        //Uri mp4VideoUri = Uri.parse(source);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        final int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        simpleExoPlayerView.setUseController(false);//set to true or false to see controllers
        simpleExoPlayerView.requestFocus();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        //DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayer2example"), bandwidthMeter);
        //MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);

        //Uri uri = Uri.parse(localPath);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Exo2"), bandwidthMeter);
        HlsMediaSource hlsMediaSource = new HlsMediaSource(Uri.parse(hls), dataSourceFactory, mainHandler, new AdaptiveMediaSourceEventListener() {
            @Override
            public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

            }

            @Override
            public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

            }

            @Override
            public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

            }

            @Override
            public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {

            }

            @Override
            public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

            }

            @Override
            public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

            }
        });
        //DataSpec dataSpec = new DataSpec(uri);
        /*final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            Log.d("FILE_ERROR",e.getLocalizedMessage());
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource videoSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        // Prepare the player with the source.
        */
        final LoopingMediaSource loopingSource = new LoopingMediaSource(hlsMediaSource);
        player.prepare(hlsMediaSource);

        if(textureView!=null){
            //   SurfaceTexture surfaceTexture=textureView.getSurfaceTexture();
            // Surface surface=new Surface(surfaceTexture);
            //player.setVideoSurface(surface);

            player.setVideoTextureView(textureView);

        }
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                float viewWidth = textureView.getWidth();
                float viewHeight = textureView.getHeight();
                float scaleX = 1.0f;
                float scaleY = 1.0f;
                if (width > viewWidth && height > viewHeight) {
                    scaleX = width / viewWidth;
                    scaleY = height / viewHeight;
                } else if (width < viewWidth && height < viewHeight) {
                    scaleY = viewWidth / width;
                    scaleX = viewHeight / height;
                } else if (viewWidth > width) {
                    scaleY = (viewWidth / width) / (viewHeight / height);
                } else if (viewHeight > height) {
                    scaleX = (viewHeight / height) / (viewWidth / width);
                }
                int pivotPointX = (int) (viewWidth / 2);
                int pivotPointY = (int) (viewHeight / 2);
                Matrix matrix = new Matrix();
                matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);
                textureView.setTransform(matrix);
            }


            @Override
            public void onRenderedFirstFrame() {

            }
        });

        player.addListener(new ExoPlayer.EventListener() {


            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged... ");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState + "|||isDrawingCacheEnabled():" + simpleExoPlayerView.isDrawingCacheEnabled());
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                //player.stop();
                //player.prepare(loopingSource);
                //player.setPlayWhenReady(true);
                if(errorListner!=null){
                    errorListner.onExoError(error,hls);
                }

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(this);
        //startfetching();
        return player;
    }




    public void releasePlayer(){
        if(player!=null){
            player.release();
        }
    }

    public void puase(){
        if(player!=null){
            player.setPlayWhenReady(false);
        }
    }
    public void resume(){
        if(player!=null){
            player.setPlayWhenReady(true);
        }
    }

    private MediaSource buildMediaSource(Uri uri, Context context) {
        return new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(context,"Exoplayer-local")).
                createMediaSource(uri);
    }

    public void prep() {
        Uri mp4VideoUri = Uri.parse(source);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        simpleExoPlayerView.setUseController(false);//set to true or false to see controllers
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayer2example"), bandwidthMeter);
        MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        // Prepare the player with the source.
        player.prepare(videoSource);
        player.addListener(new ExoPlayer.EventListener() {


            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged... ");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState + "|||isDrawingCacheEnabled():" + simpleExoPlayerView.isDrawingCacheEnabled());
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(loopingSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(this);
        //startfetching();


    }
    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.v(TAG, "onVideoSizeChanged [" + " width: " + width + " height: " + height + "]");
       // resolutionTextView.setText("RES:(WxH):" + width + "X" + height + "\n           " + height + "p");//shows video info
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }


    @Override
    public void onStop() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        player.setPlayWhenReady(true);
    }

    @Override
    public void onPause() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void onDestroy() {

    }
    public boolean getState(){
        return player.getPlayWhenReady();
    }

}
