package com.alipay.multimedia.artvc.streamerdemo.util;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;

import org.webrtc.Logging;
import org.webrtc.MediaCodecVideoEncoder;

/**
 * Created by zhoull on 17/3/16.
 */

public class AppMediaCapabilityReporter {

    private static final String VP8_MIME_TYPE = "video/x-vnd.on2.vp8";
    private static final String VP9_MIME_TYPE = "video/x-vnd.on2.vp9";
    private static final String H264_MIME_TYPE = "video/avc";

    private static final String REPORTE_DESTINATION = "http://120.76.204.118:8888/report/";

    /** Information about the current build, taken from system properties. */
    private static String deviceInfo() {
        return  "Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT;
    }

    private static String findHwCodec(String mime) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
            MediaCodecInfo info = null;
            try {
                info = MediaCodecList.getCodecInfoAt(i);
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (info == null || !info.isEncoder()) {
                continue;
            }
            String name = null;
            for (String mimeType : info.getSupportedTypes()) {
                if (mimeType.equals(mime)) {
                    name = info.getName();
                    break;
                }
            }
            if (name == null) {
                continue; // No HW support in this codec; try the next one.
            }

            // Check if HW codec supports known color format.
            MediaCodecInfo.CodecCapabilities capabilities;
            try {
                capabilities = info.getCapabilitiesForType(mime);
            } catch (IllegalArgumentException e) {
                continue;
            }

            builder.append("find codec for mime " + mime + " : " + name + ",color format : ");
            int num=1;
            for (int colorFormat : capabilities.colorFormats) {
                builder.append("0x" + Integer.toHexString(colorFormat) + " ");
                if(num++ % 5 == 0)
                    builder.append("\r\n");
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public static int reportMediaCapability(){

        StringBuilder content = new StringBuilder();
        content.append(deviceInfo() + "\r\n");
        content.append(findHwCodec(VP8_MIME_TYPE));
        content.append(findHwCodec(VP9_MIME_TYPE));
        content.append(findHwCodec(H264_MIME_TYPE));

        new AsyncHttpURLConnection("POST", REPORTE_DESTINATION + Build.MODEL, content.toString()
                , new AsyncHttpURLConnection.AsyncHttpEvents() {
            @Override
            public void onHttpError(String errorMessage) {

            }

            @Override
            public void onHttpComplete(String response) {

            }
        }).send();

        return 0;
    }

}
