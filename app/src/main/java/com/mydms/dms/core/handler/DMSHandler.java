package com.mydms.dms.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * @author: zhousf
 */
public class DMSHandler extends Handler {

    private static DMSHandler singleton;

    public static DMSHandler getInstance(){
        if(null == singleton){
            synchronized (DMSHandler.class){
                if(null == singleton)
                    singleton = new DMSHandler();
            }
        }
        return singleton;
    }

    public DMSHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 成功回调
     */
    public static final int SUCCESS_CALLBACK_MODEL = 0x01;
    /**
     * 成功回调
     */
    public static final int SUCCESS_CALLBACK_LIST = 0x02;
    /**
     * 失败回调
     */
    public static final int FAILURE_CALLBACK = 0x03;

    @Override
    public void handleMessage(Message msg) {
        try {
            final int what = msg.what;
            switch (what){
                case SUCCESS_CALLBACK_MODEL:
                    CallbackMessage modelMsg = (CallbackMessage)msg.obj;
                    modelMsg.callBack.success(modelMsg.model);
                    break;
                case SUCCESS_CALLBACK_LIST:
                    CallbackMessage listMsg = (CallbackMessage)msg.obj;
                    listMsg.callBack.success(listMsg.list);
                    break;
                case FAILURE_CALLBACK:
                    CallbackMessage failMsg = (CallbackMessage)msg.obj;
                    failMsg.callBack.failure(failMsg.detail);
                    break;
            }
        } catch (Exception e){

        }
    }
}
