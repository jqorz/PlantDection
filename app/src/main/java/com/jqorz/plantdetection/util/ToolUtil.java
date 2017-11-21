package com.jqorz.plantdetection.util;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jqorz on 2017/10/26.
 */

public class ToolUtil {
    /**
     * 自定义View所需的dp单位（像素密度）转px单位（像素）
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 保存文件
     *
     * @param bm       需要保存的bitmap
     * @param path     路径
     * @param fileName 想保存的文件名
     * @throws IOException
     */
    public static void saveBitmapToFile(Bitmap bm, String path, String fileName) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        String filePath = path.endsWith(File.separator) ? path + fileName : path + File.separator + fileName;
        File myCaptureFile = new File(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }
}
