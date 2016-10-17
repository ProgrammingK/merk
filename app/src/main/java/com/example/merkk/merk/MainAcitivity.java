package com.example.merkk.merk;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import bean.TodayWeather;
import util.NetUtil;

/**
 * Created by acer on 2016/9/20.
 */
public class MainAcitivity extends Activity  implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQyalityTv,
                        temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private Handler mHandler = new Handler(  ){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updataTodayWeather( (TodayWeather) msg.obj);
                    break;
                default:
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate( saveInstanceState );
        setContentView( R.layout.weather_info );

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
        city_name_Tv.setText( "N/A" );
        cityTv.setText( "N/A"  );
        timeTv.setText( "N/A" );
        humidityTv.setText( "N/A" );
        pmDataTv.setText( "N/A" );
        pmQyalityTv.setText( "N/A" );
        weekTv.setText( "N/A" );
        climateTv.setText( "N/A" );
        windTv.setText( "N/A" );
    }

    @Override
    public void onClick(View view){
        if (view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences( "config", MODE_PRIVATE );
            String cityCode = sharedPreferences.getString( "main_city_code","101010100" );

            if(NetUtil.getNetwornState( this ) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode( cityCode );
            }else{
                Log.d( "myWeather", "网络挂了" );
                Toast.makeText( MainAcitivity.this, "网络挂了！",Toast.LENGTH_LONG ).show();
            }
        }
    }

    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;

        new Thread( new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
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
                    if(todayWeather != null){
                        Log.d( "myWeather", todayWeather.toString() );
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage( msg );
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        } ).start();
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

    void updataTodayWeather(TodayWeather todayWeather){
        Resources res = getResources();
        city_name_Tv.setText( todayWeather.getCity() + "天气" );
        cityTv.setText( todayWeather.getCity() );
        timeTv.setText( todayWeather.getUpdatetime() );
        humidityTv.setText( "湿度：" + todayWeather.getShidu() );
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
        weekTv.setText( todayWeather.getDate() );
        temperatureTv.setText( todayWeather.getHigh() + "~" + todayWeather.getLow() );
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
        windTv.setText( "风力" + todayWeather.getFengli() );
        Toast.makeText( MainAcitivity.this,"更新成功！", Toast.LENGTH_LONG ).show();
    }

}
