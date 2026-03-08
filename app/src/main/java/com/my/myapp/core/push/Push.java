package com.my.myapp.core.push;

public class Push {
    public final String packageName;
    public final String title;
    public final String text;

    public Push(String packageName, String title, String text) {
        this.packageName = packageName;
        this.title = title;
        this.text = text;
    }
}