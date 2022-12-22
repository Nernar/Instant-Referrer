package com.zhekasmirnov.horizon.activity.util;

public class DialogHelper {

    public static class DecisionStatus {
        boolean complete = false;
        boolean decision = false;
    }

    public interface ProgressInterface {
        boolean isTerminated();

        void onProgress(double d);
    }

    public static boolean awaitDecision(Object activity, int i, Object obj, int i2, int i3) {
        throw new UnsupportedOperationException();
    }

    public static boolean awaitDecision(int i, Object obj, int i2, int i3) {
        throw new UnsupportedOperationException();
    }

    public static class ProgressDialogHolder implements ProgressInterface {
        @SuppressWarnings("unused")
        private final Object builder;
        @SuppressWarnings("unused")
        private final int cancelWarnStr;
        @SuppressWarnings("unused")
        private final Object context;
        @SuppressWarnings("unused")
        private Object dialog;
        @SuppressWarnings("unused")
        private Object info;
        private boolean isOpened;
        @SuppressWarnings("unused")
        private boolean isPrepared;
        private boolean isTerminated;
        @SuppressWarnings("unused")
        private Object layout;
        @SuppressWarnings("unused")
        private Object message;
        @SuppressWarnings("unused")
        private Object progressBar;

        public void onComplete() {
        }

        public ProgressDialogHolder(int i, int i2) {
            this(null, i, i2);
        }

        public ProgressDialogHolder(Object activity, int i, int i2) {
            this.isPrepared = false;
            this.isOpened = false;
            this.isTerminated = false;
            this.context = activity;
            this.builder = new Object();
            this.cancelWarnStr = i2;
        }

        @SuppressWarnings("unused")
        private Object show() {
            throw new UnsupportedOperationException();
        }

        public void open() {
            this.isOpened = true;
        }

        public void close() {
            if (this.isOpened) {
                this.isOpened = false;
            }
        }

        public void setText(int i) {
            throw new UnsupportedOperationException();
        }

        public void setText(String str) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminated() {
            return this.isTerminated;
        }

        @Override
        public void onProgress(double d) {
            throw new UnsupportedOperationException();
        }

        public void onDownloadMessage(String str) {
            throw new UnsupportedOperationException();
        }
    }

    public static void showTipDialog(Object activity, Object obj, Object obj2) {
        throw new UnsupportedOperationException();
    }

    public static class EditStringDialog {
        public final Object context;
        @SuppressWarnings("unused")
        private Object dialog;
        @SuppressWarnings("unused")
        private Object editTextView;
        @SuppressWarnings("unused")
        private Object headingView;
        @SuppressWarnings("unused")
        private Object textView;
        @SuppressWarnings("unused")
        private Object titleView;
        @SuppressWarnings("unused")
        private Object valueNameView;
        @SuppressWarnings("unused")
        private ResultListener listener = null;
        @SuppressWarnings("unused")
        private String defaultValue = "";
        @SuppressWarnings("unused")
        private int dialogWidth = -1;
        @SuppressWarnings("unused")
        private int dialogHeight = -1;
        @SuppressWarnings("unused")
        private boolean isPrepared = false;
        private boolean isOpened = false;
        private boolean isResultAvailable = false;
        private String currentResult = null;
        @SuppressWarnings("unused")
        private final Object builder = new Object();

        public interface ResultListener {
            void onCancel();

            void onConfirm(String str);
        }

        public EditStringDialog(Object activity) {
            this.context = activity;
        }

        public EditStringDialog setLabels(int i, int i2) {
            return this;
        }

        public EditStringDialog setDescription(String str) {
            return this;
        }

        public EditStringDialog setDescription(int i) {
            return this;
        }

        public EditStringDialog setHeading(String str) {
            return this;
        }

        public EditStringDialog setDefaultValue(String str) {
            this.defaultValue = str;
            return this;
        }

        public EditStringDialog setSize(int i, int i2) {
            this.dialogWidth = i;
            this.dialogHeight = i2;
            return this;
        }

        @SuppressWarnings("unused")
        private Object show() {
            throw new UnsupportedOperationException();
        }

        public void open() {
            this.isOpened = true;
        }

        public void close() {
            if (this.isOpened) {
                this.isOpened = false;
            }
        }

        public EditStringDialog setListener(int i, ResultListener resultListener) {
            this.listener = resultListener;
            return this;
        }

        public String awaitResult() {
            while (!this.isResultAvailable) {
                Thread.yield();
            }
            return this.currentResult;
        }
    }

    public static void showTipDialog(int i, int i2) {
        showTipDialog(null, Integer.valueOf(i), Integer.valueOf(i2));
    }
}
