package com.mydms.dms.rule;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 模型配置信息
 * @author: zhousf
 */
public class ModelConfig extends RealmObject {

    @PrimaryKey
    private String modelName;
    private long lastUpdateTime;


    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
