package com.example.design_pattern.创建型设计模式.单例模式;

public class DoubleCheck {
    private volatile static DoubleCheck doubleCheck;
    private DoubleCheck(){ }
    // volatile关键字保证了：instance实例对于所有线程都是可见的
    // 禁止了instance操作指令重排序。
    public static DoubleCheck getDoubleCheck() {
        // 第一次校验，防止不必要的同步。
        if(doubleCheck == null){
            // synchronized关键字加锁，保证每次只有一个线程执行对象初始化操作
            synchronized (DoubleCheck.class){
                // 第二次校验，进行判空，如果为空则执行初始化
                if(doubleCheck == null){
                    doubleCheck = new DoubleCheck();
                }
            }
        }
        return doubleCheck;
    }
    //volatile关键字禁止了instance 操作指令重排序，我们来解释一下，我们知道instance = new DoubleCheckSingleton()这个操作 在汇编指令里大致会做三件事情：
    //1给我们知道instance分配内存。
    //2调用DoubleCheckSingleton()构造方法。
    //3将构造的对象赋值给instance。

    //但是在真正执行的时候，Java编译器是允许指令乱序执行的（编译优化），所以上述3步的顺序得不到保证，有可能是132，试想一下，如果线程A没有执行第2步，
    // 先执行了 第3步，而恰在此时，线程B取走了instance对象，在使用instance对象时就会有问题，双层校验锁单例失败，
    // 而volatile关键字可以禁止指令重排序从而解决这个问题。
}
