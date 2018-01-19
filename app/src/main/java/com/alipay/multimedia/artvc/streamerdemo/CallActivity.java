/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.alipay.multimedia.artvc.streamerdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.alipay.multimedia.streamer.api.ArtvcStreamer;
import com.alipay.multimedia.streamer.api.StreamerConfig;
import com.alipay.multimedia.streamer.api.StreamerConstants;
import com.alipay.multimedia.streamer.api.StreamerErrorCode;
import com.alipay.multimedia.streamer.util.AppRTCUtils;

import org.webrtc.RendererCommon;
import org.webrtc.StatsReport;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends Activity implements CallFragment.OnCallEvents {
  public static final String EXTRA_ROOM_URL = "org.appspot.apprtc.ROOM_URL";
  public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
  public static final String EXTRA_TOKENID = "org.appspot.apprtc.TOKENID";
  public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
  public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
  public static final String EXTRA_CALL_MODE = "org.appspot.apprtc.CALL_MODE";
  public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
  public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
  public static final String EXTRA_BEAUTIFY = "org.appspot.apprtc.BEAUTIFY";
  public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
  public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
  public static final String EXTRA_ENCODER_VIDEO_WIDTH = "org.appspot.apprtc.ENCODER_VIDEO_WIDTH";
  public static final String EXTRA_ENCODER_VIDEO_HEIGHT = "org.appspot.apprtc.ENCODER_VIDEO_HEIGHT";
  public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
  public static final String EXTRA_VIDEO_MIN_FPS = "org.appspot.apprtc.VIDEO_MIN_FPS";
  public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
      "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
  public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
  public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
  public static final String EXTRA_HWCODEC_ACC_ENABLED = "org.appspot.apprtc.HWCODEC.acc";
  public static final String EXTRA_HWCODEC_OPEN = "org.appspot.apprtc.HWCODEC.open";
  public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
  public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
  public static final String EXTRA_AUDIO_SAMPLERATE = "org.appspot.apprtc.AUDIO_SAMPLERATE";
  public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
  public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
      "org.appspot.apprtc.NOAUDIOPROCESSING";
  public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
  public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
  public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
  public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
  public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
  public static final String EXTRA_ENABLE_LEVEL_CONTROL = "org.appspot.apprtc.ENABLE_LEVEL_CONTROL";
  public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
  public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
  public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
  public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
  public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
      "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
      "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
      "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
  public static final String EXTRA_USE_VALUES_FROM_INTENT =
      "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
  private static final String TAG = "CallRTCClient";
  private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

  // List of mandatory application permissions.
  private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
      "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

  // Peer connection statistics callback period in ms.
  private static final int STAT_CALLBACK_PERIOD = 1000;
  // Local preview screen position before call is connected.
  private static final int LOCAL_X_CONNECTING = 0;
  private static final int LOCAL_Y_CONNECTING = 0;
  private static final int LOCAL_WIDTH_CONNECTING = 100;
  private static final int LOCAL_HEIGHT_CONNECTING = 100;
  // Local preview screen position after call is connected.
  private static final int LOCAL_X_CONNECTED = 72;
  private static final int LOCAL_Y_CONNECTED = 72;
  private static final int LOCAL_WIDTH_CONNECTED = 25;
  private static final int LOCAL_HEIGHT_CONNECTED = 25;
  // Remote video screen position
  private static final int REMOTE_X = 0;
  private static final int REMOTE_Y = 0;
  private static final int REMOTE_WIDTH = 100;
  private static final int REMOTE_HEIGHT = 100;

  private PercentFrameLayout localRenderLayout;
  private PercentFrameLayout remoteRenderLayout;
  private RendererCommon.ScalingType scalingType;
  private Toast logToast;
  private boolean commandLineRun;
  private int runTimeMs;
  private boolean activityRunning;
  private boolean iceConnected;
  private boolean isError;
  private boolean callControlFragmentVisible = false;
  private long callStartedTimeMs = 0;
  private boolean micEnabled = true;
  private boolean screencaptureEnabled = false;
  private static Intent mediaProjectionPermissionResultData;
  private static int mediaProjectionPermissionResultCode;

  // Controls
  private CallFragment callFragment;
  private HudFragment hudFragment;
  private CpuMonitor cpuMonitor;

  private ArtvcStreamer streamer;
  private StreamerConfig config;
  private String roomId;
  private String token;
  private String userId;

  private ArtvcStreamer.OnErrorListener errorListener = new ArtvcStreamer.OnErrorListener() {
    @Override
    public void onError(StreamerErrorCode what, String id, String msg) {
      Log.e(TAG,"Error:" + what);
    }
  };

  private ArtvcStreamer.OnInfoListener infoListener = new ArtvcStreamer.OnInfoListener() {
    @Override
    public void onInfo(StreamerErrorCode what, String id, Object obj) {
      Log.i(TAG,"Info:" + what);
      switch (what){
        case STREAMER_INFO_CREATE_ROOM_SUCCESS:
          roomId = id;
          token = (String)obj;
          toggleCallControlFragmentVisibility();
          break;
        case STREAMER_INFO_GET_STAT_SUCCESS:
          StatsReport[] report = (StatsReport[])obj;
          hudFragment.updateEncoderStatistics(report);
          break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

    // Set window styles for fullscreen-window size. Needs to be done before
    // adding content.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON
        | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | LayoutParams.FLAG_TURN_SCREEN_ON);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    final Intent intent = getIntent();
    setContentView(R.layout.multichat_call);

    // Check for mandatory permissions.
    for (String permission : MANDATORY_PERMISSIONS) {
      if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        logAndToast("Permission " + permission + " is not granted");
        setResult(RESULT_CANCELED);
        finish();
        return;
      }
    }

    //Uri roomUri = intent.getData();
    String roomUri = intent.getStringExtra(EXTRA_ROOM_URL);
    if (roomUri == null) {
      logAndToast(getString(R.string.missing_url));
      Log.e(TAG, "Didn't get any URL in intent!");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    // Get Intent parameters.
    roomId = intent.getStringExtra(EXTRA_ROOMID);
    Log.d(TAG, "Room ID: " + roomId);

    String codecName = intent.getStringExtra(EXTRA_VIDEOCODEC);
    Boolean openHwEncode = intent.getBooleanExtra(EXTRA_HWCODEC_OPEN,false);

    // Create CPU monitor
    cpuMonitor = new CpuMonitor(this);

    callFragment = new CallFragment();
    hudFragment = new HudFragment();
    hudFragment.setCpuMonitor(cpuMonitor);

    // Send intent arguments to fragments.
    callFragment.setArguments(intent.getExtras());
    hudFragment.setArguments(intent.getExtras());

    // Activate call and HUD fragments and start the call.
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    ft.add(R.id.call_fragment_container, callFragment);
    ft.add(R.id.hud_fragment_container, hudFragment);
    ft.commit();

    userId = String.valueOf(System.currentTimeMillis());
    config = new StreamerConfig(userId,roomUri,StreamerConstants.DEFAULT_SIGNATURE);
    config.mVideoCodecId = AppRTCUtils.getCodecId(codecName);
    config.mEncodeMethod = openHwEncode ? StreamerConstants.ENCODE_METHOD_HARDWARE : StreamerConstants.ENCODE_METHOD_SOFTWARE;

    streamer = new ArtvcStreamer(this,config);
    streamer.setOnErrorListener(errorListener);
    streamer.setOnInfoListener(infoListener);
    streamer.startStream();
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
      return;
    mediaProjectionPermissionResultCode = resultCode;
    mediaProjectionPermissionResultData = data;

    streamer.startStream();
  }


  // Activity interfaces
  @Override
  public void onPause() {
    super.onPause();
    activityRunning = false;
    streamer.onPause();
    cpuMonitor.pause();
  }

  @Override
  public void onResume() {
    super.onResume();
    activityRunning = true;
    cpuMonitor.resume();
    streamer.onResume();
  }

  @Override
  protected void onDestroy() {
    disconnect();
    if (logToast != null) {
      logToast.cancel();
    }
    activityRunning = false;
    super.onDestroy();
  }

  // CallFragment.OnCallEvents interface implementation.
  @Override
  public void onCallHangUp() {
    disconnect();
  }

  @Override
  public void onCameraSwitch() {
  }

  @Override
  public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {

  }

  @Override
  public void onCaptureFormatChange(int width, int height, int framerate) {

  }

  @Override
  public boolean onToggleMic() {
    return micEnabled;
  }

  @Override
  public boolean onSwitchVoice(boolean videoOn,boolean trickByButton) {
    return false;
  }

  @Override
  public boolean onSwitchLocalVideo(boolean videoOn) {
    return false;
  }

  // Helper functions.
  private void toggleCallControlFragmentVisibility() {
    if (!callFragment.isAdded()) {
      return;
    }
    // Show/hide call control fragment
    callControlFragmentVisible = !callControlFragmentVisible;
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (callControlFragmentVisible) {
      callFragment.showRoomId("rid:" + roomId +
              "\ntid:" + token
              +"\nuid:" + userId );
      ft.show(callFragment);
      ft.show(hudFragment);
    } else {
      ft.hide(callFragment);
      ft.hide(hudFragment);
    }
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
  }

  // Disconnect from remote resources, dispose of local resources, and exit.
  private void disconnect() {
    activityRunning = false;
    streamer.stopStream();
    if (iceConnected && !isError) {
      setResult(RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
    }
    finish();
  }

  private void disconnectWithErrorMessage(final String errorMessage) {
    if (commandLineRun || !activityRunning) {
      Log.e(TAG, "Critical error: " + errorMessage);
      disconnect();
    } else {
      new AlertDialog.Builder(this)
          .setTitle(getText(R.string.channel_error_title))
          .setMessage(errorMessage)
          .setCancelable(false)
          .setNeutralButton(R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
                  disconnect();
                }
              })
          .create()
          .show();
    }
  }

  // Log |msg| and Toast about it.
  private void logAndToast(String msg) {
    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    logToast.show();
  }

  private void reportError(final String description) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!isError) {
          isError = true;
          disconnectWithErrorMessage(description);
        }
      }
    });
  }
}
