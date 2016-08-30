package com.mydms.dms.core;

import android.util.Log;

import com.mydms.dms.core.bean.DataInfo;
import com.mydms.dms.core.callback.Callback;
import com.mydms.dms.core.handler.CallbackMessage;
import com.mydms.dms.core.handler.DMSHandler;
import com.mydms.dms.core.realm.RealmUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.RealmConfiguration;
import io.realm.RealmObject;


/**
 * 数据管理系统
 */
public class DMS {

    private final String TAG = getClass().getSimpleName();

    private static DMS singleton;

    /**
     * 模型-控制器
     */
    Map<Class,BaseController> modelControllerMap = new ConcurrentHashMap<>();
    /**
     * 模型-状态
     */
    Map<Class,Boolean> modelSwitchMap = new ConcurrentHashMap<>();
    /**
     * 模型-回调
     */
    Map<Class,CopyOnWriteArrayList<Callback>> callbackMap = new ConcurrentHashMap<>();

    ExecutorService executorService;
    boolean isShowLog = true;

    private static DMS getInstance(Builder builder){
        if(null == singleton){
            synchronized (DMS.class){
                if(null == singleton){
                    singleton = new DMS(builder);
                }
            }
        }
        return singleton;
    }

    public static DMS getDefault(){
        if(null == singleton){
            throw new IllegalArgumentException("DMS is null!");
        }
        return singleton;
    }

    public DMS(Builder builder) {
        //初始化线程池
        executorService = Executors.newCachedThreadPool();
        modelControllerMap = builder.modelControllerMap;
        isShowLog = builder.isShowLog;
        RealmUtil.init(builder.realmConfiguration,builder.isShowRealmLog);
        //初始化模型状态
        for(Map.Entry<Class,BaseController> entry : modelControllerMap.entrySet()){
            modelSwitchMap.put(entry.getKey(),true);
        }
    }

    final int Method_Auto = 1;
    final int Method_OnlyHttp = 2;
    final int Method_OnlyLocal = 3;

    /**
     * 仅从网络获取数据
     */
    public <E extends RealmObject> void getOnlyHttp(Callback<E> callBack, Object... params){
        get(Method_OnlyHttp,callBack,params);
    }

    /**
     * 仅从本地获取数据
     */
    public <E extends RealmObject> void getOnlyLocal(Callback<E> callBack, Object... params){
        get(Method_OnlyLocal,callBack,params);
    }

    /**
     * 获取数据
     */
    public <E extends RealmObject> void get(Callback<E> callBack, Object... params){
        get(Method_Auto,callBack,params);
    }

    <E extends RealmObject> void get(int method, Callback<E> callBack, Object... params){
        if(null == callBack)
            return ;
        Class clazz = callBack.model;
        BaseController<E> controller = modelControllerMap.get(clazz);
        registerCallback(clazz,callBack);//注册回调
        switch (method){
            case Method_Auto:
                if(modelSwitchMap.get(clazz)){
                    fetchRemoteData(clazz,controller,callBack,params);
                }else{
                    fetchLocalData(clazz,controller,callBack,params);
                }
                break;
            case Method_OnlyHttp:
                fetchRemoteData(clazz,controller,callBack,params);
                break;
            case Method_OnlyLocal:
                fetchLocalData(clazz,controller,callBack,params);
                break;
        }
    }

