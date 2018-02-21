package hungnt.com.imagetargetdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.HINT;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

public class ImageTargetActivity extends Activity implements SampleApplicationControl {

    private static final String LOGTAG = ImageTargetActivity.class.getName();
    private VuforiaApplicationSession mVuforiaAppSession;
    private AlertDialog mErrorDialog;
    private SampleApplicationGLView mGlView;
    private BookRenderer mRenderer;
    private DataSet mDataSet;
    private RelativeLayout mUILayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay_reticle, null);

        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mVuforiaAppSession = new VuforiaApplicationSession(this);
        mVuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVuforiaAppSession.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try {
            mVuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mVuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }
        System.gc();
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        TrackerManager manager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) manager.getTracker(ObjectTracker.getClassType());

        if (objectTracker == null) {
            return false;
        }

        if (mDataSet == null) {
            return false;
        }

        if (!mDataSet.load("op87_ar.xml", STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            return false;
        }

        if (!objectTracker.activateDataSet(mDataSet)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean doStartTrackers() {
        Tracker objectTrakcer = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (objectTrakcer != null) {
            objectTrakcer.start();
        }
        return true;
    }

    @Override
    public boolean doStopTrackers() {
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mDataSet != null && mDataSet.isActive()) {
            if (objectTracker.getActiveDataSet(0).equals(mDataSet)
                    && !objectTracker.deactivateDataSet(mDataSet)) {
                result = false;
            } else if (!objectTracker.destroyDataSet(mDataSet)) {
                result = false;
            }

            mDataSet = null;
        }

        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {
        if (exception != null) {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
            return;
        }

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(Vuforia.requiresAlpha(), 16, 0);

        mRenderer = new BookRenderer(this, mVuforiaAppSession);
        mGlView.setRenderer(mRenderer);

        mRenderer.setActive(true);
        addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mUILayout.bringToFront();
        mUILayout.setBackgroundColor(Color.TRANSPARENT);

        mVuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
    }

    @Override
    public void onVuforiaUpdate(State state) {

    }

    @Override
    public void onVuforiaResumed() {
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onVuforiaStarted() {
        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVuforiaAppSession.onConfigurationChanged();
    }

    public void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
                    mErrorDialog.dismiss();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ImageTargetActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }
}
