package com.example.design_pattern.行为型设计模式.命令模式;

// 调用者
public class Invoker {

    private AbstractCommand mCommmand;

    public Invoker(AbstractCommand command) {
        mCommmand = command;
    }

    public void invoke() {
        mCommmand.command();
    }
}