package hungnt.com.imagetargetdemo;

import android.app.AlertDialog;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by HungNT on 2/7/18.
 */

public class BookRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl {
    private final SampleAppRenderer mSampleAppRenderer;
    private VuforiaApplicationSession vuforiaAppSession;
    private ImageTargetActivity mActivity;
    private boolean mIsActive;

    public BookRenderer(ImageTargetActivity activity, VuforiaApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        vuforiaAppSession.onSurfaceCreated();
        mSampleAppRenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        vuforiaAppSession.onSurfaceChanged(width, height);
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
        initRendering();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (!mIsActive) {
            return;
        }

        mSampleAppRenderer.render();
    }

    @Override
    public void renderFrame(State state, float[] projectionMatrix) {
        mSampleAppRenderer.renderVideoBackground();

        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            if (trackable.getName().startsWith("op")) {
                final String name = trackable.getName();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(mActivity)
                                .setMessage("Id: " + name)
                                .setPositiveButton("OK", null)
                                .create().show();

                    }
                });
                vuforiaAppSession.onPause();
            }
        }
    }

    private void initRendering() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
    }

    public void setActive(boolean active) {
        mIsActive = active;
        if (mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }
}
