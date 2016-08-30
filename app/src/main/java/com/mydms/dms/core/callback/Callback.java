package com.mydms.dms.core.callback;

import java.util.List;

import io.realm.RealmObject;

/**
 */
public class Callback<T extends RealmObject> extends CallbackAbs<T> {

    public Class<T> model;

    public Callback(Class<T> model) {
        this.model = model;
    }

    @Override
    public void success(T model) {

    }

    @Override
    public void success(List<T> list) {

    }

    @Override
    public T update(T model) {
        return null;
    }

    @Override
    public List<T> update(List<T> list) {
        return null;
    }

    @Override
    public void failure(String msg) {

    }
}
