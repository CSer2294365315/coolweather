package com.example.cmx.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
/*
可以看到，这里在onCreate方法的一开始先从SharedPreferences文件中读取缓存数据，如果不为null就说明之前已经请求过天气数据了，那么就没有必要让用户再次选择城市，而是直接跳转到WeatherActivity即可
好了，现在重新运行一下程序，然后我们还可以向下滑动来查看更多天气信息

获取必应每日一图
虽然说现在我们已经把天气界面编写的非常不错了，不过和市场上的一些天气软件的界面相比，仍然还是有一定的差距，出色的天气软件不会像我们现在这样使用一个固定的背景色，而是会根据不同的城市或者天气情况展示不同的背景图片
当然实现这个功能并不复杂，最重要的是需要有服务器的接口支持。不过我实在是没有经历去准备这样一套晚上的服务器接口，那么为了不让我们的天气界面过于单调，这里我们准备使用一个巧妙的方法
为此我专门准备了一个获取必应每日一图的接口:http://guolin.tech/api/bing_pic.
访问这个接口，服务器会返回今日的必应背景图链接
然后我们使用Glide去加载这张图片就可以了
下面开始实践

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"/>
这里我们在FrameLayout中添加了一个ImageView，并且将他的宽和高都设置成match_parent。由于FrameLayout默认情况下会将控件放置在左上角，因此ScrollView会完全覆盖住ImageView，从而ImageView也就成为背景图片了
接着修改WeatherActivity中的代码
 */
