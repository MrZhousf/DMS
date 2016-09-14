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


    public String getWeaid() {
        return weaid;
    }

    public void setWeaid(String weaid) {
        this.weaid = weaid;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getCityno() {
        return cityno;
    }

    public void setCityno(String cityno) {
        this.cityno = cityno;
    }

    public String getCitynm() {
        return citynm;
    }

    public void setCitynm(String citynm) {
        this.citynm = citynm;
    }

    public String getCityid() {
        return cityid;
    }

    public void setCityid(String cityid) {
        this.cityid = cityid;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWeather_icon() {
        return weather_icon;
    }

    public void setWeather_icon(String weather_icon) {
        this.weather_icon = weather_icon;
    }

    public String getWeather_icon1() {
        return weather_icon1;
    }

    public void setWeather_icon1(String weather_icon1) {
        this.weather_icon1 = weather_icon1;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWinp() {
        return winp;
    }

    public void setWinp(String winp) {
        this.winp = winp;
    }

    public String getTemp_high() {
        return temp_high;
    }

    public void setTemp_high(String temp_high) {
        this.temp_high = temp_high;
    }

    public String getTemp_low() {
        return temp_low;
    }

    public void setTemp_low(String temp_low) {
        this.temp_low = temp_low;
    }
}
