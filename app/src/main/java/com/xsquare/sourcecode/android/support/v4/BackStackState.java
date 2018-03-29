package com.xsquare.sourcecode.android.support.v4;

import android.os.Build;
import android.support.v4.app.BackStackRecord;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerImpl;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LogWriter;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by xsquare on 2018/3/29.
 */

public class BackStackState {

    /**
     * BackStackRecord将Fragment与相应应的操作包装起来，传递给FragmentManager调用
     */
    final class BackStackRecord extends FragmentTransaction implements
            FragmentManager.BackStackEntry, FragmentManagerImpl.OpGenerator {
        static final String TAG = FragmentManagerImpl.TAG;
        static final boolean SUPPORTS_TRANSITIONS = Build.VERSION.SDK_INT >= 21;

        final FragmentManagerImpl mManager;

        static final int OP_NULL = 0;
        static final int OP_ADD = 1;//add
        static final int OP_REPLACE = 2;//replace
        static final int OP_REMOVE = 3;//remove
        static final int OP_HIDE = 4;//hide
        static final int OP_SHOW = 5;//show
        static final int OP_DETACH = 6;//detach
        static final int OP_ATTACH = 7;//attach
        static final int OP_SET_PRIMARY_NAV = 8;//set primary nav
        static final int OP_UNSET_PRIMARY_NAV = 9;//unset primary nav
        //封装了操作命令、Fragment、动画等内容
        static final class Op {
            int cmd;
            Fragment fragment;
            int enterAnim;
            int exitAnim;
            int popEnterAnim;
            int popExitAnim;

            Op() {
            }

            Op(int cmd, Fragment fragment) {
                this.cmd = cmd;
                this.fragment = fragment;
            }
        }

        ArrayList<android.support.v4.app.BackStackRecord.Op> mOps = new ArrayList<>();
        int mEnterAnim;
        int mExitAnim;
        int mPopEnterAnim;
        int mPopExitAnim;
        int mTransition;
        int mTransitionStyle;
        boolean mAddToBackStack;
        boolean mAllowAddToBackStack = true;
        String mName;
        boolean mCommitted;
        int mIndex = -1;

        int mBreadCrumbTitleRes;
        CharSequence mBreadCrumbTitleText;
        int mBreadCrumbShortTitleRes;
        CharSequence mBreadCrumbShortTitleText;

        ArrayList<String> mSharedElementSourceNames;
        ArrayList<String> mSharedElementTargetNames;
        boolean mReorderingAllowed = false;

        ArrayList<Runnable> mCommitRunnables;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("BackStackEntry{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            if (mIndex >= 0) {
                sb.append(" #");
                sb.append(mIndex);
            }
            if (mName != null) {
                sb.append(" ");
                sb.append(mName);
            }
            sb.append("}");
            return sb.toString();
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            dump(prefix, writer, true);
        }

