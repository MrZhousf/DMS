package com.mydms.core.realm;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

/**
 */
public class RealmUtil {

    private final String TAG = getClass().getSimpleName();

    private static RealmUtil singleton;

    private static boolean isShowLog = true;

    public static RealmUtil getInstance(){
        if(null == singleton){
            synchronized (RealmUtil.class){
                if(null ==singleton)
                    singleton = new RealmUtil();
            }
        }
        return singleton;
    }

    public static void init(RealmConfiguration configuration,boolean showLog){
        if(null != configuration)
            Realm.setDefaultConfiguration(configuration);
        isShowLog = showLog;
    }

    public Realm getRealm(){
        Realm realm = Realm.getDefaultInstance();
        commitTransaction(realm);
        return realm;
    }

    public <E extends RealmObject> boolean insertBeforeDeleteAll(E table){
        if(null != table){
            List<E> tables = new ArrayList<>();
            tables.add(table);
            return insert(true,tables);
        }
        return false;
    }

    public <E extends RealmObject> boolean insertBeforeDeleteAll(List<E> tables){
        return insert(true,tables);
    }

    public <E extends RealmObject> boolean insert(E table){
        if(null != table){
            List<E> tables = new ArrayList<>();
            tables.add(table);
            return insert(false,tables);
        }
        return false;
    }

    public <E extends RealmObject> boolean insert(List<E> tables){
        return insert(false,tables);
    }

    public <E extends RealmObject> boolean insert(boolean deleteAllBeforeInsert,List<E> tables){
        if(null != tables && tables.size() > 0){
            Realm realm = null;
            try {
                realm = getRealm();
                realm.beginTransaction();
                if(deleteAllBeforeInsert){
                    realm.where(tables.get(0).getClass()).findAll().deleteAllFromRealm();
                }
                if(realm.copyToRealm(tables).size()>0){
                    showLog(INFO,"添加"+tables.get(0).getClass().getSimpleName()+"成功 ");
                    return true;
                }else{
                    showLog(ERROR,"添加"+tables.get(0).getClass().getSimpleName()+"失败 ");
                }
            }catch (RealmPrimaryKeyConstraintException e){
                showLog(ERROR,"添加"+tables.get(0).getClass().getSimpleName()+"失败[insert]：主键重复 "+e.getMessage());
                cancelTransaction(realm);
            }catch (Exception e){
                showLog(ERROR,"添加"+tables.get(0).getClass().getSimpleName()+"失败[insert]："+e.getMessage());
                cancelTransaction(realm);
            }finally {
                commitTransaction(realm);
            }
        }
        return false;
    }

    public <T extends RealmObject> boolean deleteAll(Class<T> table){
        Boolean bResult = false;
        Realm realm = null;
        try {
            realm = getRealm();
            realm.beginTransaction();
            RealmResults results = realm.where(table).findAll();
            bResult = results.deleteAllFromRealm();
            if(bResult){
                showLog(INFO,"删除"+table.getSimpleName()+"成功");
            }else{
                showLog(ERROR,"删除"+table.getSimpleName()+"失败");
            }
        } catch (Exception e){
            showLog(ERROR,"删除"+table.getSimpleName()+"失败[deleteAll]："+e.getMessage());
            cancelTransaction(realm);
        } finally {
            commitTransaction(realm);
        }
        return bResult;
    }

    public <E extends RealmObject> List<E> update(List<E> tables){
        List<E> element = null;
        if(null != tables && tables.size() > 0) {
            Realm realm = null;
            try {
                realm = getRealm();
                realm.beginTransaction();
                element = realm.copyToRealmOrUpdate(tables);
                showLog(INFO, "更新" + tables.get(0).getClass().getSimpleName() + "成功");
            } catch (Exception e) {
                showLog(ERROR, "更新" + tables.get(0).getClass().getSimpleName() + "失败[update]：" + e.getMessage());
                cancelTransaction(realm);
            } finally {
                commitTransaction(realm);
            }
        }
        return element;
    }

    public <E extends RealmObject> E update(E table){
        if(null != table){
            List<E> tables = new ArrayList<>();
            tables.add(table);
            List<E> list = update(tables);
            if(null != list && list.size() == 1){
                return list.get(0);
            }else{
                return null;
            }
        }
        return null;
    }

    public <E extends RealmObject> List<E> findAll(Class table){
        List<E> list = new ArrayList<>();
        Realm realm = null;
        try {
            realm = getRealm();
            realm.beginTransaction();
            RealmResults results = realm.where(table).findAll();
            list.addAll(results);
            //不托管，禁止持久化
            list = realm.copyFromRealm(list);
        } catch (Exception e){
            showLog(ERROR,"查询"+table.getClass().getSimpleName()+"失败[findAll]："+e.getMessage());
            cancelTransaction(realm);
        }finally {
            commitTransaction(realm);
        }
        return list;
    }

    public boolean deleteAll(){
        try {
            RealmUtil.getInstance().getRealm().executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void cancelTransaction(Realm realm){
        if(null != realm)
            realm.cancelTransaction();
    }

    public static void commitTransaction(Realm realm){
        if(null != realm && realm.isInTransaction()){
            realm.commitTransaction();
        }
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





}
