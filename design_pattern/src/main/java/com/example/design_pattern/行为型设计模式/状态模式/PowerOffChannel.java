package com.example.design_pattern.行为型设计模式.状态模式;

// 关机状态
public class PowerOffChannel implements TVState {
    @Override
    public void nextChannel() {
        // do nothing
    }

    @Override
    public void lastChannel() {
        // do nothing
    }
}