package com.example.design_pattern.创建型设计模式.工厂模式.简单工厂模式;

import com.example.design_pattern.创建型设计模式.工厂模式.AbstractProduct;

// 简单工厂
public class SimpleFactory {

    public static <T extends AbstractProduct> T create(Class<T> clasz) {
        AbstractProduct product = null;
        try {
            product = (AbstractProduct) Class.forName(clasz.getName()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (T) product;
    }
}