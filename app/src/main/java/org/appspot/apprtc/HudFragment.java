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

import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.webrtc.Logging;
import org.webrtc.StatsReport;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment for HUD statistics display.
 */
public class HudFragment extends Fragment {
  private View controlView;
  private TextView encoderStatView;
  private TextView hudViewBwe;
  private TextView hudViewConnection;
  private TextView hudViewVideoSend;
  private TextView hudViewVideoRecv;
  private ImageButton toggleDebugButton;
  private boolean videoCallEnabled;
  private boolean displayHud;
  private volatile boolean isRunning;
  private CpuMonitor cpuMonitor;
  private String lastBytesSent = null;
  private String lastBytesRecv = null;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    controlView = inflater.inflate(R.layout.fragment_hud, container, false);

    // Create UI controls.
    encoderStatView = (TextView) controlView.findViewById(R.id.encoder_stat_call);
    hudViewBwe = (TextView) controlView.findViewById(R.id.hud_stat_bwe);
    hudViewConnection = (TextView) controlView.findViewById(R.id.hud_stat_connection);
    hudViewVideoSend = (TextView) controlView.findViewById(R.id.hud_stat_video_send);
    hudViewVideoRecv = (TextView) controlView.findViewById(R.id.hud_stat_video_recv);
    toggleDebugButton = (ImageButton) controlView.findViewById(R.id.button_toggle_debug);

    toggleDebugButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (displayHud) {
          int visibility =
              (hudViewBwe.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE;
          hudViewsSetProperties(visibility);
        }
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    Bundle args = getArguments();
    if (args != null) {
      videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
      displayHud = args.getBoolean(CallActivity.EXTRA_DISPLAY_HUD, false);
    }
    int visibility = displayHud ? View.VISIBLE : View.INVISIBLE;
    encoderStatView.setVisibility(visibility);
    toggleDebugButton.setVisibility(visibility);
    hudViewsSetProperties(View.INVISIBLE);
    isRunning = true;
  }

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();
  }

  public void setCpuMonitor(CpuMonitor cpuMonitor) {
    this.cpuMonitor = cpuMonitor;
  }

  private void hudViewsSetProperties(int visibility) {
    hudViewBwe.setVisibility(visibility);
    hudViewConnection.setVisibility(visibility);
    hudViewVideoSend.setVisibility(visibility);
    hudViewVideoRecv.setVisibility(visibility);
    hudViewBwe.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
    hudViewConnection.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
    hudViewVideoSend.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
    hudViewVideoRecv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
  }

  private Map<String, String> getReportMap(StatsReport report) {
    Map<String, String> reportMap = new HashMap<String, String>();
    for (StatsReport.Value value : report.values) {
      reportMap.put(value.name, value.value);
    }
    return reportMap;
  }

  public String updateEncoderStatistics(final StatsReport[] reports) {
    if (!isRunning || !displayHud) {
      return "";
    }
    StringBuilder encoderStat = new StringBuilder(128);
    StringBuilder bweStat = new StringBuilder();
    StringBuilder connectionStat = new StringBuilder();
    StringBuilder videoSendStat = new StringBuilder();
    StringBuilder videoRecvStat = new StringBuilder();
    StringBuilder fpsStat = new StringBuilder();
    String fps = null;
    String targetBitrate = null;
    String actualBitrate = null;
    int framesEncoded = 0;
    int pqSum = 0;

    for (StatsReport report : reports) {
      if (report.type.equals("ssrc") && report.id.contains("ssrc") && report.id.contains("send")) {
        // Send video statistics.
        Map<String, String> reportMap = getReportMap(report);
        String trackId = reportMap.get("googTrackId");
        if (trackId != null) {
          fps = reportMap.get("googFrameRateSent");
          fpsStat.append("googFrameRateSent=").append(fps).append(' ');
          videoSendStat.append(report.id).append("\n");
          for (StatsReport.Value value : report.values) {
            String name = value.name.replace("goog", "");
            if(name.equals("qpSum")){
              pqSum = Integer.parseInt(value.value);
            } else if(name.equals("framesEncoded")){
              framesEncoded = Integer.parseInt(value.value);
            }
            //记录每秒发送了多少字节
            if(name.equals("bytesSent")){
              if(lastBytesSent != null){
                int diff = Integer.valueOf(value.value) - Integer.valueOf(lastBytesSent);
                videoSendStat.append(name).append("=").append(value.value).append('/').append(diff).append("\n");
              }else{
                videoSendStat.append(name).append("=").append(value.value).append("\n");
              }
              lastBytesSent = value.value;
            } else {
              videoSendStat.append(name).append("=").append(value.value).append("\n");
            }
          }
        }else{
          for (StatsReport.Value value : report.values) {
              if(value.name.equals("googCodecPayloadType")){
                Logging.w("HudFragment","Audio codec playload type is " + value.value);
              }
            if(value.name.equals("googCodecName")){
              Logging.w("HudFragment","Audio codec name is " + value.value);
            }
          }
        }
      } else if (report.type.equals("ssrc") && report.id.contains("ssrc")
          && report.id.contains("recv")) {
        // Receive video statistics.
        Map<String, String> reportMap = getReportMap(report);
        // Check if this stat is for video track.
        String frameWidth = reportMap.get("googFrameWidthReceived");
        if (frameWidth != null) {
          videoRecvStat.append(report.id).append("\n");
          for (StatsReport.Value value : report.values) {
            String name = value.name.replace("goog", "");
            //记录每秒接收了多少字节
            if(name.equals("bytesReceived")){
              if(lastBytesRecv != null){
                int diff = Integer.valueOf(value.value) - Integer.valueOf(lastBytesRecv);
                videoRecvStat.append(name).append("=").append(value.value).append('/').append(diff).append("\n");
              }else{
                videoRecvStat.append(name).append("=").append(value.value).append("\n");
              }
              lastBytesRecv = value.value;
            } else {
              videoRecvStat.append(name).append("=").append(value.value).append("\n");
            }
          }
        }
      } else if (report.id.equals("bweforvideo")) {
        // BWE statistics.
        Map<String, String> reportMap = getReportMap(report);
        targetBitrate = reportMap.get("googTargetEncBitrate");
        actualBitrate = reportMap.get("googActualEncBitrate");

        bweStat.append(report.id).append("\n");
        for (StatsReport.Value value : report.values) {
          String name = value.name.replace("goog", "").replace("Available", "");
          bweStat.append(name).append("=").append(value.value).append("\n");
          if(name.equals("SendBandwidth")){
            fpsStat.append("googAvailableSendBandwidth").append('=').append(value.value).append(' ');
          }
        }
      } else if (report.type.equals("googCandidatePair")) {
        // Connection statistics.
        Map<String, String> reportMap = getReportMap(report);
        String activeConnection = reportMap.get("googActiveConnection");
        if (activeConnection != null && activeConnection.equals("true")) {
          connectionStat.append(report.id).append("\n");
          for (StatsReport.Value value : report.values) {
            String name = value.name.replace("goog", "");
            connectionStat.append(name).append("=").append(value.value).append("\n");
          }
        }
      }
    }
    hudViewBwe.setText(bweStat.toString());
    hudViewConnection.setText(connectionStat.toString());
    hudViewVideoSend.setText(videoSendStat.toString());
    hudViewVideoRecv.setText(videoRecvStat.toString());

    if (videoCallEnabled) {
      if (fps != null) {
        encoderStat.append("Fps:  ").append(fps).append("\n");
      }
      if (targetBitrate != null) {
        encoderStat.append("Target BR: ").append(targetBitrate).append("\n");
      }
      if (actualBitrate != null) {
        encoderStat.append("Actual BR: ").append(actualBitrate).append("\n");
      }
      if(framesEncoded != 0) {
        float avgQp = pqSum / (float) framesEncoded;
        encoderStat.append("Avg QP: ").append(avgQp).append("\n");
      }
    }

    if (cpuMonitor != null) {
      encoderStat.append("CPU%: ")
          .append(cpuMonitor.getCpuUsageCurrent())
          .append("/")
          .append(cpuMonitor.getCpuUsageAverage())
          .append(". Freq: ")
          .append(cpuMonitor.getFrequencyScaleAverage());
    }
    encoderStatView.setText(encoderStat.toString());
    return fpsStat.toString();
  }
}
