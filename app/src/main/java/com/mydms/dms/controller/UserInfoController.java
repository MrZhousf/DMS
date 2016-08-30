package com.mydms.dms.controller;


import com.google.gson.Gson;
import com.mydms.dms.core.BaseController;
import com.mydms.dms.model.UserInfo;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;

import org.json.JSONObject;

import java.util.List;


/**
 */
public class UserInfoController extends BaseController<UserInfo> {


    @Override
    public List<UserInfo> doHttp(Class clazz, Object[] params)  throws Exception {
        HttpInfo info = HttpInfo.Builder().setUrl(params[0].toString()).build();
        OkHttpUtil.getDefault().doGetSync(info);
        if(info.isSuccessful()){
            try {
                JSONObject jo = new JSONObject(info.getRetDetail());
                String result = jo.optString("result");
                UserInfo user = new Gson().fromJson(result,UserInfo.class);
                return  modelToList(user);
            }catch (Exception e){
                onFailureDetail("接口参数解析错误");
            }
        }else {
            onFailureDetail(info.getRetDetail());
        }
        return null;
    }




}
