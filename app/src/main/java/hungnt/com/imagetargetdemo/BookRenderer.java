package hungnt.com.imagetargetdemo;

import android.app.AlertDialog;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

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
    private static final String LOGTAG = "ImageTargetRenderer";
    private final SampleAppRenderer mSampleAppRenderer;
    private VuforiaApplicationSession vuforiaAppSession;
    private ImageTargetActivity mActivity;
    private boolean mIsActive;

    public BookRenderer(ImageTargetActivity activity, VuforiaApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        // Initializes rendering
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
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
        }
    }

    // Function for initializing the renderer.
    private void initRendering() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        // TODO Hide the Loading Dialog
//        mActivity.loadingDialogHandler
//                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

    }

    private void printUserData(final Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
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

    public void setActive(boolean active) {
        mIsActive = active;
        if (mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }
}
