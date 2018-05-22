package com.example.design_pattern.结构型设计模式.适配器模式;

// 适配器
public class Adapter extends Adaptee implements TargetInterface {

    @Override
    public int getFive() {
        return 5;
    }
}