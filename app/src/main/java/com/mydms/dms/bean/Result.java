package com.mydms.dms.bean;

import java.util.List;

import io.realm.RealmObject;

/**
 * 返回结果信息体
 * @author: zhousf
 */
public class Result<T extends RealmObject> {

    T model;

    List<T> modelList;

    boolean isSuccessful;

    String retDetail;

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    public List<T> getModelList() {
        return modelList;
    }

    public void setModelList(List<T> modelList) {
        this.modelList = modelList;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getRetDetail() {
        return retDetail;
    }

    public void setRetDetail(String retDetail) {
        this.retDetail = retDetail;
    }
}
