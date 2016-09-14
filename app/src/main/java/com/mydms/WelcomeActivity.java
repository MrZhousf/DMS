package com.mydms;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mydms.base.BaseActivity;
import com.mydms.dms.bean.Result;
import com.mydms.dms.data.DMSUserInfo;
import com.mydms.dms.data.DMSWeather;
import com.mydms.dms.listener.DMSChangeListener;
import com.mydms.dms.listener.DMSPushListener;
import com.mydms.dms.model.UserInfo;
import com.mydms.dms.model.Weather;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * DMS演示
 *
 * @author: zhousf
 */
public class WelcomeActivity extends BaseActivity {

    @Bind(R.id.tvChange)
    TextView tvChange;
    @Bind(R.id.tvPush)
    TextView tvPush;
    @Bind(R.id.tvGetData)
    TextView tvGetData;
    @Bind(R.id.tvPushWeather)
    TextView tvPushWeather;
    @Bind(R.id.tvGetWeather)
    TextView tvGetWeather;

    @Override
    protected int initLayout() {
        return R.layout.activity_welcom;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //增加用户信息监听
        DMSUserInfo.getInstance().addChangeListener(userInfoDMSChangeListener);

    }

    @OnClick({R.id.btnPush, R.id.btnGetData, R.id.btnUpdateData, R.id.btnPushWeather, R.id.btnGetWeather})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPush://push用户信息
                DMSUserInfo.getInstance().push(userInfoDMSPushListener);
                break;
            case R.id.btnGetData://读取用户信息
                tvGetData.setText(DMSUserInfo.getInstance().getModel().toString());
                break;
            case R.id.btnUpdateData://修改用户信息
                UserInfo info = DMSUserInfo.getInstance().getModel();
                info.setDatetime_1("修改用户信息");
                if (DMSUserInfo.getInstance().updateModel(info)) {
                    Toast.makeText(WelcomeActivity.this, "修改用户信息成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnPushWeather://push天气信息
                DMSWeather.getInstance().push(weatherDMSPushListener);
                break;
            case R.id.btnGetWeather://读取天气信息
                tvGetWeather.setText(DMSWeather.getInstance().getModel().toString());
                break;
        }
    }

    //用户信息回调
    DMSPushListener<UserInfo> userInfoDMSPushListener = new DMSPushListener<UserInfo>() {
        @Override
        public void onPushed(Result<UserInfo> result) {
            if (result.isSuccessful()) {
                Toast.makeText(WelcomeActivity.this, "Push用户信息成功", Toast.LENGTH_SHORT).show();
                tvPush.setText(result.getModel().toString());
            } else {
                tvPush.setText(result.getRetDetail());
            }
        }
    };

    //天气信息回调
    DMSPushListener<Weather> weatherDMSPushListener = new DMSPushListener<Weather>() {
        @Override
        public void onPushed(Result<Weather> result) {
            if (result.isSuccessful()) {
                tvPushWeather.setText(result.getModel().toString());
                StringBuilder log = new StringBuilder("******\n");
                for (Weather w : result.getModelList()) {
                    log.append(w.toString() + "\n");
                }
//                LogUtil.d("weather",log.toString());
                Toast.makeText(WelcomeActivity.this, "Push天气信息成功", Toast.LENGTH_SHORT).show();
            } else {
                tvPushWeather.setText(result.getRetDetail());
            }
        }
    };

    //用户信息改变回调
    DMSChangeListener<UserInfo> userInfoDMSChangeListener = new DMSChangeListener<UserInfo>() {
        @Override
        public void onChange(UserInfo model, List<UserInfo> list) {
            tvChange.setText(model.toString());
        }
    };

    @Override
    protected void onDestroy() {
        //删除用户信息监听
        DMSUserInfo.getInstance().removeChangeListener(userInfoDMSChangeListener);
        super.onDestroy();
    }


}
