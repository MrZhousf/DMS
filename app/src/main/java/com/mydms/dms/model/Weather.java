package com.mydms.dms.model;

import io.realm.RealmObject;

/**
 * 天气信息模型
 * @author: zhousf
 */
public class Weather extends RealmObject implements Cloneable{

    String weaid;  // 1,
    String days;  // 2016-09-01,
    String week;  // 星期四,
    String cityno;  // beijing,
    String citynm;  // 北京,
    String cityid;  // 101010100,
    String temperature;  // 27℃/19℃,
    String humidity;  // 0℉/0℉,
    String weather;  // 阴,
    String weather_icon;  // http://api.k780.com:88/upload/weather/d/2.gif,
    String weather_icon1;  // http://api.k780.com:88/upload/weather/n/2.gif,
    String wind;  // 北风转无持续风向,
    String winp;  // 3-4级转微风,
    String temp_high;  // 27,
    String temp_low;  // 19,


    @Override
    public String toString() {
        return citynm+" | "+days+" | "+week+" | "+weather+" | "+temperature+" | "+wind+" | "+temp_low+" | "+temp_high;
    }
}
