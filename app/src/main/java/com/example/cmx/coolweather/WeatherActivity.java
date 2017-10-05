package com.example.cmx.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.tv.TvContract;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmx.coolweather.gson.Forecast;
import com.example.cmx.coolweather.gson.Weather;
import com.example.cmx.coolweather.util.HttpUtil;
import com.example.cmx.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;
    /*
    这个活动中的代码也比较长，我们还是一步步梳理下。在onCreate方法中仍然是先去获取一些控件的实例，然后尝试从本地缓存中读取天气数据。那么第一次肯定是没有缓存的，因此就会从Intent中取出天气id，
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        /*
        由于这个功能是Android5.0及以上系统才支持的，因此我们在代码中做了一个系统版本号的判断，只有当版本号大于等于21，也就是5.0及以上系统时才会执行后面的代码
        接着我们调用了getWindow().getDecorView()方法拿到当前活动的DecorView，在调用它的setSystemUiVisibility()方法来改变系统的UI的显式，这里传入View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 和 View.SYSTEM_UI_FLAG_LAYOUT_STATE就表示活动的布局会显示在状态栏上面，最后调用一下setStatusBarColor方法将状态栏设置成透明色
        仅仅这些代码就可以实现让背景图和状态栏融合在一起的效果了。不过，如果运行以下程序，你会发现还是有些问题的，天气界面的头布局几乎和系统状态栏紧贴到一起了，这是由于系统状态栏已经成为我们布局的一部分，因此没有单独为他留出空间。当然，这个问题也非常好解决，借助android:fitsSystemWindows属性就可以了。修改activity_weather中的代码
        这里在ScrollView的LinearLayout中增加了android:fitsSystemWindows属性，设置成true就表示会为系统状态栏留出空间




         */
        setContentView(R.layout.activity_weather);










        //初始化各控件
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);


        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

