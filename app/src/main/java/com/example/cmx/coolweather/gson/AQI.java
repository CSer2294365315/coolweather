package com.example.cmx.coolweather.gson;

/**
 * Created by cmx on 2017/10/4.
 */

public class AQI {
    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}

