package es.mschez.plugins;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;

public class LockTask extends CordovaPlugin {

  private enum Actions {
    START_LOCK_TASK,
    STOP_LOCK_TASK,
    IS_LOCK_TASK_SUPPORTED
  }

  // Reference to the web view for static access
  private static CordovaWebView webView;
  private Activity activity;
  private ActivityManager activityManager;
  private static Handler handlerLockTaskActive;

  /**
   * Called after plugin construction and fields have been initialized.
   * Prefer to use pluginInitialize instead since there is no value in
   * having parameters on the initialize() function.
   *
   * pluginInitialize is not available for cordova 3.0-3.5 !
   */
  @Override
  public void initialize (final CordovaInterface cordova, CordovaWebView webView) {
    LockTask.webView = super.webView;

    activity = cordova.getActivity();
    activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
  }

  /**
   * Called when the system is about to start resuming a previous activity.
   *
   * @param multitasking
   *      Flag indicating if multitasking is turned on for app
   */
  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
  }

  /**
   * Called when the activity will start interacting with the user.
   *
   * @param multitasking
   *      Flag indicating if multitasking is turned on for app
   */
  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
  }

  /**
   * The final call you receive before your activity is destroyed.
   */
  @Override
  public void onDestroy() {
  }

  /**
   * Executes the request.
   *
   * This method is called from the WebView thread. To do a non-trivial
   * amount of work, use:
   *      cordova.getThreadPool().execute(runnable);
   *
   * To run on the UI thread, use:
   *     cordova.getActivity().runOnUiThread(runnable);
   *
   * @param action
   *      The action to execute.
   * @param args
   *      The exec() arguments in JSON form.
   * @param callbackContext
   *      The callback context used when calling back into JavaScript.
   * @return
   *      Whether the action was valid.
   */
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      Actions currentAction = Actions.valueOf(action);
      switch (currentAction) {
        case START_LOCK_TASK:
          this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              startLockTask(callbackContext);
            }
          });
          break;
        case STOP_LOCK_TASK:
          this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              stopLockTask(callbackContext);
            }
          });
          break;
        case IS_LOCK_TASK_SUPPORTED:
          this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              isLockTaskSupported(callbackContext);
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

  /**
   * Use this function to start lockTask mode
   *
   * @param callbackContext
   *        Cordova callback context
   * @return true, valid action
   */
  public boolean startLockTask(CallbackContext callbackContext) {

    if (!activityManager.isInLockTaskMode()) {
      activity.startLockTask();
    }

    initLockTaskInteval(activityManager);

    callbackContext.success();
    return true;
  }

  /**
   * Use this function to stop lockTask mode
   *
   * @param callbackContext
   *        Cordova callback context
   * @return true, valid action
   */
  public boolean stopLockTask(CallbackContext callbackContext) {
    if (activityManager.isInLockTaskMode()) {
      activity.stopLockTask();
    }

    callbackContext.success();
    return true;
  }

  /**
   * Is Lock task mode supported?
   */
  protected boolean isLockTaskSupported(CallbackContext callbackContext)
  {
    boolean supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    PluginResult res = new PluginResult(PluginResult.Status.OK, supported);
    callbackContext.sendPluginResult(res);

    return true;
  }

  static void fireEvent (String event) {
    String js = "window.LockTask.fireEvent(\"" + event + "\")";
    sendJavascript(js);
  }

  /**
   * Use this instead of deprecated sendJavascript
   *
   * @param js
   *      JS code snippet as string
   */
  private static synchronized void sendJavascript(final String js) {
    Runnable jsLoader = new Runnable() {
      public void run() {
        webView.loadUrl("javascript:" + js);
      }
    };

    try {
      Method post = webView.getClass().getMethod("post", Runnable.class);
      post.invoke(webView, jsLoader);
    } catch(Exception e) {
      ((Activity)(webView.getContext())).runOnUiThread(jsLoader);
    }
  }

  private static void initLockTaskInteval(final ActivityManager activityManager) {
    handlerLockTaskActive = new Handler();
    handlerLockTaskActive.postDelayed(new Runnable() {
      private boolean isStarted = false;
      @Override
      public void run() {
        if (!activityManager.isInLockTaskMode() && isStarted) {
          handlerLockTaskActive.removeCallbacks(this);
          fireEvent("LockTaskModeExiting");
        } else {
          if(activityManager.isInLockTaskMode() && !isStarted) {
            fireEvent("LockTaskModeEntering");
            isStarted = true;
          }
          handlerLockTaskActive.postDelayed(this, 1000);
        }
      }
    }, 1000);
  }
}