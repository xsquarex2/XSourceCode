package com.xsquare.sourcecode.android.content.res;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by xsquare on 2018/3/27.
 */

public class AssetManager implements AutoCloseable {
    @Override
    public void close() throws Exception {

    }
    /**
     * {@hide}
     * Retrieve a non-asset as a compiled XML file.  Not for use by
     * applications.
     * @param cookie Identifier of the package to be opened.
     * @param fileName Name of the asset to retrieve.
     */
    /*package*/ final XmlBlock openXmlBlockAsset(int cookie, String fileName)
            throws IOException {
        synchronized (this) {
            if (!mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            // 代开fileName指定的xml文件，成功打开该文件后，会得到C++层的ResXMLTree对象的地址xmlBlock，
            // 然后将xmlBlock封装进 XmlBlock中返回给调用者。

            // 具体实现也就是一个打开资源文件的过程，资源文件一般存放在APK中，APK是一个zip包，
            // 所以最终会调用openAssetFromZipLocked()方法打开xml文件。
            long xmlBlock = openXmlAssetNative(cookie, fileName);
            if (xmlBlock != 0) {
                XmlBlock res = new XmlBlock(this, xmlBlock);
                incRefsLocked(res.hashCode());
                return res;
            }
        }
        throw new FileNotFoundException("Asset XML file: " + fileName);
    }
    private native final long openXmlAssetNative(int cookie, String fileName);
}
