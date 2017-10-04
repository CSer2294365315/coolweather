package com.example.cmx.coolweather.util;

import android.text.TextUtils;

import com.example.cmx.coolweather.db.City;
import com.example.cmx.coolweather.db.County;
import com.example.cmx.coolweather.db.Province;
import com.example.cmx.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cmx on 2017/10/3.
 */

public class Utility {
    /*
    解析和处理服务器返回的省级数据
     */
    public static boolean   handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    /*
    解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();++i){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();

                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    /*
    解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();++i){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJSON(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /*
    可以看到，handleWeatherResponse方法先是通过JSONObject和JSONArray将天气主体中的数据解析出来，即如下内容：
    {
        "status":"ok",
        "basic":{},
        "aqi":{},
        "now":{},
        "suggest":{},
        "daily_forecast":[]
     }


     public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}

public class Basic  {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
     然后由于我们之前已经按照上面的数据格式定义过相应的GSON实体类，因此只需要通过调用fromJSON方法就能直接将JSON数据转换成Weather对象了


接下来的工作是我们如何在活动中去请求天气数据，以及将数据展示到界面上。修改WeatherActivity中的代码



     */

}
/*
可以看到，我们提供了handleProvinceResponse()，handleCitiesResponse()，handleCountiesResponse()这3个方法，分别用于解析和处理服务器返回的省级，市级，县级数据，处理的方式都是类似的，先使用JSONArray和JSONObject将数据解析出来，然后组装成实体类对象，在调用save方法将数据保存到数据库中，由于这里的JSON数据结构比较简单，所以我们就不使用GSON进行解析了
需要准备的工具类就这么多，现在可以开始写界面了。由于便利全国省市县的功能我们在后面还会复用，因此就不写在活动里面了，而是写在碎片里面，这样需要复用的时候直接在布局里面应用碎片就可以了
在res/layout目录里新建choose_area布局
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#fff">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary">
        <TextView
            android:id="@+id/title_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#fff"
            android:textSize="20sp"/>
        <Button
            android:id="@+id/back_button"
            android:layout_width="25dp"
            android:layout_height="25dp"
            android:layout_marginLeft="10dp"
            android:layout_alignParentLeft="true"
            android:layout_centerVertical="true"
            android:background="@drawable/ic_back"/>
    </RelativeLayout>
    <ListView
        android:id="@+id/list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </ListView>
</LinearLayout>

布局文件中的内容并不复杂，我们先是定义了一个头布局来作为标题栏，将布局高度设置为actionBar的高度，背景色设置为colorPrimary，然后在头布局中放置一个TextView用于显示标题的内容，放置了一个Button用于执行返回操作，注意我已经提前准备好了一张ic_back图片作为按钮的背景图。之所以要使用自己定义的标题栏，是因为碎片中最好不要直接使用ActionBar或者Toolbar。不然在复用的时候可能会出现一些你不想看到的效果
接下来在头布局的下面定义了一个TextView，省市县的数据就将显示在这里，之所以这次使用了ListView，是因为它会自动给每个字线之间添加一条分割线，而如果使用RecyclerView想实现同样的功能则比较麻烦，在这里我们总是选择最优的实现方案
接下来也是最关键的一步，我么需要编写用于遍历省市县数据的碎片了。新建ChooseAreaFragment继承自Fragment

 */