`       String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        /*
        从Intent中取出天气id，并调用requestWeather方法来从服务器请求天气数据。注意，请求数据的时候现将ScrollLayout进行隐藏，否则数据的界面看上去比较奇怪

         */


    }

    /*
    根据天气id请求天气信息
     */
    /*
     requestWeather()方法先是使用了参数中传入的天气id和我们之前申请好的APIKey拼装出一个接口地址，接着调用HttpUtil.sendOkHttpRequest方法来向该地址发出请求，服务器会将相应城市的天气信息以JSON格式返回，然后我们在onResponse回调中先调用Utility.handleWeatherResponse方法将返回的JSON数据转换成Weather对象，再将当前线程切换到主线程，然后进行判断，如果服务器返回的status状态是ok，就说明天气请求成功了，此时将返回的数据缓存到SharedPreferences当中，并调用showWeatherInfo()方法来进行内容显示。
     处理完了WeatherActivity中的逻辑，接下来我们要做的，就是如何从省市县列表跳转到天气界面了，修改ChooseAreaFragment中的代码


     */
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=b43bb0ed9a594067a46cf9fb1c916c35";
        HttpUtil.sendOkHttpRequest(weatherUrl,new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call,IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loadBingPic();
    }
    /*
    加载必应每日一图
     */
    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic,new Callback(){
            @Override
            public void onResponse(Call call,Response response) throws IOException{
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call,IOException e){
                e.printStackTrace();
            }
        });
    }
    /*
    可以看到，首先在onCreate方法中获取了新增控件ImageView的实例，然后尝试从SharePreferences读取缓存的背景图片。如果有缓存的话就直接使用Glide来家在这张照片，如果没有缓存的话就去请求今日的必应背景图
    loadBingPic方法中的逻辑就非常简单了，先是调用HttpUtil.sendOkHttpRequest()方法获取到必应背景图的链接，然后将这个链接缓存到SharedPreferences当中，再将当前线程切换到主线程，最后使用Glide来加载这张图片就可以了，另外需要注意，在requestWeather方法的最后也要挑用一下新背景图片，现在重新云新程序

    不过如果你仔细观察图像，会发现背景图片并没有和状态栏融合在一起，这样的话视觉体验就还是没有达到最佳的效果，虽然说我们已经学习过如果将背景图片和状态栏融合到一起，但当时是借助Design Support库完成的，而我们这个项目中并没有引入Design Support库
    当然如果还是模仿以前的做法，引入Design Support库，然后嵌套CoordinatorLayout，AppBarLayout，CollapsingToolbarLayout等布局，也能实现背景图和状态栏融合到一起的效果，不过这样就过于麻烦了，这里我准备叫你另外一些更加简单的实现方式。修改WeatherActivity中的代码。

     */
  /*
    处理并展示Weather实体类中的数据
     */
    /*
     showWeatherInfo()方法中的逻辑就比较简单了，其实就是从Weather对象中获取数据，然后显示到相应的控件上。注意在未来几天天气预报的部分我们使用了一个for循环来处理每天的天气信息，在循环中动态加载forecast_item布局并设置相应的数据，然后添加到父布局当中。设置完了所有数据后，记得要将ScrollView重新变为可见
     这样我们就将首次进入WeatherActivity是的逻辑全部处理完了，那么当下一次在进入WeatherActivity时，由于缓存已经存在了，因此会直接解析并显示天气数据，
     */
    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }
}


/*
由于所有的天气信息都将在同一个界面上显式，因此activity_weather会是一个很长的布局文件。那么为了让里面的代码不至于混乱不堪，这里我们准备使用之前学过的引入布局技术，即将界面的不同部分写在不同的布局文件里面，再通过引入布局的方式集成到activity_weather中，这样整个布局文件就会显得非常工整
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize">

    <TextView
        android:id="@+id/title_city"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:textColor="#fff"
        android:textSize="20sp"/>
    <TextView
        android:id="@+id/title_update_time"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginRight="10dp"
        android:layout_alignParentRight="true"
        android:layout_centerVertical="true"
        android:textColor="#fff"
        android:textSize="16sp"/>

</RelativeLayout>

这段代码还是比较简单的，头布局中放置了两个TextView，一个居中显式城市名，一个具有现实更新时间
然后新建一个now.xml作为当前天气信息的布局，代码如下所示
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15sp">

    <TextView
        android:id="@+id/degree_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:textColor="#fff"
        android:textSize="60sp"/>

    <TextView
        android:id="@+id/weather_info_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:textColor="#fff"
        android:textSize="20sp"/>

</LinearLayout>

当前天气信息的布局中也是放置了两个TextView，一个用于显示当前气温，一个用于显示天气概况。
然后新建forecast.xml作为未来几天天气信息的布局，代码如下所示：


<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp"
    android:background="#8000">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="15dp"
        android:layout_marginTop="15dp"
        android:text="预报"
        android:textColor="#fff"
        android:textSize="20sp"/>
    <LinearLayout
        android:id="@+id/forecast_layout"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
    </LinearLayout>

</LinearLayout>

这里最外层使用LinearLayout定义了一个半透明的背景，然后使TextView定义了一个标题，接着又使用一个LinearLayout定义了一个用于显式未来几天天气信息的布局，不过这个布局里面没有放入任何的内容，因为这是要根据服务器返回的数据在代码中动态添加的
为此，我们还需要再定义一个未来天气信息的子项布局，创建forecast_item文件

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp">

    <TextView
        android:id="@+id/date_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_weight="2"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/info_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_weight="1"
        android:gravity="center"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/max_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_weight="1"
        android:gravity="right"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/min_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_weight="1"
        android:gravity="right"
        android:textColor="#fff"/>
</LinearLayout>

子项布局中放置了4个TextView，一个用于显示天气预报日期，一个用于显示天气概况，另外两个分别用于显示当天的最高气温和最低气温
然后新建aqi.xml作为空气质量信息的布局，代码如下所示：
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp"
    android:background="#8000">


这个布局中的代码虽然看上去有一些长，但是并不复杂，首先前面都是一样的，使用LinearLayout定义了一个半透明的背景，然后使用TextView定义了一个标题。接下来这里使用LinearLayout和RelativeLayout嵌套的方式实现了一个左右平分并且居中对齐的布局，分别用于显式AQI指数和PM2.5指数。相信你只要仔细看一下，这个布局还是很好理解的

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="15dp"
        android:layout_marginTop="15dp"
        android:text="空气质量"
        android:textColor="#fff"
        android:textSize="20sp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1">

            <LinearLayout
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_centerInParent="true">

                <TextView
                    android:id="@+id/aqi_text"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:textColor="#fff"
                    android:textSize="40sp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:text="AQI指数"
                    android:textColor="#fff"/>

            </LinearLayout>
        </RelativeLayout>

        <RelativeLayout
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1">

            <LinearLayout
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_centerInParent="true">

                <TextView
                    android:id="@+id/pm25_text"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:textColor="#fff"
                    android:textSize="40sp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:text="PM2.5 指数"
                    android:textColor="#fff"/>

            </LinearLayout>

        </RelativeLayout>

    </LinearLayout>

</LinearLayout>

然后新建suggestion.xml作为生活建议信息的布局，代码如下所示：



<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp"
    android:background="#8000">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="15dp"
        android:layout_marginTop="15dp"
        android:text="生活建议"
        android:textColor="#fff"
        android:textSize="20sp"/>

    <TextView
        android:id="@+id/comfort_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="15dp"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/car_wash_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="15dp"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/sport_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="15dp"
        android:textColor="#fff"/>

</LinearLayout>
这里同样是先定义了一个半透明的背景和一个标题，然后下面使用了3个TextView分别用于显示舒适度，洗车指数和运动建议的相关数据
这样我们就把天气界面上的每一个布局文件都写好了，接下来的工作就是将他们引入到activity_weather中


<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorPrimary">

    <ScrollView
        android:id="@+id/weather_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="none"
        android:overScrollMode="never">

        <LinearLayout
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <include layout="@layout/title"/>
            <include layout="@layout/now"/>
            <include layout="@layout/forecast"/>
            <include layout="@layout/aqi"/>
            <include layout="@layout/suggestion"/>

        </LinearLayout>

    </ScrollView>

</FrameLayout>

可以看到，首先最外层布局使用了一个FrameLayout，并将他的背景色设置成colorPrimairy。然后在FrameLayout中嵌套了一个ScrollView，这是因为天气界面中的内容比较多，使用ScrollView可以允许我们通过滚动的方式查看屏幕以外的内容
由于ScrollView的内部只允许存在一个直接子布局，因此在这里又嵌套了一个垂直方向的LinearLayout，然后在LinearLayout中将刚才定义的所有布局逐个引入。
这样我们就将天气界面编写完成了，接下来开始编写业务逻辑，将天气显示在界面上。

将天气显示到界面上
首先需要在Utility类中添加一个用于解析天气JSON数据的方法，如下所示：


 */
