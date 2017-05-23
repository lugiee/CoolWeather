package com.lugiee.user.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    //1.
    //userdembp:~ user$ cd /Users/user/AndroidStudioProjects
    //userdembp:AndroidStudioProjects user$ cd /Users/user/AndroidStudioProjects/CoolWeather
    //userdembp:CoolWeather user$ git clone https://github.com/lugiee/CoolWeather.git
    //2.
    //userdembp:CoolWeather user$ git config --global user.name "lugiee"
    //userdembp:CoolWeather user$ git config --global user.email"lzh9014@163.com"
    //userdembp:CoolWeather user$ git add .
    //userdembp:CoolWeather user$ git commit -m "First commit"
    //3.
    //userdembp:CoolWeather user$ git add .
    //userdembp:CoolWeather user$ git commit -m "新增数据库帮助类，以及各表对应实体类"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
