package com.xsquare.sourcecode.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Trace;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.*;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Created by xsquare on 2018/3/27.
 */
@SystemService(Context.LAYOUT_INFLATER_SERVICE)
public abstract class LayoutInflater {
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
        final com.xsquare.sourcecode.android.content.res.Resources res = getContext().getResources();
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
     * 1. 解析merge标签，rInflate()方法会将merge下面的所有子View直接添加到根容器中，
     * 这里我们也理解了为什么merge标签可以达到简化布局的效果。
     * 2. 不是merge标签那么直接调用createViewFromTag()方法解析成布局中的视图，
     * 这里的参数name就是要解析视图的类型，例如：ImageView。
     * 3. 调用generateLayoutParams()f方法生成布局参数，
     * 如果attachToRoot为false，即不添加到根容器里，为View设置布局参数。
     * 4. 调用rInflateChildren()方法解析当前View下面的所有子View。
     * 如果根容器不为空，且attachToRoot为true，则将解析出来的View添加到根容器中，
     * 如果根布局为空或者attachToRoot为false，那么解析出来的额View就是返回结果。返回解析出来的结果。
     *
     * @return View
     */
    public android.view.View inflate(XmlPullParser parser, @Nullable android.view.ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "inflate");
            //Context对象
            final Context inflaterContext = mContext;
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = (Context) mConstructorArgs[0];
            mConstructorArgs[0] = inflaterContext;
            //存储根视图
            android.view.View result = root;

            try {
                // Look for the root node.
                // 获取根元素
                int type;
                while ((type = parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                    // Empty
                }

                if (type != XmlPullParser.START_TAG) {
                    throw new InflateException(parser.getPositionDescription()
                            + ": No start tag found!");
                }
                //1. 解析merge标签，rInflate()方法会将merge下面的所有子View直接添加到根容器中，这里
                //我们也理解了为什么merge标签可以达到简化布局的效果。
                final String name = parser.getName();
                if (TAG_MERGE.equals(name)) {
                    if (root == null || !attachToRoot) {
                        throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }
                    //解析View树，
                    rInflate(parser, root, inflaterContext, attrs, false);
                } else {
                    // Temp is the root view that was found in the xml
                    // 不是merge标签那么直接调用createViewFromTag()方法解析成布局中的视图，
                    // 这里的参数name就是要解析视图的类型，例如：ImageView
                    final android.view.View temp = createViewFromTag(root, name, inflaterContext, attrs);
                    ViewGroup.LayoutParams params = null;
                    if (root != null) {
                        // 3. 调用generateLayoutParams()f方法生成布局参数，如果attachToRoot为false，
                        // 即不添加到根容器里，为View设置布局参数
                        params = root.generateLayoutParams(attrs);
                        if (!attachToRoot) {
                            // Set the layout params for temp if we are not
                            // attaching. (If we are, we use addView, below)
                            temp.setLayoutParams(params);
                        }
                    }
                    // 4. 调用rInflateChildren()方法解析当前View下面的所有子View
                    rInflateChildren(parser, temp, attrs, true);
                    // 如果根容器不为空，且attachToRoot为true，则将解析出来的View添加到根容器中
                    if (root != null && attachToRoot) {
                        root.addView(temp, params);
                    }
                    // 如果根布局为空或者attachToRoot为false，那么解析出来的额View就是返回结果
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
    /**
     * 用于递减xml层次结构并实例化视图的递归方法，实例化它们的子项，然后调用onFinishInflate（）。
     */
    void rInflate(XmlPullParser parser, android.view.View parent, Context context,
                  AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
        // 1. 获取树的深度，执行深度优先遍历
        final int depth = parser.getDepth();
        int type;
        boolean pendingRequestFocus = false;
        // 2. 逐个进行元素解析
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            final String name = parser.getName();

            if (TAG_REQUEST_FOCUS.equals(name)) {
                //3. 解析添加ad:focusable="true"的元素，并获取View焦点。
                pendingRequestFocus = true;
                consumeChildElements(parser);
            } else if (TAG_TAG.equals(name)) {
                //4. 解析View的tag。
                parseViewTag(parser, parent, attrs);
            } else if (TAG_INCLUDE.equals(name)) {
                if (parser.getDepth() == 0) {
                    throw new InflateException("<include /> cannot be the root element");
                }
                //5. 解析include标签，注意include标签不能作为根元素。
                parseInclude(parser, context, parent, attrs);
            } else if (TAG_MERGE.equals(name)) {//merge标签必须为根元素
                throw new InflateException("<merge /> must be the root element");
            } else {
                //6. 根据元素名进行解析，生成View。
                final View view = createViewFromTag(parent, name, context, attrs);
                final ViewGroup viewGroup = (ViewGroup) parent;
                final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
                //7. 递归调用解析该View里的所有子View，也是深度优先遍历，rInflateChildren内部调用的也是rInflate()方
                //法，只是传入了新的parent View
                rInflateChildren(parser, view, attrs, true);
                //8. 将解析出来的View添加到它的父View中。
                viewGroup.addView(view, params);
            }
        }
        if (pendingRequestFocus) {
            parent.restoreDefaultFocus();
        }
        if (finishInflate) {
            //9. 回调根容器的onFinishInflate()方法，这个方法我们应该很熟悉。
            parent.onFinishInflate();
        }
    }
    //调用的也是rInflate()方法，只是传入了新的parent View
    final void rInflateChildren(XmlPullParser parser, View parent, AttributeSet attrs,
                                boolean finishInflate) throws XmlPullParserException, IOException {
        rInflate(parser, parent, parent.getContext(), attrs, finishInflate);
    }

    /**
     * 创建View
     * @param parent 父view
     * @param name view标签
     * @param context context
     * @param attrs attrs
     * @return view
     */
    private View createViewFromTag(View parent, String name, Context context, AttributeSet attrs) {
        return createViewFromTag(parent, name, context, attrs, false);
    }
    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
                           boolean ignoreThemeAttr) {
        // 1. 解析view标签。注意是小写view，这个不太常用
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }
        // 2. 如果标签与主题相关，则需要将context与themeResId包裹成ContextThemeWrapper。
        if (!ignoreThemeAttr) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
            final int themeResId = ta.getResourceId(0, 0);
            if (themeResId != 0) {
                //context与themeResId包裹成ContextThemeWrapper。
                context = new ContextThemeWrapper(context, themeResId);
            }
            ta.recycle();
        }
        //3. BlinkLayout是一种会闪烁的布局，被包裹的内容会一直闪烁，像QQ消息那样。
        if (name.equals(TAG_1995)) {
            // Let's party like it's 1995!
            return new BlinkLayout(context, attrs);
        }

