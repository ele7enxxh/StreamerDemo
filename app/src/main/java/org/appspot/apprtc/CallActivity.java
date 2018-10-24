/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.alipay.multimedia.streamer.api.ArtvcStreamer;
import com.alipay.multimedia.streamer.api.StreamerConfig;
import com.alipay.multimedia.streamer.api.StreamerConstants;
import com.alipay.multimedia.streamer.api.StreamerCode;
import com.alipay.multimedia.streamer.util.AppRTCUtils;

import org.webrtc.RendererCommon;
import org.webrtc.StatsReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends Activity implements CallFragment.OnCallEvents {
  public static final String EXTRA_ROOM_URL = "org.appspot.apprtc.ROOM_URL";
  public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
  public static final String EXTRA_USERID = "org.appspot.apprtc.USERID";
  public static final String EXTRA_SIGN = "org.appspot.apprtc.SIGN";
  public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
  public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
  public static final String EXTRA_AUDIO_CALL = "org.appspot.apprtc.AUDIO_CALL";
  public static final String EXTRA_PULL_MODE = "org.appspot.apprtc.PULL_MODE";
  public static final String EXTRA_PUSH_MODE = "org.appspot.apprtc.PUSH_MODE";
  public static final String EXTRA_STREAM_ENCRYPT = "org.appspot.apprtc.STREAM_ENCRYPT";
  public static final String EXTRA_RECORD_AUDIO = "org.appspot.apprtc.RECORD_AUDIO";
  public static final String EXTRA_BIZNAME = "org.appspot.apprtc.BIZNAME";
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
  private String bizName;
  private String roomId;
  private String token;
  private String sign;
  private String userId;

  private final List<View> renders = new ArrayList<View>();
  private final Map<String,View> activeRenders = new HashMap<>();

  private ArtvcStreamer.OnErrorListener errorListener = new ArtvcStreamer.OnErrorListener() {
    @Override
    public void onError(StreamerCode what, String id, String msg) {
      StringBuilder error = new StringBuilder();
      error.append(what).append('\n')
              .append("id:").append(id).append('\n')
              .append("msg:").append(msg);
      logAndToast(error.toString());
      switch (what){
        case STREAMER_ERROR_ICE_ERROR:
          Log.e(TAG,"ice error,begin to retry");
          toggleCallControlFragmentVisibility();
          //streamer.restartStream(StreamerConstants.DEFAULT_SIGN);
          break;
        case STREAMER_ERROR_FROM_CAMERA:
          Log.e(TAG,"camera " + id + " error:" + msg);
          streamer.restartCamera(id);
          break;
        case STREAMER_ERROR_PUSH_TIMEOUT:
          Log.e(TAG,"stream to server overtime");
          break;
      }
    }
  };

  private ArtvcStreamer.OnInfoListener infoListener = new ArtvcStreamer.OnInfoListener() {
    @Override
    public void onInfo(StreamerCode what, String id, Object obj) {
      Log.i(TAG,"Info:" + what);
      switch (what){
        case STREAMER_INFO_GET_ROOM_INFO_SUCCESS:
          roomId = id;
          token = (String)obj;
          toggleCallControlFragmentVisibility();
          break;
        case STREAMER_INFO_GET_STAT_SUCCESS:
          StatsReport[] report = (StatsReport[])obj;
          hudFragment.updateEncoderStatistics(report);
          break;
        case STREAMER_INFO_PUSH_SUCCESS:
          Log.i(TAG,id + " stream to server success");
          break;
        case STREAMER_INFO_ON_LOCAL_STREAM_TO_RENDER:
          for(View view: renders) {
            if(view.getVisibility() == View.INVISIBLE){
                streamer.showLocalStream(id, view);
                activeRenders.put(id,view);
                break;
            }
          }
          break;
        case STREAMER_INFO_ON_REMOTE_STREAM_TO_RENDER:
          for(View view: renders) {
            if(view.getVisibility() == View.INVISIBLE){
              streamer.showRemoteStream(id, view);
              activeRenders.put(id,view);
              break;
            }
          }
          break;
        case STREAMER_INFO_ON_STREAM_TO_DISAPEAR:
        case STREAMER_INFO_ON_RMV_STREAM:
          View view = activeRenders.get(id);
          if(view != null){
            view.setVisibility(View.INVISIBLE);
            activeRenders.remove(id);
          }
          //两人视频的场景，对方已经退出
          int active = streamer.getActiveConnectionNum();
          Log.i(TAG,"some one leave,active:" + active);
          if(active <= 1){
            disconnect();
          }
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

    renders.add(findViewById(R.id.video_view1));
    renders.add(findViewById(R.id.video_view2));
    renders.add(findViewById(R.id.video_view3));
    renders.add(findViewById(R.id.video_view4));
    renders.add(findViewById(R.id.video_view5));
    renders.add(findViewById(R.id.video_view6));
    renders.add(findViewById(R.id.video_view7));
    renders.add(findViewById(R.id.video_view8));
    renders.add(findViewById(R.id.video_view9));
    for(View view:renders){
      view.setVisibility(View.INVISIBLE);
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
    bizName = intent.getStringExtra(EXTRA_BIZNAME);

    // Get Intent parameters.
    roomId = intent.getStringExtra(EXTRA_ROOMID);
    sign = intent.getStringExtra(EXTRA_SIGN);
    userId = intent.getStringExtra(EXTRA_USERID);
    if(TextUtils.isEmpty(userId)){
      userId = String.valueOf(System.currentTimeMillis());
    }

    String codecName = intent.getStringExtra(EXTRA_VIDEOCODEC);
    Boolean openHwEncode = intent.getBooleanExtra(EXTRA_HWCODEC_OPEN,false);
    Boolean streamEncrypted = intent.getBooleanExtra(EXTRA_STREAM_ENCRYPT,true);
    Boolean recordAudio = intent.getBooleanExtra(EXTRA_RECORD_AUDIO,false);
    Boolean pullMode = intent.getBooleanExtra(EXTRA_PULL_MODE,false);
    Boolean pushMode = intent.getBooleanExtra(EXTRA_PUSH_MODE,false);
    Boolean videoCall = intent.getBooleanExtra(EXTRA_VIDEO_CALL,false);
    Boolean audioCall = intent.getBooleanExtra(EXTRA_AUDIO_CALL,false);
    int videoFps = intent.getIntExtra(EXTRA_VIDEO_FPS,15);

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

    int targetEncWidth = intent.getIntExtra(EXTRA_ENCODER_VIDEO_WIDTH,640);
    int targetEncHeight = intent.getIntExtra(EXTRA_ENCODER_VIDEO_HEIGHT,360);
    int targetEncBitrate = intent.getIntExtra(EXTRA_VIDEO_BITRATE,400);

    //roomUri = "wss://artvcroom.alipay.com:443/ws";
    //roomUri = "ws://artvcroom.d3119.dl.alipaydev.com/ws";
    config = new StreamerConfig(userId,roomUri);
    config.mVideoCodecId = AppRTCUtils.getCodecId(codecName);
    config.mEncodeMethod = openHwEncode ? StreamerConstants.ENCODE_METHOD_HARDWARE : StreamerConstants.ENCODE_METHOD_SOFTWARE;
    if(!streamEncrypted){
      config.addOrUpdateOption(StreamerConstants.OPTION_KEY_DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT,StreamerConstants.FALSE);
    }
    if(recordAudio){
      config.addOrUpdateOption(StreamerConstants.OPTION_KEY_SAVE_AUDIO_TO_FILE,StreamerConstants.TRUE);
    }
    config.mTargetResolution = AppRTCUtils.getProfile(targetEncWidth,targetEncHeight);
    config.mVideoKBitrate = targetEncBitrate;

    int idx = bizName.indexOf('_');
    config.mBizName = bizName.substring(0,idx);
    config.mRtcMode = bizName.substring(idx+1);
    config.mSignature = sign;

    //如果roomId为空，那么要设置为主叫
    if(TextUtils.isEmpty(roomId)) {
      config.mRole = StreamerConstants.STREAMER_ROLE_CREATOR;
    }else{
      config.mRole = StreamerConstants.STREAMER_ROLE_JOINER;
      config.mRoomId = roomId;
    }

    //音频还是视频通话
    if(videoCall && audioCall){
      config.mCallModel = StreamerConstants.STREAMER_CALL_VIDEO_AUDIO;
    }else if(videoCall){
      config.mCallModel = StreamerConstants.STREAMER_CALL_ONLY_VIDEO;
    }else if(audioCall){
      config.mCallModel = StreamerConstants.STREAMER_CALL_ONLY_AUDIO;
    }

    //推流还是拉流
    if(pullMode && pushMode){
      config.mStreamModel = StreamerConstants.STREAMER_MODE_PUSH_PULL;
    }else if(pullMode){
      config.mStreamModel = StreamerConstants.STREAMER_MODE_ONLY_PULL;
    }else if(pushMode){
      config.mStreamModel = StreamerConstants.STREAMER_MODE_ONLY_PUSH;
    }
    config.mPreviewFrameRate = videoFps;
    config.mTargetFrameRate = videoFps;
    Log.d(TAG, "room: " + roomId + ",sign:" + sign + ",bizName:" + bizName +
          ",video:" + videoCall + ",audio:" + audioCall +",pull:" + pullMode
          + ",push:" + pushMode + ",fps:" + videoFps);

    streamer = new ArtvcStreamer(this,config);
    streamer.setOnErrorListener(errorListener);
    streamer.setOnInfoListener(infoListener);
    streamer.startStream(StreamerConstants.DEFAULT_SIGN);
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
      return;
    mediaProjectionPermissionResultCode = resultCode;
    mediaProjectionPermissionResultData = data;

    streamer.startStream(StreamerConstants.DEFAULT_SIGN);
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
    renders.clear();
    activeRenders.clear();
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
    logToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
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
