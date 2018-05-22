package com.example.design_pattern.创建型设计模式.原型模式;

public class Person implements Cloneable{

    public int age;
    public String name;

    @Override
    public Person clone() throws CloneNotSupportedException {
        return (Person) super.clone();
    }
    //原型模式要注意深拷贝和浅拷贝的问题，Object的clone()方法默认是钱拷贝，即对于引用对象拷贝的地址而不是值，
    // 所以要实现 深拷贝，在clone()方法里对于引用对象也有调用一下clone()方法，并且引用对象也要实现Cloneable接口和覆写clone()方法。
}