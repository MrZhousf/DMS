package com.mydms.dms.data;

import com.google.gson.Gson;
import com.mydms.dms.BaseDMS;
import com.mydms.dms.model.UserInfo;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * 用户信息DMS
 * @author: zhousf
 */
public class DMSUserInfo extends BaseDMS<UserInfo> {

    private static DMSUserInfo singleton;

    String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";

    public static DMSUserInfo getInstance(){
        if(null == singleton){
            synchronized (DMSUserInfo.class){
                if(null == singleton){
                    singleton = new DMSUserInfo();
                }
            }
        }
        return singleton;
    }

    @Override
    protected int initCacheRule() {
        return CacheRule_Permanent;
    }

    @Override
    protected List<UserInfo> doHttp(Object[] params) {
        UserInfo model = null;
        HttpInfo info = HttpInfo.Builder().setUrl(url).build();
        OkHttpUtil.getDefault().doGetSync(info);
        if(info.isSuccessful()){
            try {
                JSONObject jo = new JSONObject(info.getRetDetail());
                String result = jo.optString("result");
                model = new Gson().fromJson(result,UserInfo.class);
            }catch (Exception e){
                setFailedResult("网络返回数据解析错误");
            }
        }else {
            setFailedResult(info.getRetDetail());
        }
        return modelToList(model);
    }





}
