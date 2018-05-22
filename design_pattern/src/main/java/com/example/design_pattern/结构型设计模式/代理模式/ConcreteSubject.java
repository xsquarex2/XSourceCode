package com.example.design_pattern.结构型设计模式.代理模式;

// 被代理类，完成实际的功能。
public class ConcreteSubject implements Subject {

    @Override
    public void visit() {
        System.out.println("visit");
    }
}