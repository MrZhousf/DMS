package com.mydms.dms.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mydms.dms.bean.Result;
import com.mydms.dms.listener.DMSChangeListener;
import com.mydms.dms.listener.DMSListener;

/**
 * DMS主线程句柄
 * @author: zhousf
 */
public class DMSMainHandler extends Handler {

    private static DMSMainHandler singleton;

    public static DMSMainHandler getInstance(){
        if(null == singleton){
            synchronized (DMSMainHandler.class){
                if(null == singleton)
                    singleton = new DMSMainHandler();
            }
        }
        return singleton;
    }

    public DMSMainHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 结果回调
     */
    public static final int CALLBACK_RESPONSE = 0x01;
    /**
     * 改变回调
     */
    public static final int CALLBACK_CHANGE = 0x02;

    @Override
    public void handleMessage(Message msg) {
        try {
            final int what = msg.what;
            switch (what){
                case CALLBACK_RESPONSE:
                    MainMessage resMsg = (MainMessage) msg.obj;
                    DMSListener listener = resMsg.dmsListener;
                    if(null != listener)
                        listener.onResponse(resMsg.result);
                    break;
                case CALLBACK_CHANGE:
                    MainMessage changeMsg = (MainMessage) msg.obj;
                    DMSChangeListener cListener = changeMsg.dmsChangeListener;
                    Result result = changeMsg.result;
                    if(null != cListener)
                        cListener.onChange(result.getModel(),result.getModelList());
                    break;
            }
        } catch (Exception e){

        }
    }
}
