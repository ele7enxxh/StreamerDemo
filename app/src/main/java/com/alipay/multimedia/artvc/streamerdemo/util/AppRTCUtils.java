/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.alipay.multimedia.artvc.streamerdemo.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.webrtc.SessionDescription;

import java.util.regex.Pattern;

/**
 * AppRTCUtils provides helper functions for managing thread safety.
 */
public final class AppRTCUtils {
  private AppRTCUtils() {}

  /** Helper method which throws an exception  when an assertion has failed. */
  public static void assertIsTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Expected condition to be true");
    }
  }

  /** Helper method for building a string of thread information.*/
  public static String getThreadInfo() {
    return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
        + "]";
  }

  /** Information about the current build, taken from system properties. */
  public static void logDeviceInfo(String tag) {
    Log.d(tag, "Android SDK: " + Build.VERSION.SDK_INT + ", "
            + "Release: " + Build.VERSION.RELEASE + ", "
            + "Brand: " + Build.BRAND + ", "
            + "Device: " + Build.DEVICE + ", "
            + "Id: " + Build.ID + ", "
            + "Hardware: " + Build.HARDWARE + ", "
            + "Manufacturer: " + Build.MANUFACTURER + ", "
            + "Model: " + Build.MODEL + ", "
            + "Product: " + Build.PRODUCT);
  }

  public static final String regular = "m=video.*a=inactive";
  public static final Pattern pattern = Pattern.compile(regular,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  public static boolean isAuidoCallOnly(SessionDescription sdp){
    if(sdp != null && !TextUtils.isEmpty(sdp.description)){
      return pattern.matcher(sdp.description).find() || !sdp.description.contains("m=video");
    }
    return false;
  }
}
