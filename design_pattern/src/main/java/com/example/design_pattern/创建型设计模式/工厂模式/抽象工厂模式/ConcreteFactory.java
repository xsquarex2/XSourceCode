package com.example.design_pattern.创建型设计模式.工厂模式.抽象工厂模式;

// 具体工厂
public class ConcreteFactory extends AbstractFactory {
    
    @Override
    public AbstractProductA createProductA() {
        return new ConcreteProductA1();
    }

    @Override
    public AbstractProductB createProductB() {
        return new ConcreteProductB1();
    }
}