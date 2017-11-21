package com.jqorz.plantdetection.frame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jqorz.plantdetection.R;
import com.jqorz.plantdetection.base.BaseEventActivity;
import com.jqorz.plantdetection.bean.WebResult;
import com.jqorz.plantdetection.service.FileService;
import com.jqorz.plantdetection.leaf.LeafService;
import com.jqorz.plantdetection.service.WebService;
import com.jqorz.plantdetection.util.Logg;
import com.jqorz.plantdetection.util.MainEvent;
import com.jqorz.plantdetection.util.ToastUtil;
import com.jqorz.plantdetection.util.ToolUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 主界面
 */
public class MainActivity extends BaseEventActivity {
    private static final int RESULT_LOAD_IMAGE = 2020;
    private static final int RESULT_IMAGE_CUTTING = 3030;
    private static final String IMAGE_NAME = "pic_temp.jpg";
    private static final String PLANTS_DESCRIPTION = "/sdcard/PlantDetection/leaves description/";
    @BindView(R.id.tv_Content)
    TextView tv_Content;
    @BindView(R.id.iv_Pic)
    ImageView iv_Pic;
    private List<Map<String, String>> plantName = new ArrayList<>();
    private LeafService leafService;
    private WebService webService = new WebService();//使用此类连接百度API
    private FileService fileService = new FileService();//使用此类获取本地植物文本数据
    private boolean useWeb = true;

    @Override
    protected void init0() {

        initPermission();
        plantName = fileService.getNameList(PLANTS_DESCRIPTION);
        leafService = new LeafService(this);
        for (Map<String, String> map : plantName) {
            Logg.i(map.keySet());//打印出所有的已拥有的植物文本说明
        }
    }


    /**
     * 申请Android的存储权限
     */
    private void initPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x0010);
                }
            }
        } catch (Exception e) {
            ToastUtil.showToast(this, "请检查存储权限");
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MainEvent event) {
        switch (event.getArg1()) {
            case MainEvent.GET_PLANT_INFO:
                if (event.getArg2() == MainEvent.SUCC)
                    tv_Content.setText((String) event.getWhat());
                else
                    tv_Content.setText("ERROR");
                break;
            case MainEvent.GET_RESULT:
                if (event.getArg2() == MainEvent.SUCC) {
                    WebResult webResult = ((WebResult) event.getWhat());
                    List<WebResult.RES> list = webResult.getResult();
                    Logg.i(list.toString());
                    boolean isContinue = true;
                    for (WebResult.RES res : list) {
                        for (Map<String, String> name : plantName) {
                            if (name.containsKey(res.getName())) {
                                tv_Content.setText(name.get(res.getName()));
                                isContinue = false;
                                break;
                            }
                            if (name.containsKey(res.getName().replace("叶", ""))) {
                                tv_Content.setText(name.get(res.getName().replace("叶", "")));
                                isContinue = false;
                                break;
                            }
                            if (name.containsKey(res.getName() + "树")) {
                                tv_Content.setText(name.get(res.getName() + "树"));
                                isContinue = false;
                                break;
                            }

                        }
                        if (!isContinue) break;
                    }
                    if (isContinue) {
                        tv_Content.setText("匹配失败");
                    }
                } else {
                    ToastUtil.showToast(this, "ERROR");
                }
        }
    }

    @OnClick(R.id.iv_Pic)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_Pic:
                //调用系统图库进行图片选择
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                break;
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE://图片获取成功，进行裁剪操作

                if (resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(selectedImage, "image/*");
                    intent.putExtra("return-data", true);
                    intent.putExtra("crop", "");//调用裁剪
                    intent.putExtra("aspectX", 1); // 这个是裁剪时候的 裁剪框的 X 方向的比例
                    intent.putExtra("aspectY", 1); // 这个是裁剪时候的 裁剪框的 Y 方向的比例
                    intent.putExtra("outputX", 300);  //返回数据的时候的 X 像素大小
                    intent.putExtra("outputY", 300);  //返回的时候 Y 的像素大小
                    startActivityForResult(intent, RESULT_IMAGE_CUTTING);
                }
                break;
            case RESULT_IMAGE_CUTTING://裁剪成功，保存到本地
                if (data != null) {
                    Bitmap bitmap = data.getParcelableExtra("data");
                    try {
                        if (bitmap != null) {
                            iv_Pic.setImageBitmap(bitmap);
                            ToolUtil.saveBitmapToFile(bitmap, getFilesDir().getPath(), IMAGE_NAME);
                            tv_Content.setText("正在分析,请稍候...");

                            if (useWeb) {
                                webService.start(getFilesDir().getPath(), IMAGE_NAME);
                            } else {
                                startAnalysis(getFilesDir().getPath(), IMAGE_NAME);
                            }
                        }
                    } catch (IOException e) {
                        Logg.e(e);
                    }
                }
                break;
        }


    }

    /**
     * 开始进行分析
     * @param path 图像路径
     * @param imageName 图像名称
     */
    private void startAnalysis(String path, String imageName) {

        final String filePath = path.endsWith(File.separator) ? path + imageName : path + File.separator + imageName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String s = leafService.leaf(filePath);
                if (s != null) {
                    EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.GET_PLANT_INFO).setArg2(MainEvent.SUCC).setWhat(s));
                } else {
                    EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.GET_PLANT_INFO).setArg2(MainEvent.FAIL));
                }
            }
        }).start();
    }


}