        public void dump(String prefix, PrintWriter writer, boolean full) {
            if (full) {
                writer.print(prefix); writer.print("mName="); writer.print(mName);
                writer.print(" mIndex="); writer.print(mIndex);
                writer.print(" mCommitted="); writer.println(mCommitted);
                if (mTransition != FragmentTransaction.TRANSIT_NONE) {
                    writer.print(prefix); writer.print("mTransition=#");
                    writer.print(Integer.toHexString(mTransition));
                    writer.print(" mTransitionStyle=#");
                    writer.println(Integer.toHexString(mTransitionStyle));
                }
                if (mEnterAnim != 0 || mExitAnim !=0) {
                    writer.print(prefix); writer.print("mEnterAnim=#");
                    writer.print(Integer.toHexString(mEnterAnim));
                    writer.print(" mExitAnim=#");
                    writer.println(Integer.toHexString(mExitAnim));
                }
                if (mPopEnterAnim != 0 || mPopExitAnim !=0) {
                    writer.print(prefix); writer.print("mPopEnterAnim=#");
                    writer.print(Integer.toHexString(mPopEnterAnim));
                    writer.print(" mPopExitAnim=#");
                    writer.println(Integer.toHexString(mPopExitAnim));
                }
                if (mBreadCrumbTitleRes != 0 || mBreadCrumbTitleText != null) {
                    writer.print(prefix); writer.print("mBreadCrumbTitleRes=#");
                    writer.print(Integer.toHexString(mBreadCrumbTitleRes));
                    writer.print(" mBreadCrumbTitleText=");
                    writer.println(mBreadCrumbTitleText);
                }
                if (mBreadCrumbShortTitleRes != 0 || mBreadCrumbShortTitleText != null) {
                    writer.print(prefix); writer.print("mBreadCrumbShortTitleRes=#");
                    writer.print(Integer.toHexString(mBreadCrumbShortTitleRes));
                    writer.print(" mBreadCrumbShortTitleText=");
                    writer.println(mBreadCrumbShortTitleText);
                }
            }

            if (!mOps.isEmpty()) {
                writer.print(prefix); writer.println("Operations:");
                String innerPrefix = prefix + "    ";
                final int numOps = mOps.size();
                for (int opNum = 0; opNum < numOps; opNum++) {
                    final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                    String cmdStr;
                    switch (op.cmd) {
                        case OP_NULL: cmdStr="NULL"; break;
                        case OP_ADD: cmdStr="ADD"; break;
                        case OP_REPLACE: cmdStr="REPLACE"; break;
                        case OP_REMOVE: cmdStr="REMOVE"; break;
                        case OP_HIDE: cmdStr="HIDE"; break;
                        case OP_SHOW: cmdStr="SHOW"; break;
                        case OP_DETACH: cmdStr="DETACH"; break;
                        case OP_ATTACH: cmdStr="ATTACH"; break;
                        case OP_SET_PRIMARY_NAV: cmdStr="SET_PRIMARY_NAV"; break;
                        case OP_UNSET_PRIMARY_NAV: cmdStr="UNSET_PRIMARY_NAV";break;
                        default: cmdStr="cmd=" + op.cmd; break;
                    }
                    writer.print(prefix); writer.print("  Op #"); writer.print(opNum);
                    writer.print(": "); writer.print(cmdStr);
                    writer.print(" "); writer.println(op.fragment);
                    if (full) {
                        if (op.enterAnim != 0 || op.exitAnim != 0) {
                            writer.print(prefix); writer.print("enterAnim=#");
                            writer.print(Integer.toHexString(op.enterAnim));
                            writer.print(" exitAnim=#");
                            writer.println(Integer.toHexString(op.exitAnim));
                        }
                        if (op.popEnterAnim != 0 || op.popExitAnim != 0) {
                            writer.print(prefix); writer.print("popEnterAnim=#");
                            writer.print(Integer.toHexString(op.popEnterAnim));
                            writer.print(" popExitAnim=#");
                            writer.println(Integer.toHexString(op.popExitAnim));
                        }
                    }
                }
            }
        }

        public BackStackRecord(FragmentManagerImpl manager) {
            mManager = manager;
        }

        @Override
        public int getId() {
            return mIndex;
        }

        @Override
        public int getBreadCrumbTitleRes() {
            return mBreadCrumbTitleRes;
        }

        @Override
        public int getBreadCrumbShortTitleRes() {
            return mBreadCrumbShortTitleRes;
        }

        @Override
        public CharSequence getBreadCrumbTitle() {
            if (mBreadCrumbTitleRes != 0) {
                return mManager.mHost.getContext().getText(mBreadCrumbTitleRes);
            }
            return mBreadCrumbTitleText;
        }

        @Override
        public CharSequence getBreadCrumbShortTitle() {
            if (mBreadCrumbShortTitleRes != 0) {
                return mManager.mHost.getContext().getText(mBreadCrumbShortTitleRes);
            }
            return mBreadCrumbShortTitleText;
        }

        void addOp(android.support.v4.app.BackStackRecord.Op op) {
            mOps.add(op);
            op.enterAnim = mEnterAnim;
            op.exitAnim = mExitAnim;
            op.popEnterAnim = mPopEnterAnim;
            op.popExitAnim = mPopExitAnim;
        }

        @Override
        public FragmentTransaction add(Fragment fragment, String tag) {
            doAddOp(0, fragment, tag, OP_ADD);
            return this;
        }

        @Override
        public FragmentTransaction add(int containerViewId, Fragment fragment) {
            doAddOp(containerViewId, fragment, null, OP_ADD);
            return this;
        }

