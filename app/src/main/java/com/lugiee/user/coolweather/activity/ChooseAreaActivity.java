package com.lugiee.user.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lugiee.user.coolweather.R;
import com.lugiee.user.coolweather.db.CoolWeatherDB;
import com.lugiee.user.coolweather.model.City;
import com.lugiee.user.coolweather.model.County;
import com.lugiee.user.coolweather.model.Province;
import com.lugiee.user.coolweather.util.HttpCallbackListener;
import com.lugiee.user.coolweather.util.HttpUtil;
import com.lugiee.user.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by user on 2017/5/23.
 */

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ListView listView;
    private TextView textView;
    private List<String> dataList = new ArrayList<String>();
    private CoolWeatherDB coolWeatherDB;
    /**
     * 当前选中的级别
     */
    private int currentLevel;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;
    private boolean isFromWeatherActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("city_selected",false)&& !isFromWeatherActivity){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choosearea);
        Log.e("chooseActivty", "oncreat");
        listView = (ListView) findViewById(R.id.list_view);
        textView = (TextView) findViewById(R.id.title_text);
        dataList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Log.d(TAG, countyCode+"h");
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("countyCode", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        Log.e("chooseActivity", "queryProvinces");
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            Log.e("chooseActivity", "size"+provinceList.size());
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            Log.e("chooseActivity", "datasize"+dataList.size());
            Log.e("chooseActivity", "data"+dataList.get(1));
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }
    /**
     * 查询全国所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities(){
        cityList=coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }

    }
    /**
     * 查询全国所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties(){
        countyList=coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }

    }
    private void queryFromServer(final String code, final String type) {
        Log.e("chooseActivity", "queryFromServer");
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.e("chooseActivty", "onFinish");
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result=Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result=Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
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
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        }else if(currentLevel==LEVEL_CITY){
            queryProvinces();
        }else{
            if (isFromWeatherActivity) {
                Log.d(TAG, "issi");
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();

        }
    }
}
