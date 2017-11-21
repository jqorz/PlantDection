package com.jqorz.plantdetection.leaf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.jqorz.plantdetection.util.Logg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cpu.KNN.KNN;
import cpu.KNN.TestKNN;

public class LeafService {
    private Bitmap source;
    private List<Double> Data = new ArrayList<>();
    private Context context;

    public LeafService(Context context) {
        this.context = context;
    }

    public String leaf(String picturePath) {
        Data.clear();

        source = BitmapFactory.decodeFile(picturePath);
        if (source == null) {
            return null;
        } else {
            source = blur(source);
        }

        //得到纹理特征 size=8
        Vein vein = new Vein(source);
        double[] w = vein.getT();
        Logg.i("w", w);

        //获得形状特征里近似宽比和7个Hu不变距 size=8
        double[] edge = EdgeDetector.getImage(source);
        Logg.i("edge", edge);

        //获得颜色特征中的颜色矩阵 3*3 size=9
        double[][] color = ColorUtil.getColormoment(source);
        for (double[] aColor : color) {
            for (double anAColor : aColor) {
                Data.add(anAColor);
            }

        }
        for (double aW : w) {
            Data.add(aW);
        }
        for (int j = 1; j < edge.length; j++) {
            Data.add(edge[j]);
        }

        TestKNN success = new TestKNN();
        String datafile = "/sdcard/PlantDetection/datafile.txt";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Data.size(); i++) {
            builder.append(Data.get(i)).append(" ");
        }
//        Logg.i("Data=", builder.toString());

        try {
            List<List<Double>> datas = new ArrayList<>();
            success.read(datas, datafile);
            KNN knn = new KNN();
            return readdescription(Math.round(Float.parseFloat((knn.knn(datas, Data, 3)))));

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * 使用RenderScript实现高斯低通滤波
     */
    public Bitmap blur(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //新建RenderScript对象
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        //设置参数
        blurScript.setRadius(20.0f);
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        allOut.copyTo(outBitmap);
        bitmap.recycle();
        rs.destroy();

        return outBitmap;

    }

    public String readdescription(int num) throws IOException {
        Logg.i("当前植物标号=" + num);

        String description = " ";
        File dir = new File("/sdcard/PlantDetection/leaves description");
        if (!dir.exists() || dir.isFile()) {
            return "路径有问题！";
        }
        String[] l = dir.list();
        File leafdir = new File(dir, l[num - 1]);
        InputStreamReader fReader = new InputStreamReader(new FileInputStream(leafdir), "GBK");
        BufferedReader br = new BufferedReader(fReader);// 构造一个BufferedReader类来读取文件
        String s;
        while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
            description = description + s + "|";
        }
        br.close();


        return description;
    }
}
