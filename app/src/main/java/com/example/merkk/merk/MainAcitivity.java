package com.example.merkk.merk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.MyApplication;
import bean.TodayWeather;
import location.MyLocationListener;
import util.NetUtil;

/**
 * Created by acer on 2016/9/20.
 */
public class MainAcitivity extends Activity  implements View.OnClickListener,ViewPager.OnPageChangeListener{

    public LocationClient mLocationClient;
    public BDLocationListener myListener = new MyLocationListener();
    public LocationClientOption option = new LocationClientOption();

    private static final int UPDATE_TODAY_WEATHER = 1,UPDATE_TODAY_FAIL = 2,SIX_DAY_WEATHER = 3;
    private static final int SDK_PERMISSION_REQUEST = 100;
    private ImageView mUpdateBtn, mCitySelect, mCityLocation;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQyalityTv,
                        temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private MyApplication myApplication = (MyApplication.getInstance());

    private AnimationDrawable _animationDrawable;

    //ViewPage一周天气
    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;

    private static final int[] pics = {R.layout.weatherpage1,R.layout.weatherpage1};
    private ImageView[] dots;
    private int currentIndex;

    private Handler mHandler = new Handler(  ){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updataTodayWeather( (TodayWeather) msg.obj);
                    break;
                case UPDATE_TODAY_FAIL:
                    if(!mUpdateBtn.isClickable()){
                        mUpdateBtn.setClickable( true );
                        _animationDrawable.stop();
                        mUpdateBtn.setImageResource( R.drawable.title_update );
                    }
                    break;
                case SIX_DAY_WEATHER:
                    updateSixDayWeaher((ArrayList<TodayWeather>) msg.obj);
                default:
                    break;

            }
        }
    };
    private String permissionInfo;


    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限

            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate( saveInstanceState );
        setContentView( R.layout.weather_info );

        mLocationClient = new LocationClient( getApplicationContext() );
        mLocationClient.registerLocationListener( myListener );
        initLocation();

        mUpdateBtn = (ImageView) findViewById( R.id.title_update_btn );
        mUpdateBtn.setOnClickListener( this );

        if(NetUtil.getNetwornState( this ) != NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络OK");
            Toast.makeText( MainAcitivity.this,"网络OK!",Toast.LENGTH_LONG ).show();
        }else{
            Log.d( "myWeather", "网络挂了" );
            Toast.makeText( MainAcitivity.this, "网络挂了！",Toast.LENGTH_LONG ).show();
        }
        initView();
    }

    private void initLocation() {

        option.setLocationMode( LocationClientOption.LocationMode.Hight_Accuracy );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setAddrType( "all" );
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        //option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        //option.setEnableSimulateGps(true);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        Log.d("baidu location","localClient setted");
    }

    void initView(){
        city_name_Tv = (TextView) findViewById( R.id.title_city_name );
        cityTv = (TextView) findViewById( R.id.city );
        timeTv = (TextView) findViewById( R.id.time );
        humidityTv = (TextView) findViewById( R.id.humidity );
        weekTv = (TextView) findViewById( R.id.week_today );
        pmDataTv = (TextView) findViewById( R.id.pm_data );
        pmQyalityTv = (TextView) findViewById( R.id.pm2_5_quality );
        pmImg = (ImageView) findViewById( R.id.pm2_5_img );
        temperatureTv = (TextView) findViewById( R.id.temperature );
        climateTv = (TextView) findViewById( R.id.climate );
        windTv = (TextView) findViewById( R.id.wind );
        weatherImg = (ImageView) findViewById( R.id.weather_img );
        mCitySelect = (ImageView) findViewById( R.id.title_city_manager );
        mCityLocation = (ImageView) findViewById( R.id.title_location );

        city_name_Tv.setText( "N/A" );
        cityTv.setText( "N/A"  );
        timeTv.setText( "N/A" );
        humidityTv.setText( "N/A" );
        pmDataTv.setText( "N/A" );
        pmQyalityTv.setText( "N/A" );
        weekTv.setText( "N/A" );
        climateTv.setText( "N/A" );
        windTv.setText( "N/A" );
        temperatureTv.setText( "N/A" );
        mCitySelect.setOnClickListener( this );
        mCityLocation.setOnClickListener( this );

        //ViewPage
        LayoutInflater inflater = LayoutInflater.from( this );
        views = new ArrayList<View>();
        for(int i = 0; i< pics.length; i++){
            views.add( inflater.inflate( pics[i],null ) );
        }
        vpAdapter = new ViewPagerAdapter( views,this );
        vp = (ViewPager) findViewById( R.id.six_day_info );
        vp.setAdapter( vpAdapter );
        initDots();
        vp.setOnPageChangeListener( this );

    }
    private void initDots() {
        LinearLayout l1 = (LinearLayout) findViewById( R.id.weather_info_page);
        l1.setOnClickListener( this );

        dots = new ImageView[pics.length];
        for(int i = 0; i < pics.length; i++){
            dots[i] = (ImageView) l1.getChildAt( i );
            dots[i].setEnabled( false );
            dots[i].setTag( i );
        }
        currentIndex = 0;
        dots[currentIndex].setEnabled( true );
    }

    @Override
    public void onClick(View view){
        if (view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences( "config", MODE_PRIVATE );
            String cityCode = sharedPreferences.getString( "main_city_code","101010100" );
            Log.d( "citycode",cityCode );
            //设置动画背景
            if(NetUtil.getNetwornState( this ) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                //刷新图标开始旋转，取消按钮
                mUpdateBtn.setImageResource( R.drawable.refresh );
                _animationDrawable = (AnimationDrawable) mUpdateBtn.getDrawable();
                _animationDrawable.setOneShot( false );
                _animationDrawable.start();
                view.setClickable( false );
                queryWeatherCode( cityCode);
            }else{
                Log.d( "myWeather", "网络挂了" );
                Toast.makeText( MainAcitivity.this, "网络挂了！",Toast.LENGTH_LONG ).show();
            }
        }
        if(view.getId() == R.id.title_city_manager){
            Intent i = new Intent( this, SelectCity.class );
            startActivityForResult( i,1 );
        }
        if(view.getId() == R.id.title_location){
            new Thread( new Runnable() {
                @Override
                public void run() {
                    getPersimmions();
                    mLocationClient.requestLocation();
                    String cityname = mLocationClient.getLastKnownLocation().getCity();
                    String citycode =null;
                    List<HashMap<String,String>> data =myApplication.getCityData();
                    for(HashMap<String,String> item : data){
                        if(item.get( "cityname" ).equals( cityname ))
                            citycode = item.get( "citycode" );
                    }
                    if(!citycode.equals( null )){
                        queryWeatherCode( citycode );
                    }else{
                        Toast.makeText( MainAcitivity.this, "location failed", Toast.LENGTH_SHORT ).show();
                    }
                    Log.d( "MainActivity","num = ++++++++" + mLocationClient );
                }
            } );

            //lc.stop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra( "cityCode" );
            Log.d( "merkk","选择城市的代码" + newCityCode );

            if(NetUtil.getNetwornState( this ) != NetUtil.NETWORN_NONE){
                Log.d( "merkk", "Internet OK" );
                queryWeatherCode( newCityCode );
            }else{
                Log.d( "merkk","Internet down" );
                Toast.makeText( MainAcitivity.this, "Internet down", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode ){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Thread thread1 = new Thread( new Runnable() {
            @Override
            public void run() {

                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                List<TodayWeather> sixDays = null;
                try{
                    URL url = new URL( address );
                    con =(HttpURLConnection) url.openConnection();
                    con.setRequestMethod( "GET" );
                    con.setConnectTimeout( 8000 );
                    con.setReadTimeout( 8000 );
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
                    StringBuilder response = new StringBuilder(  );
                    String str;
                    while((str = reader.readLine()) != null){
                        response.append( str );
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d( "myWeather",responseStr );
                    todayWeather = parseXML(responseStr);
                    sixDays = parseSixDayXML( responseStr );
                    if(todayWeather != null && sixDays != null){
                        Log.d( "myWeather", todayWeather.toString() );
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage( msg );
                        msg = new Message();
                        msg.what = SIX_DAY_WEATHER;
                        msg.obj = sixDays;
                        mHandler.sendMessage( msg );
                    }

                }catch(Exception e){
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = UPDATE_TODAY_FAIL;
                    msg.obj = todayWeather;
                    mHandler.sendMessage( msg );
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }



            }
        } );
        thread1.start();

    }

    private TodayWeather parseXML(String xmlData) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dataCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try{
            XmlPullParserFactory fac =XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(  xmlData ));
            int eventType = xmlPullParser.getEventType();
            Log.d( "myWeather","parser" );
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals( "resp" )){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            if(xmlPullParser.getName().equals( "city" )){
                                xmlPullParser.next();
                                todayWeather.setCity( xmlPullParser.getText()  );
                            }else if(xmlPullParser.getName().equals( "updatetime" )){
                                xmlPullParser.next();
                                todayWeather.setUpdatetime( xmlPullParser.getText() );
                            }else if(xmlPullParser.getName().equals( "shidu" )){
                                xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText() );
                            }else if(xmlPullParser.getName().equals( "wendu" )){
                                xmlPullParser.next();
                                todayWeather.setWendu( xmlPullParser.getText() );
                            }else if(xmlPullParser.getName().equals( "pm25" )){
                                xmlPullParser.next();
                                todayWeather.setPm25( xmlPullParser.getText() );
                            }else if(xmlPullParser.getName().equals( "quality" )){
                                xmlPullParser.next();
                                todayWeather.setQuality( xmlPullParser.getText() );
                            }else if(xmlPullParser.getName().equals( "fengxiang" ) && fengxiangCount == 0){
                                xmlPullParser.next();
                                todayWeather.setFengxiang( xmlPullParser.getText() );
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals( "fengli" ) && fengliCount == 0){
                                xmlPullParser.next();
                                todayWeather.setFengli( xmlPullParser.getText() );
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals( "date" ) && dataCount == 0){
                                xmlPullParser.next();
                                todayWeather.setDate( xmlPullParser.getText() );
                                dataCount++;
                            }else if(xmlPullParser.getName().equals( "high" ) && highCount == 0){
                                xmlPullParser.next();
                                todayWeather.setHigh( xmlPullParser.getText().substring( 2 ).trim() );
                                highCount++;
                            }else if(xmlPullParser.getName().equals( "low" ) && lowCount == 0){
                                xmlPullParser.next();
                                todayWeather.setLow( xmlPullParser.getText().substring( 2 ).trim() );
                                lowCount++;
                            }else if(xmlPullParser.getName().equals( "type" ) && typeCount == 0){
                                xmlPullParser.next();
                                todayWeather.setType( xmlPullParser.getText() );
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;

                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    private List<TodayWeather> parseSixDayXML(String xmlData) {
        List<TodayWeather> sixDays = new ArrayList<TodayWeather>(  );

        boolean typeCount = true;//获取白天的天气类型

        TodayWeather weather = null;
        try{
            XmlPullParserFactory fac =XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(  xmlData ));
            int eventType = xmlPullParser.getEventType();
            Log.d( "myWeather","parser" );
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals( "yesterday" ) || xmlPullParser.getName().equals( "weather" )){
                            weather = new TodayWeather();
                        }
                        if(weather != null){
                            if(xmlPullParser.getName().equals( "date_1" ) || xmlPullParser.getName().equals( "date" )){
                                xmlPullParser.next();
                                weather.setDate( xmlPullParser.getText()  );
                            }else if(xmlPullParser.getName().equals( "high_1" ) || xmlPullParser.getName().equals( "high" )){
                                xmlPullParser.next();
                                weather.setHigh( xmlPullParser.getText().substring( 2 ).trim());
                            }else if(xmlPullParser.getName().equals( "low_1" ) || xmlPullParser.getName().equals( "low" )){
                                xmlPullParser.next();
                                weather.setLow( xmlPullParser.getText().substring( 2 ).trim() );
                            }else if(xmlPullParser.getName().equals( "type_1" ) || xmlPullParser.getName().equals( "type" )){
                                if(typeCount){

                                    xmlPullParser.next();
                                    weather.setType( xmlPullParser.getText() );
                                    typeCount = false;

                                }else
                                    typeCount = true;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(xmlPullParser.getName().equals( "yesterday" ) || xmlPullParser.getName().equals( "weather" )){
                            sixDays.add( weather );
                        }
                        break;

                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return sixDays;
    }

    void updataTodayWeather(TodayWeather todayWeather){
        Resources res = getResources();

        city_name_Tv.setText( todayWeather.getCity() + "天气" );
        cityTv.setText( todayWeather.getCity() );
        timeTv.setText( todayWeather.getUpdatetime() );
        humidityTv.setText( "湿度：" + todayWeather.getShidu() );
        if(todayWeather.getPm25() != null){
            pmDataTv.setText( todayWeather.getPm25() );
            if(Integer.valueOf( todayWeather.getPm25() ) < 50){
                pmImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_0_50 ) );
            }else if(Integer.valueOf( todayWeather.getPm25() ) < 100){
                pmImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_51_100 ) );
            }else if(Integer.valueOf( todayWeather.getPm25() ) < 150){
                pmImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_101_150 ) );
            }else if(Integer.valueOf( todayWeather.getPm25() ) < 200){
                pmImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_151_200 ) );
            }else if(Integer.valueOf( todayWeather.getPm25() ) < 300){
                pmImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_201_300 ) );
            }else{
                pmImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_greater_300 ) );
            }
            pmQyalityTv.setText( todayWeather.getQuality() );
        }else{
            pmDataTv.setText( "not found" );
            pmQyalityTv.setText("not found" );
        }
        if(todayWeather.getDate() != null){
            weekTv.setText( todayWeather.getDate() );
        }else{
            weekTv.setText( "not found" );
        }
        if(todayWeather.getHigh() != null && todayWeather.getLow()!= null){
            temperatureTv.setText( todayWeather.getHigh() + "~" + todayWeather.getLow() );
        }else{
            temperatureTv.setText( "not found" );
        }
        if(todayWeather.getType() != null){
            refreshWeatherType( climateTv,weatherImg,todayWeather );
        }else{
            climateTv.setText( "not found" );
        }
        if(todayWeather.getFengli() != null){
            windTv.setText( "风力" + todayWeather.getFengli() );
        }else{
            windTv.setText( "not found" );
        }

        if(!mUpdateBtn.isClickable()){
            mUpdateBtn.setClickable( true );
            _animationDrawable.stop();
            mUpdateBtn.setImageResource( R.drawable.title_update );
        }

        Toast.makeText( MainAcitivity.this,"更新成功！", Toast.LENGTH_LONG ).show();
    }

    private void updateSixDayWeaher(ArrayList<TodayWeather> wList) {


        TextView date,temperature,climate;
        ImageView type;
        TodayWeather weather = null;
        int[] ids = {R.id.p1,R.id.p2,R.id.p3};
        for(int i = 0; i < wList.size();i++ ){
            date = (TextView) views.get( i / 3 ).findViewById( ids[i % 3] ).findViewById( R.id.date );
            temperature = (TextView) views.get( i / 3 ).findViewById( ids[i % 3] ).findViewById( R.id.day_temp );
            climate = (TextView) views.get( i / 3 ).findViewById( ids[i % 3] ).findViewById( R.id.day_weather );
            type = (ImageView) views.get( i / 3 ).findViewById(ids[i % 3] ).findViewById( R.id.day_weather_img );
            weather = wList.get( i );
            Log.d( "sixDay", weather.toString());
            if(weather.getDate() != null){
                date.setText( weather.getDate() );
            }else{
                date.setText( "not found" );
            }
            if(weather.getHigh() != null && weather.getLow()!= null){
                temperature.setText( weather.getHigh() + "~" + weather.getLow() );
            }else{
                temperature.setText( "not found" );
            }

            if(weather.getType() != null){
                refreshWeatherType( climate,type,weather );
            }else{
                climateTv.setText( "not found" );
            }
        }
        Toast.makeText( MainAcitivity.this,"更新成功！", Toast.LENGTH_LONG ).show();
    }
    private void refreshWeatherType(TextView climateTv, ImageView weatherImg,TodayWeather todayWeather){
        Resources res = getResources();
        climateTv.setText( todayWeather.getType() );
        if(todayWeather.getType().equals( "暴雪" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_baoxue ) );
        }else if(todayWeather.getType().equals( "暴雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_baoyu ) );
        }else if(todayWeather.getType().equals( "大暴雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_dabaoyu ) );
        }else if(todayWeather.getType().equals( "大雪" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_daxue ) );
        }else if(todayWeather.getType().equals( "大雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_dayu ) );
        }else if(todayWeather.getType().equals( "多云" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_duoyun ) );
        }else if(todayWeather.getType().equals( "雷阵雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_leizhenyu ) );
        }else if(todayWeather.getType().equals( "雷阵雨冰雹" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_leizhenyubingbao ) );
        }else if(todayWeather.getType().equals( "晴" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_qing ) );
        }else if(todayWeather.getType().equals( "沙尘暴" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_shachenbao ) );
        }else if(todayWeather.getType().equals( "特大暴雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_tedabaoyu ) );
        }else if(todayWeather.getType().equals( "雾" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_wu ) );
        }else if(todayWeather.getType().equals( "小雪" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_xiaoxue ) );
        }else if(todayWeather.getType().equals( "小雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_xiaoyu ) );
        }else if(todayWeather.getType().equals( "阴" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_yin ) );
        }else if(todayWeather.getType().equals( "雨夹雪" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_yujiaxue ) );
        }else if(todayWeather.getType().equals( "阵雪" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_zhenxue ) );
        }else if(todayWeather.getType().equals( "阵雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_zhenyu ) );
        }else if(todayWeather.getType().equals( "中雨" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_zhongyu ) );
        }else if(todayWeather.getType().equals( "中雪" )){
            weatherImg.setImageDrawable( res.getDrawable( R.drawable.biz_plugin_weather_zhongxue ) );
        }

    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if(position < 0 || position >= pics.length  || currentIndex == position){
            return;
        }
        dots[position].setEnabled( true );
        dots[currentIndex] .setEnabled( false );
        currentIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public LocationClient getLocationClient(){
        return mLocationClient;
    }
}
