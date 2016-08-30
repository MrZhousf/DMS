package com.mydms;

import android.view.View;
import android.widget.TextView;

import com.mydms.base.BaseActivity;
import com.mydms.dms.core.DMS;
import com.mydms.dms.core.callback.Callback;
import com.mydms.dms.model.UserInfo;
import com.mydms.util.LogUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @Bind(R.id.tvOnlyHttp)
    TextView tvOnlyHttp;
    @Bind(R.id.tvOnlyLocal)
    TextView tvOnlyLocal;
    @Bind(R.id.tvData)
    TextView tvData;

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    private String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";


    @OnClick({R.id.getOnlyHttp, R.id.getOnlyLocal, R.id.getData})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getOnlyHttp:
                DMS.getDefault().getOnlyHttp(callbackOnlyHttp, url);
                break;
            case R.id.getOnlyLocal:
                DMS.getDefault().getOnlyLocal(callbackOnlyLocal);
                break;
            case R.id.getData:
                DMS.getDefault().get(callbackAuto);
                break;
        }
    }

    Callback callbackOnlyLocal = new Callback<UserInfo>(UserInfo.class) {
        @Override
        public void success(UserInfo model) {
            tvOnlyLocal.setText(model.toString());
        }

        @Override
        public void failure(String msg) {
            tvOnlyLocal.setText(msg);
        }

        @Override
        public void success(List<UserInfo> list) {
            for (UserInfo u : list) {
                LogUtil.d("onlyLocal", u.toString());
            }
        }
    };

    Callback callbackOnlyHttp = new Callback<UserInfo>(UserInfo.class) {
        @Override
        public void success(UserInfo model) {
            tvOnlyHttp.setText(model.toString());
        }

        @Override
        public void failure(String msg) {
            tvOnlyHttp.setText(msg);
        }

        @Override
        public void success(List<UserInfo> list) {
            for (UserInfo u : list) {
                LogUtil.d("onlyHttp", u.toString());
            }
        }
    };

    Callback callbackAuto = new Callback<UserInfo>(UserInfo.class) {
        @Override
        public void success(UserInfo model) {
            tvData.setText(model.toString());
        }

        @Override
        public void failure(String msg) {
            tvData.setText(msg);
        }

        @Override
        public void success(List<UserInfo> list) {
            for (UserInfo u : list) {
                LogUtil.d("auto", u.toString());
            }
        }
    };

}
