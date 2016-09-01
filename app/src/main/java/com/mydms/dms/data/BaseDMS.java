package com.mydms.dms.data;

import android.os.Message;

import com.mydms.dms.bean.Result;
import com.mydms.core.realm.RealmUtil;
import com.mydms.dms.handler.DMSMainHandler;
import com.mydms.dms.handler.MainMessage;
import com.mydms.dms.listener.DMSListener;
import com.mydms.dms.listener.DMSChangeListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.RealmObject;

/**
 * DMS基类
 * @author: zhousf
 */
public abstract class BaseDMS<T extends RealmObject> {

    protected abstract List<T> doHttp(Object[] params);

    protected abstract Class initModelClass();

    final List<DMSChangeListener<T>> changeListeners = new CopyOnWriteArrayList<>();

    static ExecutorService executorService;

    T model;

    List<T> modelList;

    String failedResult;

    /**
     * 发送网络获取数据命令
     * @param listener 监听器
     * @param params 参数
     */
    public void push(final DMSListener<T> listener, final Object... params){
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
                init();
            }
        });
    }

    /**
     * 添加DMS模型改变监听
     */
    public void addChangeListener(DMSChangeListener<T> listener){
        if (null == listener) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (!changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    /**
     * 移除DMS模型改变监听
     */
    public void removeChangeListener(DMSChangeListener listener){
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

    void initExecutorService(){
        if(null == executorService){
            executorService = Executors.newCachedThreadPool();
        }
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

    List<T> getDataFromDB(Class clazz){
        return RealmUtil.getInstance().findAll(clazz);
    }

    /**
     * 初始化数据模型
     */
    protected void init(){
        Class clazz = initModelClass();
        if(null == clazz)
            throw new IllegalArgumentException("please override the abstract method initModelClass and return an value of available!");
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

    public T getModel() {
        return model;
    }

    public List<T> getModelList() {
        return modelList;
    }



}
