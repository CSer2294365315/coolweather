package com.example.cmx.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmx.coolweather.db.City;
import com.example.cmx.coolweather.db.County;
import com.example.cmx.coolweather.db.Province;
import com.example.cmx.coolweather.util.HttpUtil;
import com.example.cmx.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by cmx on 2017/10/3.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;

    public static final int LEVEL_CITY=1;

    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList=new ArrayList<>();

    /*
    省列表
     */
    private List<Province> provinceList;

    /*
    市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button) view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }
    /*
    在onCreateView方法中先是获取到了一些控件的实例，然后去初始化了ArrayAdapter,并将它设置为ListView的适配器
    接着在onActivityCreated方法中给ListView和Button设置了点击事件，到这里我们的初始化工作就算是完成了
     */

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view ,int position,long id){
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
            }
        });
        /*
        当你点击了某个省的时候会进入到ListView的onItemClick()方法，这个时候会根据当前的级别来判断是去调用queryCities方法还是queryCounties方法，queryCities方法是去查询市级数据，queryConuties是去查询县级数据，这俩ing个方法内部的流程和queryProvinces方法基本相同，在这里就不重复讲解了
         */
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }

            }
        });
        /*
        如果当前是县级列表，那就返回到市级列表，如果是市级列表，就返回到省级列表。当返回到省级列表的时候，返回按钮会自动隐藏，而且也就不需要再做进一步的处理了。
        这样我们就把遍历全国省市县的功能完成了，可是碎片是不能直接显示在界面上的，因此我们还需要把它添加到活动里面才行。修改activity_main中的代码
        <FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <fragment
        android:id="@+id/choose_area_fragment"
        android:name="com.example.cmx.coolweather.ChooseAreaFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</FrameLayout>

布局文件很简单，只是定义了一个FrameLayout，然后将ChooseAreaFragment添加进来，并让他充满整个布局
另外，我们刚才在碎片的布局里面已经自定义了一个标题栏，因此就不再需要原生的ActionBar了，修改res/values/styles.xml中的代码
<resources>

    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

</resources>
现在第二阶段的开发工作也完成的差不多了，我们可以运行一下来看看效果。不过在运行之前还有一件事情没有作，那就是声明程序需要的权限。修改AndroidManifest中的代码
由于我们是通过网络接口来获取全国省市县数据的，因此必须要添加访问网络的权限才可以
现在可以运行以下程序了，结果如图所示
可以看到，全国所有省级的数据都显示出来了，我们还可以继续查看市级数据，比如点击江苏省，结果如图所示：
这个时候标题栏上会出现一个返回按钮，好了，第二阶段的开发工作也都完成了，我们仍然要把代码提交一下
接下来我们完成第三阶段的开发工作

显示天气信息

在第三阶段中，我们就要开始去查询天气，并且把天气信息显示出来了。由于和风天气返回的JSON数据结构非常复杂，如果还是用JSONObject来解析就会很麻烦，这里我们就准备借助GSON来对天气信息进行解析了

定义GSON实体类

GSON的用法很简单,解析数据只需要一行代码就完成了，但前提是需要先将数据对应的实体类创建好。由于和丰田其返回的数据内容非常多，这里我们不可能将所有的内容都利用起来，因此我们筛选了一些比较重要的数据来进行解析。
首先我们回顾一下返回数据的大致格式：
{
    "HeWeather":[
        {
            "status":"ok"
            "basic":={},
            "aqi":={},
            "now":={},
            suggestion={},
            daily_forecast:=[]
        }
      ]
}

其中，basic，aqi，now，suggestion和daily_forecast的内部又都会有具体的内容，那么我们就可以将这五个部分定义成武个实体类
下面来一个个的看：
"basic":={
    "city":="苏州"，
    "id":"CN101190401"
    "update"={
        "loc":"2016-08-08 21:58"
     }
  }
其中，city表示城市名，id表示城市对应的天气id，update表示天气的更新时间
我们按照此结构就可以在gson包下建立一个Basic类，代码如下所示：

可以看到，daily_forecast包含的是一个数组，数组中的每一项都代表着未来的天气信息，针对这种情况，我们只需要定义出单日天气的实体类就可以了，然后在声明实体类引用的时候使用集合类型来进行声明

这样我们就把basic，aqi，now，suggestion和daily_forecast对应的实体类全部都创建好了，接下来我们还需要在创建一个总的实例类来引用刚刚创建的各个实体类。在gson包下新建一个Weather类，代码如下所示：



         */
        queryProvinces();
    }
    /**
     *查询全国所有的省，优先从数据库中查询，如果没有就在到服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /*
    queryProvinces方法中首先会将头布局的标题设置成中国，将返回按钮隐藏起来，因为省级列表已经不能再返回了。然后调用LitePal的查询接口来从数据库中读取省级数据，如果读取到了就直接将数据显示到界面上，如果没有读取到就按照接口组装出一个请求地址，然后调用queryFromServer方法来从服务器上查询数据

     */
    /**
     *查询选中省内的所有市，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到就从服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    /*
    queryFromServer()方法会调用HttpUtil的sendOkHttpRequests方法来向服务器发送请求，响应的数据会回调到onResponse() 方法中，然后我们在这里去调用Utility的handleProvincesResponse方法解析和处理服务器返回的数据，并存储到数据库中。接下来的一部很关键，在解析和处理完数据之后，我们在此调用了queryProvinces方法来重新加载省级数据，由于queryProvinces方法牵扯到了UI操作，因此必须要在主线程上调用，这里借助了runOnUiThread方法来实现从子线程切换到主线程。现在数据库中已经存在了数据，因此调用queryProvinces就会直接将数据显示到界面上了。
    当你点击了某个省的时候会进入到ListView的OnItemClick方法
    另外还有一点需要注意，再返回按钮的点击事件里，会对当前ListView的列表进行判断，如果当前是县级列表，饭么就返回
     */
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call,Response response)throws IOException{
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call ,IOException e){
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /**
     * 显式进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

}
/*
这里的代码非常多，但是逻辑却不复杂，我们来慢慢数以理一下。在onCreateView()方法中
 */