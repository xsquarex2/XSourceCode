package com.example.design_pattern.行为型设计模式.责任链模式;

import android.text.TextUtils;

// 处理器2
public class Handler2 extends Handler {
    
    @Override
    public void handleRequest(String condition) {
        if (TextUtils.equals(condition, "Handler2")) {
            // process request
        } else {
            // next handler
            next.handleRequest(condition);
        }
    }
}