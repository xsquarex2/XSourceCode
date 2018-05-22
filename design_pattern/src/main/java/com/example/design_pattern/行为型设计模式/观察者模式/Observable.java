package com.example.design_pattern.行为型设计模式.观察者模式;

// 被监听者
public class Observable {

    private Listener mListener;

    // 设置监听器
    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void onChange() {
        // 通知对象发生改变
        mListener.change();
    }
}