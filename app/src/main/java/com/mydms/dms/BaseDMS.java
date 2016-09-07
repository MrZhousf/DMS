package com.mydms.dms;

import android.os.Message;

import com.mydms.core.realm.RealmUtil;
import com.mydms.dms.bean.Result;
import com.mydms.dms.handler.DMSMainHandler;
import com.mydms.dms.handler.MainMessage;
import com.mydms.dms.listener.DMSChangeListener;
import com.mydms.dms.listener.DMSListener;

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

import io.realm.RealmObject;

/**
 * DMS基类
 * @author: zhousf
 */
public abstract class BaseDMS<T extends RealmObject> implements BaseDMSInterface<T> {

    protected abstract List<T> doHttp(Object[] params);

    final List<DMSChangeListener<T>> changeListeners = new CopyOnWriteArrayList<>();

    static ExecutorService executorService;

    T model;

    Class<T> clazz;

    List<T> modelList;

    String failedResult;

    public BaseDMS() {
        init();
    }

    @Override
    public final void push(final DMSListener<T> listener, final Object... params) {
        //初始化线程池
        initExecutorService();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //初始化回调信息
                Result<T> result = new Result<T>();
                result.setSuccessful(false);
                failedResult = "获取网络数据失败";
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
                        }
                    }else{
                        result.setRetDetail(failedResult);
                    }
                } catch (Exception e){
                    result.setRetDetail(failedResult+":"+e.getMessage());
                }
                //操作结果回调
                if(null != listener){
                    Message resMsg = new MainMessage<T>(
                            DMSMainHandler.CALLBACK_RESPONSE,
                            result,
                            listener)
                            .build();
                    DMSMainHandler.getInstance().sendMessage(resMsg);
                }
                initModel();
            }
        });
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

    boolean insertBeforeDeleteAll(List<T> model){
        return RealmUtil.getInstance().insertBeforeDeleteAll(model);
    }

    protected List<T> getDataFromDB(Class clazz){
        return RealmUtil.getInstance().findAll(clazz);
    }

    /**
     * 初始化数据模型
     */
    void init(){
        //初始化线程池
        initExecutorService();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                initModel();
            }
        });
    }

    void initModel(){
        initClazz();
        if(null == clazz)
            throw new IllegalArgumentException("please override the abstract method initModelClass " +
                    "and return an value of available!");
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
    public final boolean updateModel(List<T> modelList) {
        if(null != modelList && modelList.size() > 0){
            List<T> list = RealmUtil.getInstance().update(modelList);
            if(null != list && list.size() > 0){
                init();
                Result<T> result = new Result<T>();
                result.setModel(list.get(0));
                result.setModelList(list);
                result.setSuccessful(true);
                result.setRetDetail("操作成功");
                //数据改变回调
                changeModelCallback(result);
                return true;
            }
        }
        return false;
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

}
