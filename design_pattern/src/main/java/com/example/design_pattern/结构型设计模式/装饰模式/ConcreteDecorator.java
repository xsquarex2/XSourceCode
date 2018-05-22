package com.example.design_pattern.结构型设计模式.装饰模式;

// 具体装饰类
public class ConcreteDecorator extends AbstractDecorator {

    public ConcreteDecorator(AbstractComponent component) {
        super(component);
    }

    @Override
    protected void operation() {
        operationA();
        super.operation();
        operationB();
    }

    private void operationA() {

    }

    private void operationB() {

    }
}