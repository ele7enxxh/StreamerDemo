package com.alipay.multimedia.artvc.streamerdemo.util;

import com.alipay.multimedia.streamer.util.SignalUtil;
import org.webrtc.Logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoull on 2017/7/31.
 */
public class ViewPosManager {
    private static final String TAG = "ViewPosManager";
    //private static final ViewPosManager instance = new ViewPosManager(SignalChannelUtil.MAX_PERSONS_IN_ROOM);

    private class ViewItem {
        private String streamId;
        int pos;

        public ViewItem(String streamId,int pos){
            this.streamId = streamId;
            this.pos = pos;
        }
    }
    private List<ViewItem> items = new ArrayList<ViewItem>();

    public ViewPosManager(int num){
        if(num>0 && num <= SignalUtil.MAX_PERSONS_IN_ROOM){
            for(int i=0;i<num;i++){
                ViewItem item = new ViewItem("",-1);
                items.add(item);
            }
        }
    }

    //public static ViewPosManager getInstance(){return instance;}

    public int addViewPos(String streamId){
        int pos = -1;
        int num = items.size();
        for(int i=0;i<num;i++){
            ViewItem item = items.get(i);
            if(item.pos == -1){
                item.pos = i;
                item.streamId = streamId;
                pos = i;
                break;
            }
        }
        Logging.d(TAG,"Add stream " + streamId + ",pos " + pos);
        return pos;
    }

    public int rmvViewPos(String streamId){
        int pos = -1;
        int num = items.size();
        for(int i=0;i<num;i++){
            ViewItem item = items.get(i);
            if(item.streamId.equals(streamId)){
                item.pos = -1;
                item.streamId = "";
                pos = i;
                break;
            }
        }
        Logging.d(TAG,"Rmv stream " + streamId + ",pos " + pos);
        return pos;
    }

    public List<String> getCurrentRemoteStreamIds(){
        List<String> ids = new ArrayList<>();
        for(int i=0;i<items.size();i++){
            ViewItem item = items.get(i);
            if(item.pos != -1){
                Logging.d(TAG,"Get stream " + item.streamId + ",pos " + i);
                ids.add(item.streamId);
            }
        }
        return ids;
    }
}
