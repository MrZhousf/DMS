package com.mydms.dms.core;

import com.mydms.dms.core.bean.DataInfo;
import com.mydms.dms.core.realm.RealmUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;


/**
 * 控制器基类
 */
public abstract class BaseController<E extends RealmObject> {

    String failureDetail = "获取数据失败";

    public abstract List<E> doHttp(Class clazz, Object[] params) throws Exception;

    public void onFailureDetail(String failureDetail){
        this.failureDetail = failureDetail;
    }

    protected List<E> modelToList(E t){
        if(null == t)
            return null;
        List<E> list = new ArrayList<E>();
        list.add(t);
        return list;
    }

    //获取网络数据
    public DataInfo<E> fetchRemoteData(Class clazz, Object... params){
        DataInfo<E> info = new DataInfo<>();
        info.setClazz(clazz);
        info.setSuccess(false);
        info.setRetDetail(failureDetail);
        try {
            //网络请求
            List<E> list = doHttp(clazz,params);
            //保存数据库
            if(null != list){
                info.setSuccess(true);
                info.setList(list);
                if(!insertBeforeDeleteAll(list)){
                    info.setSuccess(false);
                    info.setRetDetail(failureDetail+"[插入数据库失败]");
                }
            }
        } catch (Exception e){
            info.setRetDetail(failureDetail+":"+e.getMessage());
        }
        return info;
    }

    //获取本地数据
    public DataInfo<E> fetchLocalData(Class clazz){
        DataInfo<E> info = new DataInfo<>();
        info.setClazz(clazz);
        info.setSuccess(false);
        try {
            //查询数据库
            List<E> list = RealmUtil.getInstance().findAll(clazz);
            if(null != list && list.size() > 0){
                info.setSuccess(true);
                info.setList(list);
            }else{
                info.setRetDetail(failureDetail);
            }
        } catch (Exception e){
            info.setRetDetail(failureDetail+":"+e.getMessage());
        }
        return info;
    }

    //保存数据
    private boolean insertBeforeDeleteAll(List<E> list){
        return RealmUtil.getInstance().insertBeforeDeleteAll(list);
    }





}
