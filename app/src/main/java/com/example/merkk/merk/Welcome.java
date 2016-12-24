package com.example.merkk.merk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class Welcome extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {

    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;

    private static final int[] pics = {R.layout.welpage1,R.layout.welpage2};
    private ImageView[] dots;
    private int currentIndex;
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_welcome );
        SharedPreferences sharedPreferences = getSharedPreferences( "config", MODE_PRIVATE );
        Boolean firstOpen = sharedPreferences.getBoolean( "firstOpen",false );
        if(firstOpen){
            Intent i = new Intent( Welcome.this, MainAcitivity.class );
            startActivity( i );
            finish();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean( "firstOpen",true );
        editor.commit();
        initViews();
        btn = (Button) views.get( pics.length -1).findViewById( R.id.start );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( Welcome.this, MainAcitivity.class );
                startActivity( i );
                finish();
            }
        } );
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from( this );
        views = new ArrayList<View>();
        for(int i = 0; i< pics.length; i++){
            views.add( inflater.inflate( pics[i],null ) );
        }
        vpAdapter = new ViewPagerAdapter( views,this );
        vp = (ViewPager) findViewById( R.id.welcomePage );
        vp.setAdapter( vpAdapter );
        initDots();
        vp.setOnPageChangeListener( this );

    }

    private void initDots() {
        LinearLayout l1 = (LinearLayout) findViewById( R.id.ll );
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

    private void setCurView(int position){
        if(position < 0 || position >= pics.length){
            return;
        }
        vp.setCurrentItem( position );
    }
    private void setCurDot(int position){
        if(position < 0 || position >= pics.length  || currentIndex == position){
            return;
        }
        dots[position].setEnabled( true );
        dots[currentIndex] .setEnabled( false );
        currentIndex = position;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurDot( position );
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ll){
            int position = (Integer) v.getTag();
            setCurView( position );
            setCurDot( position );
        }

    }
}
