package com.mydms.dms.listener;

import com.mydms.dms.bean.Result;

import io.realm.RealmObject;

/**
 * DMS（push）结果监听
 * @author: zhousf
 */
public interface DMSListener<T extends RealmObject> {

    void onResponse(Result<T> result);

}
