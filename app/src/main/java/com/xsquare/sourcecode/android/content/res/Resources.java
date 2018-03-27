package com.xsquare.sourcecode.android.content.res;

import android.content.res.XmlResourceParser;
import android.support.annotation.AnyRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * Created by xsquare on 2018/3/27.
 */

public class Resources {
    public XmlResourceParser getLayout(@LayoutRes int id) throws android.content.res.Resources.NotFoundException {
        return loadXmlResourceParser(id, "layout");
    }
    XmlResourceParser loadXmlResourceParser(@AnyRes int id, @NonNull String type)
            throws android.content.res.Resources.NotFoundException {
        final TypedValue value = obtainTempTypedValue();
        try {
            final ResourcesImpl impl = mResourcesImpl;
            //1. 获取xml布局资源，并保存在TypedValue中。
            impl.getValue(id, value, true);
            if (value.type == TypedValue.TYPE_STRING) {
                //2. 加载对应的loadXmlResourceParser解析器。
                return impl.loadXmlResourceParser(value.string.toString(), id,
                        value.assetCookie, type);
            }
            throw new android.content.res.Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
                    + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }
    private TypedValue obtainTempTypedValue() {
        TypedValue tmpValue = null;
        synchronized (mTmpValueLock) {
            if (mTmpValue != null) {
                tmpValue = mTmpValue;
                mTmpValue = null;
            }
        }
        if (tmpValue == null) {
            return new TypedValue();
        }
        return tmpValue;
    }
}
