# DMS
基于Realm、OkHttp3封装的公共数据管理系统
###公共数据：
    共用、常用的数据，例如用户信息、偏好设置、项目配置、规则等
###公共数据管理系统：
    对公共数据进行统一管理与维护，并提供简洁的API，在数据源改变时自动回调更新UI的以数据为核心的数据管理系统，简称DMS
##编写背景：
    任何项目中都应该有公共数据的维护系统，然而公共数据的维护也是一项耗时耗力的工作。
    以用户信息为例：
    * 方案一：采用静态类
        用户信息包含用户的基本属性，状态等，许多项目中会简单使用静态类来保存用户信息。
        采用静态类保存用户信息可能在手机内存不够的情况下被虚拟机释放（请参考jvm垃圾回收机制）。
        一旦用户静态类被释放，项目中调用时会面临着这空指针而导致app崩溃的风险。
    * 方案二：采用数据库+单例
        将用户信息保存到数据库，采用单例实例化数据库中的用户信息，这样方式的确避免了被释放的危险，但没有处理用户信息联动效应。
        若用户信息在改变时，其他使用的地方就没有办法获取最新信息并刷新UI了。
    * 结合以上需求以及问题，编写一个简洁的数据管理系统，提供公共数据单例，集合数据改变回调通知业务。
##相关示例
###获取用户信息
```java
DMSUserInfo.getInstance().getModel()
```
###Push用户信息
```java
DMSUserInfo.getInstance().push(userInfoDMSListener);
//用户信息回调
    DMSListener<UserInfo> userInfoDMSListener = new DMSListener<UserInfo>() {
        @Override
        public void onResponse(Result<UserInfo> result) {
            if (result.isSuccessful()) {
                Toast.makeText(WelcomeActivity.this,"Push用户信息成功",Toast.LENGTH_SHORT).show();
                tvPush.setText(result.getModel().toString());
            } else {
                tvPush.setText(result.getRetDetail());
            }
        }
    };
```
###用户信息改变监听
```java
//增加用户信息监听
DMSUserInfo.getInstance().addChangeListener(userInfoDMSChangeListener);

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
```

##相关截图
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/1.jpg?raw=true)