package com.mydms.dms.listener;

import com.mydms.dms.bean.Result;

import io.realm.RealmObject;

/**
 * DMS（push）结果监听
 * @author: zhousf
 */
public interface DMSPushListener<T extends RealmObject> {

    void onPushed(Result<T> result);

}