/*
我们又怎样才能查看到具体的天气信息呢？这就必须要用到每个地区对应的天气Id了，观察上面返回的数据，你会发现每个县或者区都会有一个weather_id，拿着这个id再去访问和风天气的接口，就能获取到该地区的天气信息了
下面我们来看一下和风天气该如何使用，首先你需要注册一个自己的账号
有了API key ，再配合刚才的weather_id，我们就能获取到任何城市的天气信息了。比如说苏州的weather_id是CN101190401，那么访问如下接口即可查看苏州的天气信息
http://guolin.tech/api/weather?cityid=CN101190401&key=b43bb0ed9a594067a46cf9fb1c916c35
其中，cityid部分填入的就是待查看城市的weather_id，key部分填入的就是我们申请到的API key。这样，服务器就会把苏州详细的天气信息以JSON的形式返回给我们了
{
    "HeWeather": [
        {
            "aqi": {},
            "basic": {},
            "daily_forecast": [],
            "hourly_forecast": [],
            "now": {},
            "status": "ok",
            "suggestion": {}
        }
    ]
}

返回数据的格式大体上就是这个样子了，其中status代表请求的状态，ok表示成功，basic中会包含城市的一些基本信息，api中会包含当前空气质量的情况，now中会保护按当前的天气信息，suggestion中会包含一些天及建议相关的生活建议，daily_forecast中会包含为起来几天的天气的信息

数据都能获取到了之后，接下来就是JSON解析的工作了。确定的技术完全可行之后，接下来就可以开始编码了，不过别着急，我们准备让CoolWheator，成为一个开源软件，并使用GitHub来进行代码托管，首先我们进入到本书自后一次的Git时间

将库欧天气的代码托管到GitHub上面
GitHub是全球最大的代码托管网站，主要是借助Git来进行版本控制的，任务开源软件都可以免费的将代码提交到GitHub上，

接下来就可以点击Start a project来创建一个版本库了，这里将版本库命名为coolweather
选择Android项目类型的.gitignore使用Apache License 2.0 作为开源协议
创建完版本库后，需要创建库欧天气这个项目。

在Android创建完项目后，需要将远程版本库克隆到本地，首先必须知道远程版本的Git地址，点击Clone or download 按钮就可以看到了
点击右边的复制按钮就可以将版本库的Git地址复制到剪贴板

cd 到项目文件目录
输入git clone https://github.com/CSer2294365315/coolweather.git
现在我们需要将这个目录中的所有文件全部复制粘贴到上一层目录，这样就能将整个CoolWeather工程目录添加到版本控制中去了。注意.git是一个隐藏目录，在复制的时候千万不能漏掉。另外，上一层目录中也有.gitignore文件，我们直接将其覆盖即可。复制完后可以吧coolweather目录删除掉，



Command+Shift+. 可以显示隐藏文件、文件夹，再按一次，恢复隐藏；
finder下使用Command+Shift+G 可以前往任何文件夹，包括隐藏文件夹。

接下来我们把CoolWeather中现有的文件提交到GitHub上面
git add .
然后在本地执行提交操作
git commit -m "First commit"
最后将提交的内容同步到远程版本库，也就是GitHub上面
git push origin master
注意，在最后一步的时候GitHub要求输入用户名和密码来进行身份校验，这个时候我们输入注册GitHub时填入的用户名和密码就可以了
这样同步就完成了，现在刷新一下库欧天气版本库的主页，你会看到刚才提交的哪些文件已经存在了
*/
/*

创建数据库和表

从本节开始，我们就要真正的动手编码了，为了要让项目能够有更好的结构，这里需要在com.coolweather.android包中在新建几个包

其中db包用于存放数据库模型相关的代码，gson包用于存放GSON模型相关的代码，service包用于存放服务相关的代码，util包用于存放工具相关的代码

第一阶段我们要做的就是创建好数据库和表，这样从服务器获取到的数据才能够存储到本地。关于数据库和标的创建方式，我们之前已经学过
为了简化数据库的操作，这里我们准备使用LitePal来管理库欧天气的数据库
首先需要将项目所需的各种依赖库进行声明，编辑app/build.gradle

  compile 'org.litepal.android:core:1.4.1'
    compile 'com.suqareup.okhttp3:okhttp:3.4.1'
    compile 'com.google.code.gson:gson:2.7'
    compile 'com.github.bumptech.glide:glide:3.7.0'

这里声明的4个库我们之前都是使用过的，LitePal用于对数据库进行操作，OkHttp用于进行网络请求，GSON用于解析JSON数据，Glide用于加载和展示图片。库欧天气将会对这几个库进行综合利用，这里直接一次性将他们都添加进来
然后我们来设计一下数据库表结构，表的设计不绝对。这里我们准备建立3张表：province，city，county，分别用于存放省，市，县的数据信息。对应到实体类中的话，就应该建立Province，City，County这3各类
那么，在db包下新建一个Province类，代码如下所示：

其中，id是每个实体类都应该有的字段，provinceName记录省的名字，provinceCode记录省的代号。另外，LitePal中的每一个实体类都是必须要继承自DataSupport类的
接着在db包下新建一个City类
其中，cityName记录市的名字，cityCode记录市的代号，provinceId记录当前市所属省的id值

然后在db包下新建一个County类
    private int id;

    private String countyName;

    private String weatherId;

    private int cityId;
其中，countryName记录县的名字，weatherId记录县所对应的天气id，cityId记录当前县所属市的id
可以看到，实体类的内容都非常简单，就是声明了一些需要的字段，并生成相应的getter和setter方法就可以了
接下来需要配置litepal.xml文件

<litepal>
    <dbname value="cool_weather"/>

    <version value="1"/>

    <list>
        <mapping class="com.example.cmx.coolweather.db.Province"/>
        <mapping class="com.example.cmx.coolweather.db.County"/>
        <mapping class="com.example.cmx.coolweather.db.City"/>
    </list>
</litepal>

这里我们将数据库名指定为cool_weather,数据库版本指定为1，并将Province，City和County这3个实体类添加到映射表中
最后还需要配置一下LitePalApplication，修改AndroidManifest中的代码


android:name="org.litepal.LitePalApplication"

这样我们就将所有的配置都完成了，数据库和表会在首次执行任意数据库操作的时候自动创建
好了，第一阶段的代码写到这里就差不多了，我们现在来提交一下，首先将所有新增的文件添加到版本控制中
git add .
接着执行提交操作
git commit -m "加入创建数据库和表的各项配置"
最后将提交同步到GitHub上面
git push origin master
OK，第一阶段完工，下面让我们赶快进入第二阶段的开发工作吧

遍历全国省市县数据
在第二阶段中，我们准备把遍历全国省市县的功能加入，这一阶段需要编写的代码量比较大
我们已经知道，全国所有省市县的数据都是从服务端获取到的，因此这里和服务器的交互式是必不可少的，所以我们可以在util包下现增加一个HttpUtil类，代码如下所示：
public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
由于OkHttp的出色封装，这里和服务器进行交互的代码非常简单，仅仅3行就完成了，现在我们发起一条Http请求只需要调用sendOkHttpRequest()方法，传入请求地址，并注册一个回调来处理服务器响应就可以了
另外，由于服务器返回的省市县数据都是JSON格式的，所以我们最好在提供一个工具来处理这种数据，在util包下新建一个Utility类，代码如下所示

 */
/*
可以看到，这里在ScrollView的外面又嵌套了一层SwipeRefreshLayout，这样就自动拥有下拉刷新的功能了
然后修改WeatherActivity中的代码，加入更新天气的处理逻辑，如下所示：

 */