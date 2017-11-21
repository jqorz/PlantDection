package com.jqorz.plantdetection.service;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.jqorz.plantdetection.bean.WebResult;
import com.jqorz.plantdetection.util.GsonUtil;
import com.jqorz.plantdetection.util.MainEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;


/**
 * 使用百度识图api进行检测
 */
public class WebService {

    private final String APP_ID = "10290165";
    private final String API_KEY = "zvgIKF4OskyFVuCnCAC7MYPk";
    private final String SECRET_KEY = "d5lwosLOC8hRNXGy0IBQNo8LTbPfrZpd";


    public void start(String path, String imageName) {
        final String filePath = path.endsWith(File.separator) ? path + imageName : path + File.separator + imageName;

        final AipImageClassify client = new AipImageClassify(APP_ID, API_KEY, SECRET_KEY);

        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject res = client.plantDetect(filePath, new HashMap<String, String>());
                WebResult result=  GsonUtil.Json2Bean(res.toString(), WebResult.class);
                EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.GET_RESULT).setArg2(MainEvent.SUCC).setWhat(result));
            }
        }).start();

    }
}
