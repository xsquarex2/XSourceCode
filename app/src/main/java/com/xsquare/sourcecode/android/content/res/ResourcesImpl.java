package com.xsquare.sourcecode.android.content.res;

import android.content.res.*;
import android.content.res.Resources;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;

/**
 * Created by xsquare on 2018/3/27.
 */

public class ResourcesImpl {
    final AssetManager mAssets;
    /**
     * ResourcesImpl会缓存最近解析的4个xml资源，如果不在缓存里则调用
     * AssetManger的openXmlBlockAsset()方法创建一个XmlBlock。XmlBlock是已编译的xml文件的一个包装类。
     *
     * @param file xml文件的路径
     * @param id xml文件的资源ID
     * @param assetCookie xml文件的资源缓存
     * @param type 资源类型
     * @throws Resources.NotFoundException
     */
    XmlResourceParser loadXmlResourceParser(@NonNull String file, @AnyRes int id, int assetCookie,
                                            @NonNull String type)
            throws Resources.NotFoundException {
        if (id != 0) {
            try {
                synchronized (mCachedXmlBlocks) {
                    //... 从缓存中查找xml资源
                    final int[] cachedXmlBlockCookies = mCachedXmlBlockCookies;
                    final String[] cachedXmlBlockFiles = mCachedXmlBlockFiles;
                    final XmlBlock[] cachedXmlBlocks = mCachedXmlBlocks;
                    // First see if this block is in our cache.
                    final int num = cachedXmlBlockFiles.length;
                    for (int i = 0; i < num; i++) {
                        if (cachedXmlBlockCookies[i] == assetCookie && cachedXmlBlockFiles[i] != null
                                && cachedXmlBlockFiles[i].equals(file)) {
                            return cachedXmlBlocks[i].newParser();
                        }
                    }
                    // Not in the cache, create a new block and put it at
                    // the next slot in the cache.
                    final XmlBlock block = mAssets.openXmlBlockAsset(assetCookie, file);
                    if (block != null) {
                        final int pos = (mLastCachedXmlBlockIndex + 1) % num;
                        mLastCachedXmlBlockIndex = pos;
                        final XmlBlock oldBlock = cachedXmlBlocks[pos];
                        if (oldBlock != null) {
                            oldBlock.close();
                        }
                        cachedXmlBlockCookies[pos] = assetCookie;
                        cachedXmlBlockFiles[pos] = file;
                        cachedXmlBlocks[pos] = block;
                        return block.newParser();
                    }
                }
            } catch (Exception e) {
                final Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file
                        + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x"
                + Integer.toHexString(id));
    }
}
