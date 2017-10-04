package com.example.cmx.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cmx on 2017/10/4.
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
/*
在Weather类中，我们对Basic，AQI，Now，Suggestion和Forecast类进行了引用。其中，由于daily_forecast中包含的是一个数组，因此这里使用了List集合来引用Forecast类，另外，返回的天气数据还会包含一项status数据，成功返回ok，失败则会返回具体的原因，那么这里也需要添加一个对应的status字段。
现在所有的GSON实体类都定义好了，接下来我们开始编写天气界面

 */
/*
编写天气界面

首先创建一个用于显示天气信息的活动。
 */