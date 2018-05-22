package com.example.design_pattern.行为型设计模式.责任链模式;

import android.text.TextUtils;

// 处理器1
public class Handler1 extends Handler {
    @Override
    public void handleRequest(String condition) {
        if (TextUtils.equals(condition, "Handler1")) {
            // process request
        } else {
            // next handler
            next.handleRequest(condition);
        }
    }
}