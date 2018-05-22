package com.example.design_pattern.创建型设计模式.工厂模式.工厂模式;

import com.example.design_pattern.创建型设计模式.工厂模式.AbstractProduct;
import com.example.design_pattern.创建型设计模式.工厂模式.ConcreteProductA;

// 具体工厂
public class ConcretetFactory {

    public static AbstractProduct create() {
        return new ConcreteProductA();
//        return new ConcreteProductB();
    }
}