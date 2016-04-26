package es.mschez.plugins;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class LockTask extends CordovaPlugin {
  private enum Actions {
    START_LOCK_TASK,
    STOP_LOCK_TASK,
    IS_LOCK_TASK_SUPPORTED,
    IS_IN_LOCK_TASK_MODE
  }

  private Activity activity;
  private ActivityManager activityManager;
  private CordovaInterface cordova;
  private DevicePolicyManager mDPM;
  private String packageName;

  @Override
  public void initialize(CordovaInterface cordovaInterface, CordovaWebView webView) {
    super.initialize(cordova, webView);

    activity = cordovaInterface.getActivity();
    activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
    cordova = cordovaInterface;
    mDPM = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
    packageName = activity.getPackageName();
  }

  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      Actions currentAction = Actions.valueOf(action);
      switch (currentAction) {
        case START_LOCK_TASK:
          this.cordova.getThreadPool().execute(new Runnable() {
            private String adminClassName = args.length() > 0 ? args.getString(0) : "";

            @Override
            public void run() {
              startLockTask(callbackContext, adminClassName);
            }
          });
          break;
        case STOP_LOCK_TASK:
          this.cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
              stopLockTask(callbackContext);
            }
          });
          break;
        case IS_LOCK_TASK_SUPPORTED:
          this.cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
              isLockTaskSupported(callbackContext);
            }
          });
          break;
        case IS_IN_LOCK_TASK_MODE:
          this.cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
              isInLockTaskMode(callbackContext);
            }
          });
          break;
        default:
          callbackContext.error("The method '" + action + "' does not exist.");
          return false;
      }

      return true;
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    }
  }

  public boolean startLockTask(CallbackContext callbackContext, String adminClassName) {
    Log.i("LockTask", "adminClassName: " + adminClassName);
    Log.i("LockTask", "packageName: " + packageName);
    Log.i("LockTask", "isDeviceOwner: " + mDPM.isDeviceOwnerApp(packageName));
    Log.i("LockTask", "isLockTaskPermitted: " + mDPM.isLockTaskPermitted(packageName));

    // Only do this if we are the device owner app and we don't have the lock task permission already.
    if (!adminClassName.isEmpty() && !mDPM.isLockTaskPermitted(packageName) && mDPM.isDeviceOwnerApp(packageName)) {
      ComponentName mDeviceAdmin = new ComponentName(packageName, packageName + "." + adminClassName);

      Log.i("LockTask", "Setting lock task packages");
      // Ideally, we'd only add our package to the list, but there's no `getLockTaskPackages`.
      mDPM.setLockTaskPackages(mDeviceAdmin, new String[]{packageName});
    }

    if (!activityManager.isInLockTaskMode()) {
      activity.startLockTask();
    }

    callbackContext.success();
    return true;
  }

  public boolean stopLockTask(CallbackContext callbackContext) {
    if (activityManager.isInLockTaskMode()) {
      activity.stopLockTask();
    }

    callbackContext.success();
    return true;
  }

  protected boolean isLockTaskSupported(CallbackContext callbackContext) {
    boolean supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    PluginResult res = new PluginResult(PluginResult.Status.OK, supported);
    callbackContext.sendPluginResult(res);

    return true;
  }

  protected boolean isInLockTaskMode(CallbackContext callbackContext) {
    boolean active = activityManager.isInLockTaskMode();

    PluginResult res = new PluginResult(PluginResult.Status.OK, active);
    callbackContext.sendPluginResult(res);

    return true;
  }
}