    <E extends RealmObject> void fetchRemoteData(final Class clazz, final BaseController<E> controller, final Callback callBack, final Object... params){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DataInfo<E> info = controller.fetchRemoteData(clazz,params);
                if(!parseResult(true,clazz,info,callBack)){
                    //网络请求失败后尝试本地请求
                    fetchLocalData(clazz,controller,callBack);
                }
            }
        });
    }

    <E extends RealmObject> void fetchLocalData(final Class clazz,final BaseController<E> controller,final Callback callBack,final Object... params) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DataInfo<E> info = controller.fetchLocalData(clazz);
                parseResult(false,clazz,info,callBack);
            }
        });
    }

    <E extends RealmObject> boolean parseResult(boolean isRemote,Class clazz,DataInfo<E> info,Callback<E> callBack){
        String from = isRemote ? "网络" : "本地";
        String result = info.isSuccess() ? "成功" : "失败";
        showLog(DEBUG,"从"+from+"请求数据"+result+"："+clazz.getSimpleName());
        if(info.isSuccess()){//成功
            List<E> list = info.getList();
            if(isRemote){
                modelSwitchMap.put(clazz,false);
                //通知所有模型
                callbackAll(list,clazz);
            }else{
                callback(list,callBack);
            }
            return true;
        }else{//失败
            if(isRemote){
                modelSwitchMap.put(clazz,true);
            }
            DMSHandler.getInstance().sendMessage(
                    new CallbackMessage<E>(
                            DMSHandler.FAILURE_CALLBACK,
                            info.getRetDetail(),
                            callBack)
                            .build()
            );
            return false;
        }
    }

    <E extends RealmObject> void callback(List<E> list,Callback<E> callBack){
        DMSHandler.getInstance().sendMessage(
                new CallbackMessage<E>(
                        DMSHandler.SUCCESS_CALLBACK_LIST,
                        list,
                        callBack)
                        .build()
        );
        if(null != list && list.size() > 0){
            DMSHandler.getInstance().sendMessage(
                    new CallbackMessage<E>(
                            DMSHandler.SUCCESS_CALLBACK_MODEL,
                            list.get(0),
                            callBack)
                            .build()
            );
        }
    }

    <E extends RealmObject> void callbackAll(List<E> list, Class clazz){
        CopyOnWriteArrayList<Callback> callbacks = callbackMap.get(clazz);
        if(null != callbacks){
            for (Callback callback:callbacks){
                callback(list,callback);
            }
        }
    }

    <E extends RealmObject> void registerCallback(Class clazz,Callback<E> callBack){
        CopyOnWriteArrayList<Callback> list = callbackMap.get(clazz);
        if(null == list)
            list = new CopyOnWriteArrayList<>();
        if(!list.contains(callBack))
            list.add(callBack);
        callbackMap.put(clazz,list);
    }

    void showLog(int level,String msg){
        if(isShowLog){
            switch (level){
                case DEBUG:
                    Log.d(TAG,msg);
                    break;
                case ERROR:
                    Log.e(TAG,msg);
                    break;
                case INFO:
                    Log.i(TAG,msg);
                    break;
                case WARN:
                    Log.w(TAG,msg);
                    break;
            }
        }
    }

    private static final int DEBUG = 1;
    private static final int ERROR = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;


    public static Builder Builder(){
        return new Builder();
    }

    public static class Builder{

        Map<Class,BaseController> modelControllerMap = new ConcurrentHashMap<>();
        boolean isShowLog;
        boolean isShowRealmLog;
        RealmConfiguration realmConfiguration;

        public DMS init(){
            return getInstance(this);
        }

        /**
         * 注册MC
         * @param clazz 模型
         * @param controller 控制器
         */
        public <E extends BaseController> Builder addMC(Class clazz, E controller){
            registerMC(clazz,controller);
            return this;
        }

        public Builder showDMSLog(boolean isShowLog){
            this.isShowLog = isShowLog;
            return this;
        }

        public Builder showRealmLog(boolean isShowLog){
            this.isShowRealmLog = isShowLog;
            return this;
        }

        public Builder realmConfiguration(RealmConfiguration realmConfiguration){
            this.realmConfiguration = realmConfiguration;
            return this;
        }

        <E extends BaseController> void registerMC(Class clazz, E controller){
            if(null == clazz )
                throw new IllegalArgumentException("clazz is null!");
            if(null == controller)
                throw new IllegalArgumentException("controller is null!");
            if(modelControllerMap.containsKey(clazz)){
                throw new IllegalArgumentException(controller.getClass().getName()+" has added!");
            }
            modelControllerMap.put(clazz,controller);
        }
    }


}
