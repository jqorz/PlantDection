package com.jqorz.plantdetection.bean;

import java.util.List;

/**
 * Created by jqorz on 2017/10/27.
 */

public class WebResult {
    private String log_id;
    private List<RES> result;

    public String getLog_id() {
        return log_id;
    }

    public List<RES> getResult() {
        return result;
    }

    public static class RES {
        private String name;
        private String score;

        public String getName() {
            return name;
        }

        public String getScore() {
            return score;
        }

        @Override
        public String toString() {
            return "name=" + name + "\nscore=" + score;
        }
    }
}
