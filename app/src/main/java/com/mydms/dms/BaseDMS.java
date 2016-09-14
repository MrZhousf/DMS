package com.mydms.dms;

import android.os.Message;
import android.util.Log;

import com.mydms.core.realm.RealmUtil;
import com.mydms.dms.bean.Result;
import com.mydms.dms.handler.DMSMainHandler;
import com.mydms.dms.handler.MainMessage;
import com.mydms.dms.listener.DMSChangeListener;
import com.mydms.dms.listener.DMSPushListener;
import com.mydms.dms.rule.ModelConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;

/**
 * DMS基类
 * @author: zhousf
 */
public abstract class BaseDMS<T extends RealmObject> implements BaseDMSInterface<T> {

    /**
     * 缓存规则：单位（秒）
     */
    protected final int CacheRule_Permanent = -1;
    protected final int CacheRule_Zero = 0;
    protected final int CacheRule_First = 15;
    protected final int CacheRule_Second = 60;
    protected final int CacheRule_Third = 300;

    /**
     * 网络请求
     */
    protected abstract List<T> doHttp(Object[] params);

    /**
     * 初始化缓存规则
     */
    protected abstract int initCacheRule();

    /**
     * 业务状态
     */
    public enum Status{
        INIT,
        PUSHING,
        SUCCESS,
        FAILED
    }

    Status status;

    final List<DMSChangeListener<T>> changeListeners = new CopyOnWriteArrayList<>();

    static ExecutorService executorService;

    T model;

    Class<T> clazz;

    String clazzName;

    List<T> modelList;

    String failedResult;

    public BaseDMS() {
        init();
    }

