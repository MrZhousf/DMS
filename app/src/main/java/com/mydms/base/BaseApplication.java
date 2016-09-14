package com.mydms.base;

import android.app.Application;
import android.os.Environment;

import com.mydms.core.DMS;
import com.mydms.core.controller.UserInfoController;
import com.mydms.core.realm.Migration;
import com.mydms.core.realm.RealmUtil;
import com.mydms.dms.data.DMSUserInfo;
import com.mydms.dms.data.DMSWeather;
import com.mydms.dms.model.UserInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheLevel;
import com.okhttplib.annotation.CacheType;

import io.realm.RealmConfiguration;

/**
 * Application
 * 1、初始化全局OkHttpUtil
 * @author zhousf
 */
public class BaseApplication extends Application {

    public static BaseApplication baseApplication;

    public static BaseApplication getApplication() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        initOkHttpUtil();
        initRealm();
        initDMS();
    }

    void initDMS(){
        com.mydms.dms.DMS.Builder()
                .addDMS(DMSUserInfo.getInstance())
                .addDMS(DMSWeather.getInstance())
                .init();
    }

    void initOkHttpUtil(){
        String downloadFileDir = Environment.getExternalStorageDirectory().getPath()+"/okHttp_download/";
        OkHttpUtil.init(this)
                .setConnectTimeout(30)//连接超时时间
                .setWriteTimeout(30)//写超时时间
                .setReadTimeout(30)//读超时时间
                .setMaxCacheSize(10 * 1024 * 1024)//缓存空间大小
                .setCacheLevel(CacheLevel.FIRST_LEVEL)//缓存等级
                .setCacheType(CacheType.NETWORK_THEN_CACHE)//缓存类型
                .setShowHttpLog(false)//显示请求日志
                .setShowLifecycleLog(false)//显示Activity销毁日志
                .setRetryOnConnectionFailure(false)//失败后不自动重连
                .setDownloadFileDir(downloadFileDir)//文件下载保存目录
                .build();
    }

    void initRealm(){
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder(BaseApplication.getApplication())
                .name("realm.realm")//配置名字
                .encryptionKey(new byte[64])
                .schemaVersion(1)//版本号
                .migration(new Migration())//数据库升级/迁移
                .deleteRealmIfMigrationNeeded()
                .build();
        RealmUtil.init(realmConfiguration,true);
    }

    void initDMS_Old(){
        DMS.Builder()
                .addMC(UserInfo.class,new UserInfoController())
                .showDMSLog(true)
                .init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
