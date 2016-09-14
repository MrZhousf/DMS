package com.mydms.dms;

import com.mydms.dms.listener.DMSChangeListener;
import com.mydms.dms.listener.DMSPushListener;

import java.util.List;

import io.realm.RealmObject;

/**
 * DMS业务接口
 * @author: zhousf
 */
public interface BaseDMSInterface<T extends RealmObject> {

    /**
     * 发送网络获取数据命令
     * @param listener 监听器
     * @param params 参数
     */
    void push(final DMSPushListener<T> listener, final Object... params);

    /**
     * 获取Model
     */
    T getModel();

    /**
     * 获取ModelList
     */
    List<T> getModelList();

    /**
     * 更新ModelList
     * @param modelList 模型集合
     */
    boolean updateModel(List<T> modelList);

    /**
     * 更新Model
     * @param model 模型
     */
    boolean updateModel(T model);

    /**
     * 添加DMS模型改变监听
     * @param listener 监听器
     */
    void addChangeListener(DMSChangeListener<T> listener);

    /**
     * 移除DMS模型改变监听
     * @param listener 监听器
     */
    void removeChangeListener(DMSChangeListener listener);

}
