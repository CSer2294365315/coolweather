package com.example.cmx.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cmx on 2017/10/4.
 */

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
/*
由于JSON中的一些字段可能不太适合直接作为Java字段来命名，因此这里使用了@SerializedName注解的方式来让JSON字段和Java字段之间建立映射关系
这样我们就将Basic类定义好了，还是挺容易理解的吧，其余的几个实体类也是类似的，我们使用同样的方式来定义就可以了。比如api中的具体内容如下所示：

 */