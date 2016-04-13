package com.limxing.china;

/**
 * Created by limxing on 16/4/13.
 */
public class MoreBean {

    private String path;
    private boolean isChecked;

    public MoreBean(String ss, boolean b) {
        this.path=ss;
        this.isChecked=b;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
