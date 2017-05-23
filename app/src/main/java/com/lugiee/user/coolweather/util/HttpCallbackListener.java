package com.lugiee.user.coolweather.util;

/**
 * Created by user on 17/5/22.
 */

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
