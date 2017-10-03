package com.example.cmx.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
}
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


 */