    @Override
    public final void push(final DMSPushListener<T> listener, final Object... params) {
        status = Status.PUSHING;
        //初始化线程池
        initExecutorService();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //初始化回调信息
                Result<T> result = new Result<T>();
                result.setSuccessful(false);
                failedResult = "获取网络数据失败";
                if(needHttp()){
                    //网络请求
                    status = httpRequest(listener,result) ? Status.SUCCESS : Status.FAILED;
                }else{
                    //缓存请求
                    status = cacheRequest(listener,result) ? Status.SUCCESS : Status.FAILED;
                }
                showLog("push "+clazzName+": "+status);
            }
        });
    }

    /**
     * 缓存规则判断
     */
    boolean needHttp(){
        int time = initCacheRule();
        long timestampNow = System.currentTimeMillis();
        long timestampLast = fetchLastTime();
        //时间差
        long timeDeference = (timestampNow - timestampLast) / 1000;
        return timestampLast == -1 || (timeDeference >= time && time != CacheRule_Permanent);
    }

    long fetchLastTime(){
        long timestampLast = -1;
        Realm realm = RealmUtil.getInstance().getRealm();
        ModelConfig config = realm.where(ModelConfig.class).equalTo("modelName",clazzName).findFirst();
        if(null != config){
            return config.getLastUpdateTime();
        }
        return timestampLast;
    }

    boolean initModelConfig(){
        Realm realm = RealmUtil.getInstance().getRealm();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmObjectSchema table = realm.getSchema().get(ModelConfig.class.getSimpleName());
                    if(null == table){
                        //若没有模型配置表则创建
                        realm.createObject(ModelConfig.class);
                    }
                    ModelConfig record = realm.where(ModelConfig.class)
                            .equalTo("modelName",clazzName)
                            .findFirst();
                    //对自定义模型进行首次配置
                    if(null == record){
                        ModelConfig modelConfig = new ModelConfig();
                        modelConfig.setModelName(clazzName);
                        modelConfig.setLastUpdateTime(CacheRule_Permanent);
                        realm.insertOrUpdate(modelConfig);
                        showLog("模型["+clazzName+"]初始化配置成功");
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            showLog("模型["+clazzName+"]初始化配置失败");
            return false;
        }
        return true;
    }

    boolean cacheRequest(DMSPushListener<T> listener, Result<T> result){
        showLog("缓存请求"+"["+clazzName+"]");
        result.setModel(model);
        result.setModelList(modelList);
        result.setSuccessful(true);
        result.setRetDetail("操作成功");
        //操作结果回调
        callbackResponse(listener,result);
        return true;
    }

    boolean httpRequest(DMSPushListener<T> listener, Result<T> result, Object... params){
        boolean isSuccess = false;
        showLog("网络请求"+"["+clazzName+"]");
        try {
            //从网络获取数据
            List<T> list = doHttp(params);
            if(null != list && list.size() > 0){
                //保存数据库
                if(insertBeforeDeleteAll(list)){
                    T model = list.get(0);
                    result.setModel(model);
                    result.setModelList(list);
                    result.setSuccessful(true);
                    result.setRetDetail("操作成功");
                    //数据改变回调
                    changeModelCallback(result);
                    isSuccess = true;
                }
            }else{
                result.setRetDetail(failedResult);
            }
        } catch (Exception e){
            result.setRetDetail(failedResult+":"+e.getMessage());
        }
        //操作结果回调
        callbackResponse(listener,result);
        initModel();
        return isSuccess;
    }

    void callbackResponse(DMSPushListener<T> listener, Result<T> result){
        if(null != listener){
            Message resMsg = new MainMessage<T>(
                    DMSMainHandler.CALLBACK_RESPONSE,
                    result,
                    listener)
                    .build();
            DMSMainHandler.getInstance().sendMessage(resMsg);
        }
    }

    @Override
    public final void addChangeListener(DMSChangeListener<T> listener) {
        if (null == listener) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (!changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    @Override
    public final void removeChangeListener(DMSChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if(changeListeners.contains(listener)){
            changeListeners.remove(listener);
        }
    }

    protected List<T> modelToList(T model){
        List<T> list = new ArrayList<>();
        if(null != model)
            list.add(model);
        return list;
    }

    protected void setFailedResult(String failedResult) {
        this.failedResult = failedResult;
    }

    /**
     * 模型改变时回调
     */
    void changeModelCallback(Result<T> result){
        try {
            for(DMSChangeListener<T> listener : changeListeners){
                Message changeMsg = new MainMessage<T>(
                        DMSMainHandler.CALLBACK_CHANGE,
                        result,
                        listener)
                        .build();
                DMSMainHandler.getInstance().sendMessage(changeMsg);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected List<T> getDataFromDB(Class clazz){
        return RealmUtil.getInstance().findAll(clazz);
    }

    /**
     * 初始化数据模型
     */
    void init(){
        status = Status.INIT;
        //初始化线程池
        initExecutorService();
        initClazz();
        //初始化模型配置
        initModelConfig();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                initModel();
            }
        });
    }

    void initModel(){
        if(null == clazz)
            throw new IllegalArgumentException("clazz is null");
        List<T> list = getDataFromDB(clazz);
        modelList = new ArrayList<>();
        if(null != list && list.size() > 0){
            modelList = list;
            model = modelList.get(0);
        }else{//自动实例化防止获取数据时空指针
            try {
                model = (T) clazz.newInstance();
                initStringFields(model);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    void initClazz(){
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        clazz = (Class) params[0];
        if(null != clazz)
            clazzName = clazz.getSimpleName();
    }

    void initExecutorService(){
        if(null == executorService){
            executorService = Executors.newCachedThreadPool();
        }
    }

    @Override
    public final T getModel() {
        //返回克隆对象，防止调用时更改对象影响数据模型
        return deepCopy(model);
    }

    @Override
    public final List<T> getModelList() {
        try {
            List<T> newList = new ArrayList<>();
            for(T m : modelList){
                newList.add(deepCopy(m));
            }
            return newList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return modelList;
    }

    @Override
    public final boolean updateModel(T model) {
        return updateModel(modelToList(model));
    }

    @Override
    public final boolean updateModel(final List<T> modelList) {
        if(null != modelList && modelList.size() > 0){
            Realm realm = RealmUtil.getInstance().getRealm();
            try {
                realm.beginTransaction();
                //更新模型信息
                realm.copyToRealmOrUpdate(modelList);
                //更新模型配置信息:更新模型后的下次请求强制采用网络方式
                ModelConfig modelConfig = realm.where(ModelConfig.class).equalTo("modelName",clazzName).findFirst();
                modelConfig.setLastUpdateTime(CacheRule_Permanent);
                //初始化数据模型
                init();
                //数据改变回调
                Result<T> result = new Result<T>();
                result.setModel(modelList.get(0));
                result.setModelList(modelList);
                result.setSuccessful(true);
                result.setRetDetail("操作成功");
                changeModelCallback(result);
                return true;
            } catch (Exception e){
                e.printStackTrace();
                RealmUtil.cancelTransaction(realm);
                return false;
            } finally {
                RealmUtil.commitTransaction(realm);
            }
        }
        return false;
    }

    boolean insertBeforeDeleteAll(List<T> model){
        Realm realm = RealmUtil.getInstance().getRealm();
        try {
            realm.beginTransaction();
            //先删除
            realm.where(model.get(0).getClass()).findAll().deleteAllFromRealm();
            //再保存
            if(realm.copyToRealm(model).size() > 0){
                //更新模型配置信息
                ModelConfig modelConfig = realm.where(ModelConfig.class).equalTo("modelName",clazzName).findFirst();
                modelConfig.setLastUpdateTime(System.currentTimeMillis());
            }else{
                RealmUtil.cancelTransaction(realm);
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            RealmUtil.cancelTransaction(realm);
            return false;
        } finally {
            RealmUtil.commitTransaction(realm);
        }
        return true;
    }

    /**
     * 深度复制
     */
    T deepCopy(T model){
        T newModel = null;
        try {
            newModel = (T) clazz.newInstance();
            copyPropertiesExclude(model,newModel,null);
            return newModel;
        }catch (Exception e){
            e.printStackTrace();
        }
        return newModel;
    }

    /**
     * 复制对象属性
     * @param from 被复制对象
     * @param to 赋值对象
     * @param exclude 排除属性列表
     */
    void copyPropertiesExclude(Object from, Object to, String[] exclude) throws Exception {
        List<String> excludesList = null;
        if(exclude != null && exclude.length > 0) {
            excludesList = Arrays.asList(exclude); //构造列表对象
        }
        Method[] fromMethods = from.getClass().getDeclaredMethods();
        Method[] toMethods = to.getClass().getDeclaredMethods();
        Method fromMethod = null, toMethod = null;
        String fromMethodName = null, toMethodName = null;
        for (int i = 0; i < fromMethods.length; i++) {
            fromMethod = fromMethods[i];
            fromMethodName = fromMethod.getName();
            if (!fromMethodName.contains("get"))
                continue;
            //排除列表检测
            if(excludesList != null && excludesList.contains(fromMethodName.substring(3).toLowerCase())) {
                continue;
            }
            toMethodName = "set" + fromMethodName.substring(3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null)
                continue;
            Object value = fromMethod.invoke(from, new Object[0]);
            if(value == null)
                continue;
            //集合类判空处理
            if(value instanceof Collection) {
                Collection newValue = (Collection)value;
                if(newValue.size() <= 0)
                    continue;
            }
            toMethod.invoke(to, new Object[] {value});
        }
    }

    /**
     * 从方法数组中获取指定名称的方法
     */
    Method findMethodByName(Method[] methods, String name) {
        for (int j = 0; j < methods.length; j++) {
            if (methods[j].getName().equals(name))
                return methods[j];
        }
        return null;
    }


    /**
     * 初始化String成员变量
     */
    void initStringFields(T model){
        Field[] fields = model.getClass().getDeclaredFields();
        if(null != fields){
            try {
                for(int i=0;i<fields.length;i++){
                    if(fields[i].getType() == String.class){
                        fields[i].setAccessible(true);
                        fields[i].set(model,"");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    void showLog(String msg){
        Log.d(clazzName, msg);
    }

}