        @Override
        public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) {
            doAddOp(containerViewId, fragment, tag, OP_ADD);
            return this;
        }

        private void doAddOp(int containerViewId, Fragment fragment, String tag, int opcmd) {
            final Class fragmentClass = fragment.getClass();
            final int modifiers = fragmentClass.getModifiers();
            if (fragmentClass.isAnonymousClass() || !Modifier.isPublic(modifiers)
                    || (fragmentClass.isMemberClass() && !Modifier.isStatic(modifiers))) {
                throw new IllegalStateException("Fragment " + fragmentClass.getCanonicalName()
                        + " must be a public static class to be  properly recreated from"
                        + " instance state.");
            }

            fragment.mFragmentManager = mManager;

            if (tag != null) {
                if (fragment.mTag != null && !tag.equals(fragment.mTag)) {
                    throw new IllegalStateException("Can't change tag of fragment "
                            + fragment + ": was " + fragment.mTag
                            + " now " + tag);
                }
                fragment.mTag = tag;
            }

            if (containerViewId != 0) {
                if (containerViewId == View.NO_ID) {
                    throw new IllegalArgumentException("Can't add fragment "
                            + fragment + " with tag " + tag + " to container view with no id");
                }
                if (fragment.mFragmentId != 0 && fragment.mFragmentId != containerViewId) {
                    throw new IllegalStateException("Can't change container ID of fragment "
                            + fragment + ": was " + fragment.mFragmentId
                            + " now " + containerViewId);
                }
                fragment.mContainerId = fragment.mFragmentId = containerViewId;
            }

            addOp(new android.support.v4.app.BackStackRecord.Op(opcmd, fragment));
        }

        @Override
        public FragmentTransaction replace(int containerViewId, Fragment fragment) {
            return replace(containerViewId, fragment, null);
        }

        @Override
        public FragmentTransaction replace(int containerViewId, Fragment fragment, String tag) {
            if (containerViewId == 0) {
                throw new IllegalArgumentException("Must use non-zero containerViewId");
            }

            doAddOp(containerViewId, fragment, tag, OP_REPLACE);
            return this;
        }

        @Override
        public FragmentTransaction remove(Fragment fragment) {
            addOp(new android.support.v4.app.BackStackRecord.Op(OP_REMOVE, fragment));

            return this;
        }

        @Override
        public FragmentTransaction hide(Fragment fragment) {
            addOp(new android.support.v4.app.BackStackRecord.Op(OP_HIDE, fragment));

            return this;
        }

        @Override
        public FragmentTransaction show(Fragment fragment) {
            addOp(new android.support.v4.app.BackStackRecord.Op(OP_SHOW, fragment));

            return this;
        }

        @Override
        public FragmentTransaction detach(Fragment fragment) {
            addOp(new android.support.v4.app.BackStackRecord.Op(OP_DETACH, fragment));

            return this;
        }

        @Override
        public FragmentTransaction attach(Fragment fragment) {
            addOp(new android.support.v4.app.BackStackRecord.Op(OP_ATTACH, fragment));

            return this;
        }

        @Override
        public FragmentTransaction setPrimaryNavigationFragment(Fragment fragment) {
            addOp(new android.support.v4.app.BackStackRecord.Op(OP_SET_PRIMARY_NAV, fragment));

            return this;
        }

        @Override
        public FragmentTransaction setCustomAnimations(int enter, int exit) {
            return setCustomAnimations(enter, exit, 0, 0);
        }

        @Override
        public FragmentTransaction setCustomAnimations(int enter, int exit,
                                                       int popEnter, int popExit) {
            mEnterAnim = enter;
            mExitAnim = exit;
            mPopEnterAnim = popEnter;
            mPopExitAnim = popExit;
            return this;
        }

        @Override
        public FragmentTransaction setTransition(int transition) {
            mTransition = transition;
            return this;
        }

        @Override
        public FragmentTransaction addSharedElement(View sharedElement, String name) {
            if (SUPPORTS_TRANSITIONS) {
                String transitionName = ViewCompat.getTransitionName(sharedElement);
                if (transitionName == null) {
                    throw new IllegalArgumentException("Unique transitionNames are required for all" +
                            " sharedElements");
                }
                if (mSharedElementSourceNames == null) {
                    mSharedElementSourceNames = new ArrayList<String>();
                    mSharedElementTargetNames = new ArrayList<String>();
                } else if (mSharedElementTargetNames.contains(name)) {
                    throw new IllegalArgumentException("A shared element with the target name '"
                            + name + "' has already been added to the transaction.");
                } else if (mSharedElementSourceNames.contains(transitionName)) {
                    throw new IllegalArgumentException("A shared element with the source name '"
                            + transitionName + " has already been added to the transaction.");
                }

                mSharedElementSourceNames.add(transitionName);
                mSharedElementTargetNames.add(name);
            }
            return this;
        }

        @Override
        public FragmentTransaction setTransitionStyle(int styleRes) {
            mTransitionStyle = styleRes;
            return this;
        }

        @Override
        public FragmentTransaction addToBackStack(String name) {
            if (!mAllowAddToBackStack) {
                throw new IllegalStateException(
                        "This FragmentTransaction is not allowed to be added to the back stack.");
            }
            mAddToBackStack = true;
            mName = name;
            return this;
        }

        @Override
        public boolean isAddToBackStackAllowed() {
            return mAllowAddToBackStack;
        }

        @Override
        public FragmentTransaction disallowAddToBackStack() {
            if (mAddToBackStack) {
                throw new IllegalStateException(
                        "This transaction is already being added to the back stack");
            }
            mAllowAddToBackStack = false;
            return this;
        }

        @Override
        public FragmentTransaction setBreadCrumbTitle(int res) {
            mBreadCrumbTitleRes = res;
            mBreadCrumbTitleText = null;
            return this;
        }

        @Override
        public FragmentTransaction setBreadCrumbTitle(CharSequence text) {
            mBreadCrumbTitleRes = 0;
            mBreadCrumbTitleText = text;
            return this;
        }

        @Override
        public FragmentTransaction setBreadCrumbShortTitle(int res) {
            mBreadCrumbShortTitleRes = res;
            mBreadCrumbShortTitleText = null;
            return this;
        }

        @Override
        public FragmentTransaction setBreadCrumbShortTitle(CharSequence text) {
            mBreadCrumbShortTitleRes = 0;
            mBreadCrumbShortTitleText = text;
            return this;
        }

        void bumpBackStackNesting(int amt) {
            if (!mAddToBackStack) {
                return;
            }
            if (FragmentManagerImpl.DEBUG) Log.v(TAG, "Bump nesting in " + this
                    + " by " + amt);
            final int numOps = mOps.size();
            for (int opNum = 0; opNum < numOps; opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                if (op.fragment != null) {
                    op.fragment.mBackStackNesting += amt;
                    if (FragmentManagerImpl.DEBUG) Log.v(TAG, "Bump nesting of "
                            + op.fragment + " to " + op.fragment.mBackStackNesting);
                }
            }
        }

        @Override
        public FragmentTransaction runOnCommit(Runnable runnable) {
            if (runnable == null) {
                throw new IllegalArgumentException("runnable cannot be null");
            }
            disallowAddToBackStack();
            if (mCommitRunnables == null) {
                mCommitRunnables = new ArrayList<>();
            }
            mCommitRunnables.add(runnable);
            return this;
        }

        public void runOnCommitRunnables() {
            if (mCommitRunnables != null) {
                for (int i = 0, N = mCommitRunnables.size(); i < N; i++) {
                    mCommitRunnables.get(i).run();
                }
                mCommitRunnables = null;
            }
        }

        @Override
        public int commit() {
            return commitInternal(false);
        }

        @Override
        public int commitAllowingStateLoss() {
            return commitInternal(true);
        }

        @Override
        public void commitNow() {
            disallowAddToBackStack();
            mManager.execSingleAction(this, false);
        }

        @Override
        public void commitNowAllowingStateLoss() {
            disallowAddToBackStack();
            mManager.execSingleAction(this, true);
        }

        @Override
        public FragmentTransaction setReorderingAllowed(boolean reorderingAllowed) {
            mReorderingAllowed = reorderingAllowed;
            return this;
        }

        @Override
        public FragmentTransaction setAllowOptimization(boolean allowOptimization) {
            return setReorderingAllowed(allowOptimization);
        }

        /**
         * allowStateLoss是个标志位，表示是否允许状态丢失
         */
        int commitInternal(boolean allowStateLoss) {
            if (mCommitted) throw new IllegalStateException("commit already called");
            if (FragmentManagerImpl.DEBUG) {
                Log.v(TAG, "Commit: " + this);
                LogWriter logw = new LogWriter(TAG);
                PrintWriter pw = new PrintWriter(logw);
                dump("  ", null, pw, null);
                pw.close();
            }
            mCommitted = true;
            if (mAddToBackStack) {
                mIndex = mManager.allocBackStackIndex(this);
            } else {
                mIndex = -1;
            }
            mManager.enqueueAction(this, allowStateLoss);
            return mIndex;
        }

        /**
         * Implementation of {@link FragmentManagerImpl.OpGenerator}.
         * This operation is added to the list of pending actions during {@link #commit()}, and
         * will be executed on the UI thread to run this FragmentTransaction.
         *
         * @param records Modified to add this BackStackRecord
         * @param isRecordPop Modified to add a false (this isn't a pop)
         * @return true always because the records and isRecordPop will always be changed
         */
        @Override
        public boolean generateOps(ArrayList<android.support.v4.app.BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
            if (FragmentManagerImpl.DEBUG) {
                Log.v(TAG, "Run: " + this);
            }

            records.add(this);
            isRecordPop.add(false);
            if (mAddToBackStack) {
                mManager.addBackStackState(this);
            }
            return true;
        }

        boolean interactsWith(int containerId) {
            final int numOps = mOps.size();
            for (int opNum = 0; opNum < numOps; opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                final int fragContainer = op.fragment != null ? op.fragment.mContainerId : 0;
                if (fragContainer != 0 && fragContainer == containerId) {
                    return true;
                }
            }
            return false;
        }

        boolean interactsWith(ArrayList<android.support.v4.app.BackStackRecord> records, int startIndex, int endIndex) {
            if (endIndex == startIndex) {
                return false;
            }
            final int numOps = mOps.size();
            int lastContainer = -1;
            for (int opNum = 0; opNum < numOps; opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                final int container = op.fragment != null ? op.fragment.mContainerId : 0;
                if (container != 0 && container != lastContainer) {
                    lastContainer = container;
                    for (int i = startIndex; i < endIndex; i++) {
                        android.support.v4.app.BackStackRecord record = records.get(i);
                        final int numThoseOps = record.mOps.size();
                        for (int thoseOpIndex = 0; thoseOpIndex < numThoseOps; thoseOpIndex++) {
                            final android.support.v4.app.BackStackRecord.Op thatOp = record.mOps.get(thoseOpIndex);
                            final int thatContainer = thatOp.fragment != null
                                    ? thatOp.fragment.mContainerId : 0;
                            if (thatContainer == container) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Executes the operations contained within this transaction. The Fragment states will only
         * be modified if optimizations are not allowed.
         */
        void executeOps() {
            final int numOps = mOps.size();
            for (int opNum = 0; opNum < numOps; opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                final Fragment f = op.fragment;
                if (f != null) {
                    f.setNextTransition(mTransition, mTransitionStyle);
                }
                switch (op.cmd) {
                    case OP_ADD:
                        f.setNextAnim(op.enterAnim);
                        mManager.addFragment(f, false);
                        break;
                    case OP_REMOVE:
                        f.setNextAnim(op.exitAnim);
                        mManager.removeFragment(f);
                        break;
                    case OP_HIDE:
                        f.setNextAnim(op.exitAnim);
                        mManager.hideFragment(f);
                        break;
                    case OP_SHOW:
                        f.setNextAnim(op.enterAnim);
                        mManager.showFragment(f);
                        break;
                    case OP_DETACH:
                        f.setNextAnim(op.exitAnim);
                        mManager.detachFragment(f);
                        break;
                    case OP_ATTACH:
                        f.setNextAnim(op.enterAnim);
                        mManager.attachFragment(f);
                        break;
                    case OP_SET_PRIMARY_NAV:
                        mManager.setPrimaryNavigationFragment(f);
                        break;
                    case OP_UNSET_PRIMARY_NAV:
                        mManager.setPrimaryNavigationFragment(null);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
                }
                if (!mReorderingAllowed && op.cmd != OP_ADD && f != null) {
                    mManager.moveFragmentToExpectedState(f);
                }
            }
            if (!mReorderingAllowed) {
                // Added fragments are added at the end to comply with prior behavior.
                mManager.moveToState(mManager.mCurState, true);
            }
        }

        /**
         * Reverses the execution of the operations within this transaction. The Fragment states will
         * only be modified if reordering is not allowed.
         *
         * @param moveToState {@code true} if added fragments should be moved to their final state
         *                    in ordered transactions
         */
        void executePopOps(boolean moveToState) {
            for (int opNum = mOps.size() - 1; opNum >= 0; opNum--) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                Fragment f = op.fragment;
                if (f != null) {
                    f.setNextTransition(FragmentManagerImpl.reverseTransit(mTransition),
                            mTransitionStyle);
                }
                switch (op.cmd) {
                    case OP_ADD:
                        f.setNextAnim(op.popExitAnim);
                        mManager.removeFragment(f);
                        break;
                    case OP_REMOVE:
                        f.setNextAnim(op.popEnterAnim);
                        mManager.addFragment(f, false);
                        break;
                    case OP_HIDE:
                        f.setNextAnim(op.popEnterAnim);
                        mManager.showFragment(f);
                        break;
                    case OP_SHOW:
                        f.setNextAnim(op.popExitAnim);
                        mManager.hideFragment(f);
                        break;
                    case OP_DETACH:
                        f.setNextAnim(op.popEnterAnim);
                        mManager.attachFragment(f);
                        break;
                    case OP_ATTACH:
                        f.setNextAnim(op.popExitAnim);
                        mManager.detachFragment(f);
                        break;
                    case OP_SET_PRIMARY_NAV:
                        mManager.setPrimaryNavigationFragment(null);
                        break;
                    case OP_UNSET_PRIMARY_NAV:
                        mManager.setPrimaryNavigationFragment(f);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
                }
                if (!mReorderingAllowed && op.cmd != OP_REMOVE && f != null) {
                    mManager.moveFragmentToExpectedState(f);
                }
            }
            if (!mReorderingAllowed && moveToState) {
                mManager.moveToState(mManager.mCurState, true);
            }
        }

        /**
         * Expands all meta-ops into their more primitive equivalents. This must be called prior to
         * {@link #executeOps()} or any other call that operations on mOps for forward navigation.
         * It should not be called for pop/reverse navigation operations.
         *
         * <p>Removes all OP_REPLACE ops and replaces them with the proper add and remove
         * operations that are equivalent to the replace.</p>
         *
         * <p>Adds OP_UNSET_PRIMARY_NAV ops to match OP_SET_PRIMARY_NAV, OP_REMOVE and OP_DETACH
         * ops so that we can restore the old primary nav fragment later. Since callers call this
         * method in a loop before running ops from several transactions at once, the caller should
         * pass the return value from this method as the oldPrimaryNav parameter for the next call.
         * The first call in such a loop should pass the value of
         * {@link FragmentManager#getPrimaryNavigationFragment()}.</p>
         *
         * @param added Initialized to the fragments that are in the mManager.mAdded, this
         *              will be modified to contain the fragments that will be in mAdded
         *              after the execution ({@link #executeOps()}.
         * @param oldPrimaryNav The tracked primary navigation fragment as of the beginning of
         *                      this set of ops
         * @return the new oldPrimaryNav fragment after this record's ops would be run
         */
        @SuppressWarnings("ReferenceEquality")
        Fragment expandOps(ArrayList<Fragment> added, Fragment oldPrimaryNav) {
            for (int opNum = 0; opNum < mOps.size(); opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                switch (op.cmd) {
                    case OP_ADD:
                    case OP_ATTACH:
                        added.add(op.fragment);
                        break;
                    case OP_REMOVE:
                    case OP_DETACH: {
                        added.remove(op.fragment);
                        if (op.fragment == oldPrimaryNav) {
                            mOps.add(opNum, new android.support.v4.app.BackStackRecord.Op(OP_UNSET_PRIMARY_NAV, op.fragment));
                            opNum++;
                            oldPrimaryNav = null;
                        }
                    }
                    break;
                    case OP_REPLACE: {
                        final Fragment f = op.fragment;
                        final int containerId = f.mContainerId;
                        boolean alreadyAdded = false;
                        for (int i = added.size() - 1; i >= 0; i--) {
                            final Fragment old = added.get(i);
                            if (old.mContainerId == containerId) {
                                if (old == f) {
                                    alreadyAdded = true;
                                } else {
                                    // This is duplicated from above since we only make
                                    // a single pass for expanding ops. Unset any outgoing primary nav.
                                    if (old == oldPrimaryNav) {
                                        mOps.add(opNum, new android.support.v4.app.BackStackRecord.Op(OP_UNSET_PRIMARY_NAV, old));
                                        opNum++;
                                        oldPrimaryNav = null;
                                    }
                                    final android.support.v4.app.BackStackRecord.Op removeOp = new android.support.v4.app.BackStackRecord.Op(OP_REMOVE, old);
                                    removeOp.enterAnim = op.enterAnim;
                                    removeOp.popEnterAnim = op.popEnterAnim;
                                    removeOp.exitAnim = op.exitAnim;
                                    removeOp.popExitAnim = op.popExitAnim;
                                    mOps.add(opNum, removeOp);
                                    added.remove(old);
                                    opNum++;
                                }
                            }
                        }
                        if (alreadyAdded) {
                            mOps.remove(opNum);
                            opNum--;
                        } else {
                            op.cmd = OP_ADD;
                            added.add(f);
                        }
                    }
                    break;
                    case OP_SET_PRIMARY_NAV: {
                        // It's ok if this is null, that means we will restore to no active
                        // primary navigation fragment on a pop.
                        mOps.add(opNum, new android.support.v4.app.BackStackRecord.Op(OP_UNSET_PRIMARY_NAV, oldPrimaryNav));
                        opNum++;
                        // Will be set by the OP_SET_PRIMARY_NAV we inserted before when run
                        oldPrimaryNav = op.fragment;
                    }
                    break;
                }
            }
            return oldPrimaryNav;
        }

        /**
         * Removes fragments that are added or removed during a pop operation.
         *
         * @param added Initialized to the fragments that are in the mManager.mAdded, this
         *              will be modified to contain the fragments that will be in mAdded
         *              after the execution ({@link #executeOps()}.
         * @param oldPrimaryNav The tracked primary navigation fragment as of the beginning of
         *                      this set of ops
         * @return the new oldPrimaryNav fragment after this record's ops would be popped
         */
        Fragment trackAddedFragmentsInPop(ArrayList<Fragment> added, Fragment oldPrimaryNav) {
            for (int opNum = 0; opNum < mOps.size(); opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                switch (op.cmd) {
                    case OP_ADD:
                    case OP_ATTACH:
                        added.remove(op.fragment);
                        break;
                    case OP_REMOVE:
                    case OP_DETACH:
                        added.add(op.fragment);
                        break;
                    case OP_UNSET_PRIMARY_NAV:
                        oldPrimaryNav = op.fragment;
                        break;
                    case OP_SET_PRIMARY_NAV:
                        oldPrimaryNav = null;
                        break;
                }
            }
            return oldPrimaryNav;
        }

        boolean isPostponed() {
            for (int opNum = 0; opNum < mOps.size(); opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                if (isFragmentPostponed(op)) {
                    return true;
                }
            }
            return false;
        }

        void setOnStartPostponedListener(Fragment.OnStartEnterTransitionListener listener) {
            for (int opNum = 0; opNum < mOps.size(); opNum++) {
                final android.support.v4.app.BackStackRecord.Op op = mOps.get(opNum);
                if (isFragmentPostponed(op)) {
                    op.fragment.setOnStartEnterTransitionListener(listener);
                }
            }
        }

        private static boolean isFragmentPostponed(android.support.v4.app.BackStackRecord.Op op) {
            final Fragment fragment = op.fragment;
            return fragment != null && fragment.mAdded && fragment.mView != null && !fragment.mDetached
                    && !fragment.mHidden && fragment.isPostponed();
        }

        @Override
        public String getName() {
            return mName;
        }

        public int getTransition() {
            return mTransition;
        }

        public int getTransitionStyle() {
            return mTransitionStyle;
        }

        @Override
        public boolean isEmpty() {
            return mOps.isEmpty();
        }
    }
}
