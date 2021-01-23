package co.introtuce.nex2me.test.analytics.accelerate.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.google.mediapipe.components.CameraHelper;

import javax.annotation.Nullable;

import co.introtuce.nex2me.test.R;

public class Camera2TestActivty extends AppCompatActivity {

    private TextureView cameraView;
    private Camera2Helper camera2Helper;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_test_activty);
        cameraView = findViewById(R.id.cameraView);
        this.context=this;
        camera2Helper = new Camera2Helper(context,getWindowManager().getDefaultDisplay().getRotation());
        /*cameraView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                camera2Helper = new Camera2Helper(context,surface);
                camera2Helper.startCamera((Activity)context,null,null);
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
        });*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    private void startCamera(){
        Log.d(Camera2Helper.TAG,"Starting camera 2");
        camera2Helper.setOnCameraStartedListener(new CameraHelper.OnCameraStartedListener() {
            @Override
            public void onCameraStarted(@Nullable SurfaceTexture surfaceTexture) {
                cameraView.setSurfaceTexture(surfaceTexture);
                Log.d(Camera2Helper.TAG,"Camera surface setted");
            }
        });




        camera2Helper.startCamera(this,null,null);
    }

}
