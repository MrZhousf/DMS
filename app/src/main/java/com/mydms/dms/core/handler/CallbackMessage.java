package com.mydms.dms.core.handler;

import com.mydms.dms.core.callback.Callback;

import java.util.List;

import io.realm.RealmObject;

/**
 * @author: zhousf
 */
public class CallbackMessage<E extends RealmObject> extends DMSMessage {


    List<E> list;
    E model;
    Callback<E> callBack;
    String detail;

    public CallbackMessage(int what,List<E> list, Callback callBack) {
        this.what = what;
        this.list = list;
        this.callBack = callBack;
    }

    public CallbackMessage(int what,E model, Callback callBack) {
        this.what = what;
        this.model = model;
        this.callBack = callBack;
    }


    public CallbackMessage(int what,String detail, Callback<E> callBack) {
        this.what = what;
        this.callBack = callBack;
        this.detail = detail;
    }
}