        try {
            View view;
            //4. 用户可以设置LayoutInflater的Factory来进行View的解析，但是默认情况下
            //这些Factory都是为空的。
            if (mFactory2 != null) {
                view = mFactory2.onCreateView(parent, name, context, attrs);
            } else if (mFactory != null) {
                view = mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }
            //5. 默认情况下没有Factory，而是通过onCreateView()方法对内置View进行解析，createView()
            //方法进行自定义View的解析。
            if (view == null && mPrivateFactory != null) {
                view = mPrivateFactory.onCreateView(parent, name, context, attrs);
            }

            if (view == null) {
                final Object lastContext = mConstructorArgs[0];
                mConstructorArgs[0] = context;
                try {
                    //这里有个小技巧，因为我们在使用自定义View的时候是需要在xml指定全路径的，例如：
                    //com.guoxiaoxing.CustomView，那么这里就有个.了，可以利用这一点判定是内置View
                    //还是自定义View，Google的工程师很机智的。
                    if (-1 == name.indexOf('.')) {
                        //内置view
                        view = onCreateView(parent, name, attrs);
                    } else {
                        //自定义view
                        view = createView(name, null, attrs);
                    }
                } finally {
                    mConstructorArgs[0] = lastContext;
                }
            }

            return view;
        } catch (InflateException e) {
            throw e;

        } catch (ClassNotFoundException e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription()
                    + ": Error inflating class " + name, e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;

        } catch (Exception e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription()
                    + ": Error inflating class " + name, e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        }
    }

    /**
     * 创建view
     * @param name name
     * @param prefix 前缀
     * @param attrs atrrs
     * @return view
     * @throws ClassNotFoundException
     * @throws InflateException
     */
    public final View createView(String name, String prefix, AttributeSet attrs)
            throws ClassNotFoundException, InflateException {
        //1. 从缓存中读取构造函数。
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        if (constructor != null && !verifyClassLoader(constructor)) {
            constructor = null;
            sConstructorMap.remove(name);
        }
        Class<? extends View> clazz = null;

        try {
            Trace.traceBegin(Trace.TRACE_TAG_VIEW, name);

            if (constructor == null) {
                //2. 没有在缓存中查找到构造函数，则构造完整的路径名，并加装该类。
                clazz = mContext.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);

                if (mFilter != null && clazz != null) {
                    boolean allowed = mFilter.onLoadClass(clazz);
                    if (!allowed) {
                        failNotAllowed(name, prefix, attrs);
                    }
                }
                //3. 从Class对象中获取构造函数，并在sConstructorMap做下缓存，方便下次使用。
                constructor = clazz.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } else {
                //4. 如果sConstructorMap中有当前View构造函数的缓存，则直接使用。
                if (mFilter != null) {
                    // Have we seen this name before?
                    Boolean allowedState = mFilterMap.get(name);
                    if (allowedState == null) {
                        // New class -- remember whether it is allowed
                        clazz = mContext.getClassLoader().loadClass(
                                prefix != null ? (prefix + name) : name).asSubclass(View.class);

                        boolean allowed = clazz != null && mFilter.onLoadClass(clazz);
                        mFilterMap.put(name, allowed);
                        if (!allowed) {
                            failNotAllowed(name, prefix, attrs);
                        }
                    } else if (allowedState.equals(Boolean.FALSE)) {
                        failNotAllowed(name, prefix, attrs);
                    }
                }
            }

            Object lastContext = mConstructorArgs[0];
            if (mConstructorArgs[0] == null) {
                // Fill in the context if not already within inflation.
                mConstructorArgs[0] = mContext;
            }
            Object[] args = mConstructorArgs;
            args[1] = attrs;
            //5. 利用构造函数，构建View对象。
            final View view = constructor.newInstance(args);
            if (view instanceof ViewStub) {
                // Use the same context when inflating ViewStub later.
                final ViewStub viewStub = (ViewStub) view;
                viewStub.setLayoutInflater(cloneInContext((Context) args[0]));
            }
            mConstructorArgs[0] = lastContext;
            return view;

        } catch (NoSuchMethodException e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription()
                    + ": Error inflating class " + (prefix != null ? (prefix + name) : name), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;

        } catch (ClassCastException e) {
            // If loaded class is not a View subclass
            final InflateException ie = new InflateException(attrs.getPositionDescription()
                    + ": Class is not a View " + (prefix != null ? (prefix + name) : name), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (ClassNotFoundException e) {
            // If loadClass fails, we should propagate the exception.
            throw e;
        } catch (Exception e) {
            final InflateException ie = new InflateException(
                    attrs.getPositionDescription() + ": Error inflating class "
                            + (clazz == null ? "<unknown>" : clazz.getName()), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
        }
    }
}
