package com.mydms.dms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DMS初始化控制中心
 * 对于非业务的公共数据进行统一初始化
 * @author: zhousf
 */
public class DMS {

    static ExecutorService executorService;

    List<BaseDMS> dmsList = new CopyOnWriteArrayList<>();

    final int dealingThreadNum = 2;//处理线程数
    final int loopTimes = 3;//循环次数
    final int loopDelayTime = 15;//循环延迟时间（单位：秒）

    public DMS(Builder builder) {
        dmsList = builder.dmsList;
        init(dmsList);
    }

    void init(List<BaseDMS> dmsList){
        //初始化线程池
        initExecutorService();
        //数据分组
        List<List<BaseDMS>> list = new ArrayList<>();
        for(int group=1;group<=dealingThreadNum;group++){
            List<BaseDMS> temp = new ArrayList<>();
            for(int i=group;i<=dmsList.size();i=i+dealingThreadNum){
                temp.add(dmsList.get(i-1));
            }
            list.add(temp);
        }
        for(final List<BaseDMS> deal : list){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    dealDMS(deal);
                }
            });
        }
    }

    void dealDMS(List<BaseDMS> list){
        int loop = 0;
        List<BaseDMS> removeList = new ArrayList<>();
        while (null!=list && !list.isEmpty() && loop<loopTimes){
            removeList.clear();
            for(BaseDMS dms : list){
                if(dms.status == BaseDMS.Status.SUCCESS){
                    removeList.add(dms);
                    continue;
                }
                if(dms.status == BaseDMS.Status.INIT
                        || dms.status == BaseDMS.Status.FAILED){
                    dms.push(null);
                }
            }
            if(!removeList.isEmpty())
                list.removeAll(removeList);
            loop ++;
            try {
                if(!list.isEmpty())
                    Thread.sleep(loopDelayTime*1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    void initExecutorService(){
        if(null == executorService){
            executorService = Executors.newCachedThreadPool();
        }
    }



    public static Builder Builder(){
        return new Builder();
    }

    public static class Builder {

        List<BaseDMS> dmsList = new CopyOnWriteArrayList<>();

        public DMS init(){
            return new DMS(this);
        }

        public <E extends BaseDMS> Builder addDMS(E element) {
            dmsList.add(element);
            return this;
        }


    }



}
