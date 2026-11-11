package com.fongmi.android.tv.bean;

public class Plugin {

    private String name;
    private String path;
    private boolean selected;
    private String type;

    public Plugin(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getType() {
        return type;
    }
}
