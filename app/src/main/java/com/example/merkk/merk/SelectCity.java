package com.example.merkk.merk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.MyApplication;

/**
 * Created by acer on 2016/10/18.
 */

public class SelectCity extends Activity implements  View.OnClickListener{
    private ImageView mBackBtn;
    private ListView listView;
    private EditText eText;
    private MyApplication myApplication ;
    private List<HashMap<String,String>> data;
    private List<HashMap<String,String>> refreshData = new ArrayList<HashMap<String, String>>(  );

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.select_city );
        myApplication = (MyApplication) getApplicationContext();
        data = myApplication.getCityData();
        mBackBtn = (ImageView) findViewById( R.id.title_back );
        mBackBtn.setOnClickListener( this );
        listView = (ListView) findViewById( R.id.db_city_list );
        listView.setOnItemClickListener( new ItemClickListener() );
        eText = (EditText)findViewById( R.id.search_edit );
        eText.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshData.clear();
                String text = s.toString();
                if(text.equals( "" )){
                    show( data );
                }
                Log.d("input+++++", text);
                for(HashMap<String,String> map : data){
                    if(map.get( "citycode" ).startsWith( s.toString() )){
                        refreshData.add( map );
                    }
                }
                if(refreshData != null)
                    show( refreshData );
            }
        } );
        show(data);
        /*
        eText.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<HashMap<String,String>> mList = (List<HashMap<String, String>>) new cityFiler( data ).performFiltering( s );
                show2(mList);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //show2();
            }
        } );
        */

    }

    private final class ItemClickListener implements AdapterView.OnItemClickListener{

        /**
         * view:当前所点击条目的view对象
         * position:当前所点击的条目它所绑定的数据在集合中的索引值
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lView = (ListView) parent;
            HashMap<String,String> item = (HashMap<String,String>) lView.getItemAtPosition( position );
            Intent i = new Intent(  );
            i.putExtra( "cityCode",item.get( "citycode" ) );
            setResult( RESULT_OK , i);
            SharedPreferences sharedPreferences = getSharedPreferences( "config", MODE_PRIVATE );
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString( "main_city_code",item.get( "citycode" ) );
            editor.commit();
            Log.d( "citycodeSS",item.get( "citycode" )  );
            finish();
        }
    }

    private void show(List<HashMap<String,String>> data){
        SimpleAdapter adapter = new SimpleAdapter( this, data,R.layout.cityitem,
                new String[]{"citycode","province", "cityname"},new int[] {R.id.db_city_citycode, R.id.db_city_province, R.id.db_city_city});
        listView.setAdapter( adapter );

    }
    private void show2(List<HashMap<String,String>> data){

        SimpleAdapter adapter = new SimpleAdapter( this, data,R.layout.cityitem,
                new String[]{"citycode","province", "cityname"},new int[] {R.id.db_city_citycode, R.id.db_city_province, R.id.db_city_city});
        listView.setAdapter( adapter );


    }
    private class cityFiler extends Filter{
        private List<HashMap<String,String>> original;
        public cityFiler(List list){
            original = (ArrayList<HashMap<String,String>>)list;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint == null || constraint.length() == 0){
                results.values = original;
                results.count = original.size();
            }else {
                List<HashMap<String,String>> mList = new ArrayList<HashMap<String,String>>();
                for(HashMap<String,String> city :original){
                    if((city.get( "province" ).contains( constraint.toString() ))||(city.get( "cityname" ).contains( constraint.toString() ))){
                        mList.add( city );
                    }
                }
                results.values = mList;
                results.count = mList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

        }
    }
    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.title_back:
                finish();
                break;
            default:
                break;
        }
    }
}
