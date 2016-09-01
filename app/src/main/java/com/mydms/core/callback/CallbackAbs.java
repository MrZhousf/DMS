package com.mydms.core.callback;

import java.util.List;

/**
 */
public abstract class CallbackAbs<T> {

    public abstract void success(T model);

    public abstract void success(List<T> list);

    public abstract T update(T model);

    public abstract List<T> update(List<T> list);

    public abstract void failure(String msg);



}
