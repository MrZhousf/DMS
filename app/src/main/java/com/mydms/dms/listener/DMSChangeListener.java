package com.mydms.dms.listener;

import java.util.List;

import io.realm.RealmObject;

/**
 * DMS改变监听
 * @author: zhousf
 */
public interface DMSChangeListener<T extends RealmObject> {

    void onChange(T model, List<T> list);

}
