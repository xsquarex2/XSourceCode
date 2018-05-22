package com.example.design_pattern.结构型设计模式.装饰模式;

// 抽象装饰类
public abstract class AbstractDecorator extends AbstractComponent {

    private AbstractComponent mComponent;

    public AbstractDecorator(AbstractComponent component) {
        mComponent = component;
    }

    @Override
    protected void operation() {
        mComponent.operation();
    }
}