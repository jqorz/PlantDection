package com.jqorz.plantdetection.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用于从本地植物百科中得到植物的数据
 */
public class FileService {

    public List<Map<String, String>> getNameList(final String dir) {
        final List<Map<String, String>> names = new ArrayList<>();

        File[] children = new File(dir).listFiles();
        for (File file : children) {
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
                BufferedReader br = new BufferedReader(isr);
                String name;
                String temp;
                StringBuilder content = new StringBuilder();
                Map<String, String> one = new HashMap<>();
                if ((name = br.readLine()) != null) {
                    content.append(name).append("\n");
                    while ((temp = br.readLine()) != null) {
                        content.append(temp).append("\n");
                    }
                }
                one.put(name, content.toString());
                names.add(one);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return names;

    }
}
