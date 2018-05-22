package com.example.design_pattern.行为型设计模式.观察者模式;

// 监听者
public class Observer {

    public void setup() {
        Observable observable = new Observable();
        observable.setListener(new Listener() {
            @Override
            public void change() {
                // TODO 监听的对象发生改变
            }
        });
    }
}