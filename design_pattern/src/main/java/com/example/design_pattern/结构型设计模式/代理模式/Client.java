package com.example.design_pattern.结构型设计模式.代理模式;

import java.lang.reflect.Proxy;

public class Client {

    public static void main(String[] args) {

        Subject subject = new ConcreteSubject();
        DynamicProxy proxy = new DynamicProxy(subject);

        // 动态生成代理类
        Subject proxySubject = (Subject) Proxy.newProxyInstance(DynamicProxy.class.getClassLoader()
                , subject.getClass().getInterfaces()
                , proxy);
        proxySubject.visit();

    }
}