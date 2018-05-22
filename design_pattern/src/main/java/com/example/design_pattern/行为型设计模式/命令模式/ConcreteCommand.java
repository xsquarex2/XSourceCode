package com.example.design_pattern.行为型设计模式.命令模式;

// 具体命令
public class ConcreteCommand implements AbstractCommand {

    private Receiver mReceiver;

    public ConcreteCommand(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    public void command() {
        mReceiver.action();
    }
}