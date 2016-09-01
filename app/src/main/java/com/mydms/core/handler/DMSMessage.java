package com.mydms.core.handler;


import android.os.Message;

import java.io.Serializable;

/**
 * Handler信息体基类
 * @author zhousf
 */
public class DMSMessage implements Serializable {

    public int what;

    public Message build(){
        Message msg = new Message();
        msg.what = this.what;
        msg.obj = this;
        return msg;
    }

}
