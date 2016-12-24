package app;

import android.app.Application;

import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bean.City;
import db.CityDB;


/**
 * Created by acer on 2016/10/27.
 */

public class MyApplication extends Application {
    private static  final  String TAG = "MyApp";

    private static MyApplication mApplication;

    private CityDB mCityDB;
    private List<City> mCityList;

    private List<HashMap<String,String>> cityData = new ArrayList<HashMap<String, String>>(  );


    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication = this;
        mCityDB = openCityDB();
        initCityDb();

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



    public List<HashMap<String,String>> getCityData(){
        return cityData;
    }
}
