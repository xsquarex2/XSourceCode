package com.example.design_pattern.结构型设计模式.代理模式;

// 静态代理类，与被代理类实现同一套接口
public class StaticProxy implements Subject {

    private Subject mSubject;

    public StaticProxy(Subject subject) {
        mSubject = subject;
    }

    @Override
    public void visit() {
        mSubject.visit();
    }
}