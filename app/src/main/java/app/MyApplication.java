package app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bean.City;
import db.CityDB;
import location.MyLocationListener;


/**
 * Created by acer on 2016/10/27.
 */

public class MyApplication extends Application {
    private static  final  String TAG = "MyApp";

    private static MyApplication mApplication;

    private CityDB mCityDB;
    private List<City> mCityList;

    private List<HashMap<String,String>> cityData = new ArrayList<HashMap<String, String>>(  );

    public LocationClient mLocationClient;
    public BDLocationListener myListener = new MyLocationListener();
    public LocationClientOption option = new LocationClientOption();
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication = this;
        mCityDB = openCityDB();
        initCityDb();
        
        mLocationClient = new LocationClient( getApplicationContext() );
        mLocationClient.registerLocationListener( myListener );
        initLocation();
        mLocationClient.start();
        mLocationClient.requestLocation();
        Log.d( "MainActivity","num = ++++++++" + mLocationClient );
        mLocationClient.stop();
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
        Log.d(TAG,"localClient setted");
    }

    private void initCityDb() {
        mCityList = new ArrayList<City>();
        new Thread( new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        } ).start();
    }

    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();

        int i = 0;
        for(City city :mCityList){
            HashMap<String,String> item = new HashMap<String, String>(  );
            item.put( "citycode", city.getNumber() );
            item.put( "province", city.getProvince() );
            item.put( "cityname", city.getCity() );
            cityData.add( item );
        }
        Log.d( TAG,"cityListPrepared");
        return true;
    }

    public static  MyApplication getInstance(){
        return mApplication;
    }

    private CityDB openCityDB(){
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "database1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File( path );
        Log.d( TAG,path );
        if(!db.exists()){
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "database1"
                    + File.separator;
            File dirFirstFolder = new File( pathfolder );
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdir();
                Log.d( TAG,"mkdirs" );
            }
            Log.d(TAG,"db is not exists");
            try{
                InputStream is = getAssets().open( "city.db" );
                FileOutputStream fos = new FileOutputStream( db );
                int len = -1;
                byte[] buffer = new byte[1024];
                while((len = is.read(buffer)) != -1){
                    fos.write( buffer,0,len );
                    fos.flush();
                }
                fos.close();
                is.close();
            }catch (IOException E){
                E.printStackTrace();
                System.exit( 0 );
            }
        }
        return new CityDB( this, path );
    }

    public LocationClient getLocationClient(){
        return mLocationClient;
    }

    public List<HashMap<String,String>> getCityData(){
        return cityData;
    }
}
