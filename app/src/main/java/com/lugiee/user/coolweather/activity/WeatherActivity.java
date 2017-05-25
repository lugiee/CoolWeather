package com.lugiee.user.coolweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lugiee.user.coolweather.R;
import com.lugiee.user.coolweather.util.HttpCallbackListener;
import com.lugiee.user.coolweather.util.HttpUtil;
import com.lugiee.user.coolweather.util.Utility;

/**
 * Created by user on 2017/5/25.
 */

public class WeatherActivity extends Activity{
    private LinearLayout weatherInfoLayout;
    /*
     * 用于显示城市名
     */
    private TextView cityNameText;
    /*
     * 用于显示发布时间
     */
    private TextView publishText;
    /*
     * 用于显示天气描述小心
     */
    private TextView weatherdesptext;
    /*
     * 用于显示气温1
     */
    private TextView temp1Text;
    /*
     * 用于显示气温2
     */
    private TextView temp2Text;
    /*
     * 用于显示当前日期
     */
    private TextView currentDateText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_weather);
        //初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherdesptext = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        String countyCode=getIntent().getStringExtra("countyCode");
        if(!TextUtils.isEmpty(countyCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            queryWeathercode(countyCode);
        }else{
            //没有县级代码所就直接显示本地天气
            showWeather();
        }


    }
    /*
	 * 查询县级代码所对应的天气代号
	 */
    private void queryWeathercode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address,"countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }
    /**
     * 根据传入的地址和类型去服务器查询天气代号或者天气信息
     * @param address
     * @param type
     */
    private void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if(array != null && array.length == 2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }
    /*
	 * 从sharedPreferences文件中读取存储的天气信息，并显示到界面上
	 */
    private void showWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(sharedPreferences.getString("city_name", ""));
        temp1Text.setText(sharedPreferences.getString("temp1", ""));
        temp2Text.setText(sharedPreferences.getString("temp2", ""));
        weatherdesptext.setText(sharedPreferences.getString("weather_Desp", ""));
        publishText.setText("今天" + sharedPreferences.getString("publish_time", "") + "发布");
        currentDateText.setText(sharedPreferences.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }
}
