package com.xsquare.sourcecode.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Trace;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.*;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by xsquare on 2018/3/27.
 */
@SystemService(Context.LAYOUT_INFLATER_SERVICE)
public class LayoutInflater {
    protected final Context mContext;
    /**
     * 加载
     * @param resource
     * @param root
     * @return
     */
    public android.view.View inflate(@LayoutRes int resource, @Nullable android.view.ViewGroup root) {
        return inflate(resource, root, root != null);
    }
    public android.view.View inflate(XmlPullParser parser, @Nullable android.view.ViewGroup root) {
        return inflate(parser, root, root != null);
    }

    /**
     * 加载布局文件
     * @param resource int resource：布局ID，也就是要解析的xml布局文件
     * @param root 表示根布局
     * @param attachToRoot 表示是否要添加到父布局root中去
     * 当attachToRoot == true且root ！= null时，新解析出来的View会被add到root中去，然后将root作为结果返回。
     * 当attachToRoot == false且root ！= null时，新解析的View会直接作为结果返回，而且root会为新解析的View生成LayoutParams并设置到该View中去。
     * 当attachToRoot == false且root == null时，新解析的View会直接作为结果返回。
     * @return View
     */
    public android.view.View inflate(@LayoutRes int resource, @Nullable android.view.ViewGroup root, boolean attachToRoot) {
        final Resources res = getContext().getResources();
        //获取xml资源解析器XmlResourceParser
        final XmlResourceParser parser = res.getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }
    public Context getContext() {
        return mContext;
    }

    /**
     * 当root为null时，新解析出来的View没有LayoutParams参数，
     * 这时候你设置的layout_width和layout_height是不生效的。
     *
     * Activity内部做了处理，我们知道Activity的setContentView()方法，实际上调用的PhoneWindow的setContentView()方法。
     * 它调用的时候将Activity的顶级DecorView（FrameLayout） 作为root传了进去，
     * mLayoutInflater.inflate(layoutResID, mContentParent)实际调用的是
     * inflate(resource, root, root != null)，所以在调用Activity的setContentView()方法时
     * 可以将解析出的View添加到顶级DecorView中，我们设置的layout_width和layout_height参数也可以生效。
     *
     * @return View
     */
    public android.view.View inflate(XmlPullParser parser, @Nullable android.view.ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "inflate");

            final Context inflaterContext = mContext;
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = (Context) mConstructorArgs[0];
            mConstructorArgs[0] = inflaterContext;
            android.view.View result = root;

            try {
                // Look for the root node.
                int type;
                while ((type = parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                    // Empty
                }

                if (type != XmlPullParser.START_TAG) {
                    throw new InflateException(parser.getPositionDescription()
                            + ": No start tag found!");
                }

                final String name = parser.getName();

                if (DEBUG) {
                    System.out.println("**************************");
                    System.out.println("Creating root view: "
                            + name);
                    System.out.println("**************************");
                }

                if (TAG_MERGE.equals(name)) {
                    if (root == null || !attachToRoot) {
                        throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }

                    rInflate(parser, root, inflaterContext, attrs, false);
                } else {
                    // Temp is the root view that was found in the xml
                    final android.view.View temp = createViewFromTag(root, name, inflaterContext, attrs);

                    ViewGroup.LayoutParams params = null;

                    if (root != null) {
                        if (DEBUG) {
                            System.out.println("Creating params from root: " +
                                    root);
                        }
                        // Create layout params that match root, if supplied
                        params = root.generateLayoutParams(attrs);
                        if (!attachToRoot) {
                            // Set the layout params for temp if we are not
                            // attaching. (If we are, we use addView, below)
                            temp.setLayoutParams(params);
                        }
                    }

                    if (DEBUG) {
                        System.out.println("-----> start inflating children");
                    }

                    // Inflate all children under temp against its context.
                    rInflateChildren(parser, temp, attrs, true);

                    if (DEBUG) {
                        System.out.println("-----> done inflating children");
                    }

                    // We are supposed to attach all the views we found (int temp)
                    // to root. Do that now.
                    if (root != null && attachToRoot) {
                        root.addView(temp, params);
                    }

                    // Decide whether to return the root that was passed in or the
                    // top view found in xml.
                    if (root == null || !attachToRoot) {
                        result = temp;
                    }
                }

            } catch (XmlPullParserException e) {
                final InflateException ie = new InflateException(e.getMessage(), e);
                ie.setStackTrace(EMPTY_STACK_TRACE);
                throw ie;
            } catch (Exception e) {
                final InflateException ie = new InflateException(parser.getPositionDescription()
                        + ": " + e.getMessage(), e);
                ie.setStackTrace(EMPTY_STACK_TRACE);
                throw ie;
            } finally {
                // Don't retain static reference on context.
                mConstructorArgs[0] = lastContext;
                mConstructorArgs[1] = null;

                Trace.traceEnd(Trace.TRACE_TAG_VIEW);
            }

            return result;
        }
    }
}
