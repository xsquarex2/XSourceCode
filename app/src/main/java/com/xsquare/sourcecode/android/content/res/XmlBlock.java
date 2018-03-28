package com.xsquare.sourcecode.android.content.res;

import android.content.res.XmlResourceParser;

import org.xml.sax.Parser;

/**
 * Created by xsquare on 2018/3/28.
 */

public class XmlBlock {
    private final long mNative;

    public XmlBlock(byte[] data) {
        mAssets = null;
        mNative = nativeCreate(data, 0, data.length);
        mStrings = new StringBlock(nativeGetStringBlock(mNative), false);
    }

    public XmlBlock(byte[] data, int offset, int size) {
        mAssets = null;
        mNative = nativeCreate(data, offset, size);
        mStrings = new StringBlock(nativeGetStringBlock(mNative), false);
    }

    public XmlResourceParser newParser() {
        synchronized (this) {
            if (mNative != 0) {
                // mNative指向的是C++层的ResXMLTree对象的地址
                // nativeCreateParseState()根据这个地址找到ResXMLTree对象，利用ResXMLTree对象对象构建一个ResXMLParser对象，
                // 并将ResXMLParser对象 的地址封装进Java层的Parser对象中，构建一个Parser对象。
                // XmlBlock <--> ResXMLTree
                // Parser <--> ResXMLParser
                return new Parser(nativeCreateParseState(mNative), this);
            }
            return null;
        }
    }
}
