package com.example.cmx.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.cmx.coolweather.gson.Weather;
import com.example.cmx.coolweather.util.HttpUtil;
import com.example.cmx.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent ,int flags,int startId){
        updateWeather();
        updateBingPic();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;   //8个小时的毫秒数
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    /*
    更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            String weatherId=weather.basic.weatherId;

            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=b43bb0ed9a594067a46cf9fb1c916c35";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather=Utility.handleWeatherResponse(responseText);
                    if(weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }

            });
        }
    }

    /*
    更新必应每日一图
     */
    private void updateBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }

        });
    }
}
/*
可以看到，在onStartCommand方法中先是调用了updateWeater的方法来更新天气，然后调用了updateBingPic方法来更新背景图片，这里我们将更新后的数据直接存储到SharedPreferences文件中就可以了，因为打开WeatherActivity的时候会优先从SharedPreferences缓存中读取数据
之后就是我们学习过的创建定时任务的技巧了，为了保证软件不会消耗过多的流量，这里将时间间隔设立为8个小时，8小时后AutoUpdateService的onStartCommand方法就会重新执行，这样也就实现后台定时更新的功能了。
不过，我们还需要在代码某处去激活AutoUpdateService方法才行。修改WeatherActivity中的代码

 */
/*
创建一个AutoUpdateService服务，并将Exported和Enabled这两个属性都勾选中
 */
