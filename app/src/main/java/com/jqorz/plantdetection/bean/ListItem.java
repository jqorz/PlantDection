package com.jqorz.plantdetection.bean;

/**
 * Created by jqorz on 2017/10/27.
 */

public class ListItem {
    private String name;
    private String score;
    private String description;

    public ListItem(String name, String score, String description) {

        this.name = name;
        this.score = score;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }
}
