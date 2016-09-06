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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDatetime_1() {
        return datetime_1;
    }

    public void setDatetime_1(String datetime_1) {
        this.datetime_1 = datetime_1;
    }

    public String getDatetime_2() {
        return datetime_2;
    }

    public void setDatetime_2(String datetime_2) {
        this.datetime_2 = datetime_2;
    }

    public String getWeek_1() {
        return week_1;
    }

    public void setWeek_1(String week_1) {
        this.week_1 = week_1;
    }

    public String getWeek_2() {
        return week_2;
    }

    public void setWeek_2(String week_2) {
        this.week_2 = week_2;
    }

    public String getWeek_3() {
        return week_3;
    }

    public void setWeek_3(String week_3) {
        this.week_3 = week_3;
    }

    public String getWeek_4() {
        return week_4;
    }

    public void setWeek_4(String week_4) {
        this.week_4 = week_4;
    }
}
