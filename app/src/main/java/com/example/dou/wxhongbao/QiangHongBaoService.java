package com.example.dou.wxhongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class QiangHongBaoService extends AccessibilityService {

    private static final int MSG_BACK_HOME = 0;
    private static final int MSG_BACK_ONCE = 1;
    //该对象代表整个窗口试图快照
    private AccessibilityNodeInfo mRootNodeInfo = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType(); // 事件类型
        if (eventType == 64){
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence text : texts) {
                    String content = text.toString();
                    //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                    if (content.contains("[微信红包]")) {
                        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        mRootNodeInfo = event.getSource();
        if (mRootNodeInfo == null) {
            return;
        }
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                List<AccessibilityNodeInfo> hongbaoList = mRootNodeInfo.findAccessibilityNodeInfosByText("微信红包");
                if (hongbaoList.size() > 0) {
                    AccessibilityNodeInfo mCurrent = hongbaoList.get(hongbaoList.size() - 1);
                    //得到微信红包view的父视图进行点击操作
                    mCurrent.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                List<AccessibilityNodeInfo> infoDetails = mRootNodeInfo.findAccessibilityNodeInfosByText("红包详情");
                if (infoDetails != null && infoDetails.size() > 0) {
                    backToHome();
                    return;
                }

                List<AccessibilityNodeInfo> infoSlows = mRootNodeInfo.findAccessibilityNodeInfosByText("手慢了");
                if (infoSlows != null && infoSlows.size() > 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = infoSlows.get(infoSlows.size() - 1);
                    accessibilityNodeInfo.getParent().getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                }

                List<AccessibilityNodeInfo> infoKais = mRootNodeInfo.findAccessibilityNodeInfosByText("发了一个红包");
                if (infoKais != null && infoKais.size() > 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = infoKais.get(infoKais.size() - 1);
                    int size = accessibilityNodeInfo.getParent().getChildCount();
                    Log.d("sunxu_log", "size-->" + size);
                    for (int i = 0; i < size; i++) {
                        AccessibilityNodeInfo testNode = accessibilityNodeInfo.getParent().getChild(i);
                        testNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    return;
                }
                break;
            }
        }

    private void backToHome() {
        if(handler.hasMessages(MSG_BACK_HOME)) {
            handler.removeMessages(MSG_BACK_HOME);
        }
        handler.sendEmptyMessage(MSG_BACK_HOME);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_BACK_HOME) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                    }
                }, 1500);
            } else if(msg.what == MSG_BACK_ONCE) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                    }
                }, 1500);
            }
        }
    };

    @Override
    public void onInterrupt() {

    }
}
