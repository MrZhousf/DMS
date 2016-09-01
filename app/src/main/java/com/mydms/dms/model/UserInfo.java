package com.mydms.dms.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 用户信息模型
 * @author: zhousf
 */
public class UserInfo extends RealmObject{

    @PrimaryKey
    private String timestamp;
    private String datetime_1;
    private String datetime_2;
    private String week_1;
    private String week_2;
    private String week_3;
    private String week_4;

    @Override
    public String toString() {
        return timestamp+" | "+datetime_1+" | "+datetime_2+" | "+week_1+" | "+week_2+" | "+week_3+" | "+week_4;
    }

}
