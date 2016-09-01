package com.mydms.dms.handler;

import android.os.Message;

import com.mydms.dms.bean.Result;
import com.mydms.dms.listener.DMSChangeListener;
import com.mydms.dms.listener.DMSListener;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * 主线程消息体
 * @author: zhousf
 */
public class MainMessage<T extends RealmObject> implements Serializable {

    public int what;

    Result<T> result;

    DMSListener<T> dmsListener;

    DMSChangeListener<T> dmsChangeListener;

    public MainMessage(int what, Result<T> result, DMSListener<T> dmsListener) {
        this.what = what;
        this.result = result;
        this.dmsListener = dmsListener;
    }

    public MainMessage(int what, Result<T> result, DMSChangeListener<T> dmsChangeListener) {
        this.what = what;
        this.result = result;
        this.dmsChangeListener = dmsChangeListener;
    }

    public Message build(){
        Message msg = new Message();
        msg.what = this.what;
        msg.obj = this;
        return msg;
    }

}
