package co.introtuce.nex2me.test.analytics.accelerate;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import androidx.camera.core.CameraX;
import androidx.camera.core.CameraX.LensFacing;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import com.google.mediapipe.components.CameraHelper;

/**
 * Uses CameraX APIs for camera setup and access.
 *
 * <p>{@link CameraX} connects to the camera and provides video frames.
 */
public class MyCameraXHelper extends CameraHelper {
    private static final String TAG = "MyCameraXHelper";

    private Preview preview;

    // Size of the camera-preview frames from the camera.
    private Size frameSize;
    // Rotation of the camera-preview frames in degrees.
    private int frameRotation;

    @Override
    @SuppressWarnings("RestrictTo") // See b/132705545.
    public void startCamera(
            Activity context, CameraFacing cameraFacing, SurfaceTexture surfaceTexture) {
        LensFacing cameraLensFacing =
                cameraFacing == CameraHelper.CameraFacing.FRONT ? LensFacing.FRONT : LensFacing.BACK;
        PreviewConfig previewConfig =
                new PreviewConfig.Builder().
                        setLensFacing(cameraLensFacing)

                        .build();
        preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(
                previewOutput -> {
                    if (!previewOutput.getTextureSize().equals(frameSize)) {
                        frameSize = previewOutput.getTextureSize();
                        frameRotation = previewOutput.getRotationDegrees();
                        if (frameSize.getWidth() == 0 || frameSize.getHeight() == 0) {
                            // Invalid frame size. Wait for valid input dimensions before updating display size.
                            Log.d(TAG, "Invalid frameSize.");
                            return;
                        }
                    }
                    if (onCameraStartedListener != null) {
                        onCameraStartedListener.onCameraStarted(previewOutput.getSurfaceTexture());
                    }
                });
        CameraX.unbindAll();
        CameraX.bindToLifecycle(/*lifecycleOwner=*/ (LifecycleOwner) context, preview);
    }

    @Override
    public Size computeDisplaySizeFromViewSize(Size viewSize) {
        if (viewSize == null || frameSize == null) {
            // Wait for all inputs before setting display size.
            Log.d(TAG, "viewSize or frameSize is null.");
            return null;
        }

        // Valid rotation values are 0, 90, 180 and 270.
        // Frames are rotated relative to the device's "natural" landscape orientation. When in portrait
        // mode, valid rotation values are 90 or 270, and the width/height should be swapped to
        // calculate aspect ratio.
        float frameAspectRatio =
                frameRotation == 90 || frameRotation == 270
                        ? frameSize.getHeight() / (float) frameSize.getWidth()
                        : frameSize.getWidth() / (float) frameSize.getHeight();

        float viewAspectRatio = viewSize.getWidth() / (float) viewSize.getHeight();

        // Match shortest sides together.
        int scaledWidth;
        int scaledHeight;
        if (frameAspectRatio < viewAspectRatio) {
            scaledWidth = viewSize.getWidth();
            scaledHeight = Math.round(viewSize.getWidth() / frameAspectRatio);
        } else {
            scaledHeight = viewSize.getHeight();
            scaledWidth = Math.round(viewSize.getHeight() * frameAspectRatio);
        }

        return new Size(scaledWidth, scaledHeight);
    }



    public boolean isCameraRotated(){
        return false;
    }


}
