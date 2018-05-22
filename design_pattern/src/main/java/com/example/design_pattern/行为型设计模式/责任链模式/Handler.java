package com.example.design_pattern.行为型设计模式.责任链模式;

// 处理器，定位行为和下一个处理器
public abstract class Handler {
    protected Handler next;

    public abstract void handleRequest(String condition);
}