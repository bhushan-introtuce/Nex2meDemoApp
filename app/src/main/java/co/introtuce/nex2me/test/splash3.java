package co.introtuce.nex2me.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class splash3 extends AppCompatActivity {


    LinearLayout Layout_bars;
    TextView[] bottomBars;
    int[] screens;
    TextView Next;
    ViewPager vp;
    MyViewPagerAdapter myvpAdapter;

    //For preferences
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "co.introtuce.nex2me.test";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash3);


        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);

        screens = new int[]{
                R.layout.intro_screen1,
//                R.layout.intro_screen2

        };

        vp = (ViewPager) findViewById(R.id.view_pager);
        Layout_bars = (LinearLayout) findViewById(R.id.layoutBars);
        Next = findViewById(R.id.next);
        myvpAdapter = new MyViewPagerAdapter();
        vp.setAdapter(myvpAdapter);

//        preferenceManager = new PreferenceManager(this);
        vp.addOnPageChangeListener(viewPagerPageChangeListener);
//        if (!preferenceManager.FirstLaunch()) {
//            launchMain();
//            finish();
//        }



        //ColoredBars(0);

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if(Next.getText().equals("Start"))
//                {
                    mPreferences.edit().putBoolean("firstrun", false).commit();
                    startActivity(new Intent(splash3.this,ModelTestActivity.class));
                    finish();

//                }else {
//                    vp.setCurrentItem(1);
//                }

            }
        });
    }

    public void next(View v) {
        int i = getItem(+1);
        if (i < screens.length) {
            vp.setCurrentItem(i);
        } else {
            launchMain();
        }
    }



    @SuppressLint("ResourceAsColor")
    private void ColoredBars(int thisScreen) {

        bottomBars = new TextView[screens.length];

        Layout_bars.removeAllViews();
        for (int i = 0; i < bottomBars.length; i++) {
            bottomBars[i] = new TextView(this);
            bottomBars[i].setTextSize(100);
            bottomBars[i].setText(Html.fromHtml("¯"));
            Layout_bars.addView(bottomBars[i]);
            bottomBars[i].setTextColor(R.color.colorPrimaryDark);
        }
        if (bottomBars.length > 0)
            bottomBars[thisScreen].setTextColor(R.color.my_primary);
    }

    private int getItem(int i) {
        return vp.getCurrentItem() + i;
    }

    private void launchMain() {
        mPreferences.edit().putBoolean("firstrun", false).commit();
        startActivity(new Intent(splash3.this, ModelTestActivity.class));
        finish();
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            //ColoredBars(position);
            if (position == screens.length - 1) {
                Next.setText("Start");
                //Skip.setVisibility(View.GONE);
            } else {
                Next.setText("Next");
               // Skip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(screens[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return screens.length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = (View) object;
            container.removeView(v);
        }

        @Override
        public boolean isViewFromObject(View v, Object object) {
            return v == object;
        }
    }
}