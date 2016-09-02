package com.mydms.dms.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mydms.dms.BaseDMS;
import com.mydms.dms.model.Weather;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * 天气信息DMS
 * @author: zhousf
 */
public class DMSWeather extends BaseDMS<Weather> {

    String url = "http://api.k780.com:88/?app=weather.future&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";

    private static DMSWeather singleton;

    public DMSWeather(){
        super.init();
    }

    public static DMSWeather getInstance(){
        if(null == singleton){
            synchronized (DMSWeather.class){
                if(null == singleton){
                    singleton = new DMSWeather();
                }
            }
        }
        return singleton;
    }

    @Override
    protected Class initModelClass() {
        return Weather.class;
    }

    @Override
    protected List<Weather> doHttp(Object[] params) {
        List<Weather> list = null;
        HttpInfo info = HttpInfo.Builder().setUrl(url).build();
        OkHttpUtil.getDefault().doGetSync(info);
        if(info.isSuccessful()){
            try {
                JSONObject jo = new JSONObject(info.getRetDetail());
                String result = jo.optString("result");
                list = new Gson().fromJson(result,new TypeToken<List<Weather>>(){}.getType());
            }catch (Exception e){
                setFailedResult("网络返回数据解析错误");
            }
        }else {
            setFailedResult(info.getRetDetail());
        }
        return list;
    }


}
