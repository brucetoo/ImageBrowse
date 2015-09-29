package com.brucetoo.activityanimation.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.Scroller;

public class PhotoView extends ImageView {

    private final static int ANIM_DURING = 300;
    private final static float MAX_SCALE = 2.5f;
    private int MAX_FLING_OVER_SCROLL = 0;
    private int MAX_OVER_RESISTANCE = 0;
    private int MAX_ANIM_FROM_WAITE = 500;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mAnimMatrix = new Matrix();
    private Matrix mSynthesisMatrix = new Matrix();
    private Matrix mTmpMatrix = new Matrix();

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;
    private OnClickListener mClickListener;

    private ScaleType mScaleType;

    private boolean hasMultiTouch;
    private boolean hasDrawable;
    private boolean isKnowSize;
    private boolean hasOverTranslate;
    private boolean isEnable = false;
    private boolean isInit;
    private boolean mAdjustViewBounds;

    private boolean imgLargeWidth;
    private boolean imgLargeHeight;

    private float mScale = 1.0f;
    private int mTranslateX;
    private int mTranslateY;

    private RectF mWidgetRect = new RectF();
    private RectF mBaseRect = new RectF();
    private RectF mImgRect = new RectF();
    private RectF mTmpRect = new RectF();
    private RectF mCommonRect = new RectF();

    private PointF mScreenCenter = new PointF();
    private PointF mDoubleTab = new PointF();

    private Transform mTranslate = new Transform();

    private RectF mClip;
    private ImageInfo mImageInfo;
    private long mInfoTime;
    private Runnable mCompleteCallBack;

    private float[] mValues = new float[16];

    public PhotoView(Context context) {
        super(context);
        init();
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        super.setScaleType(ScaleType.MATRIX);
        if (mScaleType == null) mScaleType = ScaleType.CENTER_INSIDE;
        mDetector = new GestureDetector(getContext(), mGestureListener);
        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        float density = getResources().getDisplayMetrics().density;
        MAX_FLING_OVER_SCROLL = (int) (density * 30);
        MAX_OVER_RESISTANCE = (int) (density * 140);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        ScaleType old = mScaleType;
        mScaleType = scaleType;

        if (old != scaleType)
            initBase();
    }

    public void touchEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    /**
     * GIF图片支持的最大等待时间
     */
    public void setMaxAnimFromWaitTime(int wait) {
        MAX_ANIM_FROM_WAITE = wait;
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = null;
        try {
            drawable = getResources().getDrawable(resId);
        } catch (Exception e) {}

        setImageDrawable(drawable);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawable == null) {
            hasDrawable = false;
            return;
        }

        if (!hasSize(drawable))
            return;

        if (!hasDrawable) {
            hasDrawable = true;
        }

        initBase();
    }

    private boolean hasSize(Drawable d) {
        if ((d.getIntrinsicHeight() <= 0 || d.getIntrinsicWidth() <= 0)
                && (d.getMinimumWidth() <= 0 || d.getMinimumHeight() <= 0)
                && (d.getBounds().width() <= 0 || d.getBounds().height() <= 0)) {
            return false;
        }
        return true;
    }

    private int getDrawableWidth(Drawable d) {
        int width = d.getIntrinsicWidth();
        if (width <= 0) width = d.getMinimumWidth();
        if (width <= 0) width = d.getBounds().width();
        return width;
    }

    private int getDrawableHeight(Drawable d) {
        int height = d.getIntrinsicHeight();
        if (height <= 0) height = d.getMinimumHeight();
        if (height <= 0) height = d.getBounds().height();
        return height;
    }

    private void initBase() {
        if (!hasDrawable) return;
        if (!isKnowSize) return;

        mBaseMatrix.reset();
        mAnimMatrix.reset();

        Drawable img = getDrawable();

        int w = getWidth();
        int h = getHeight();
        int imgw = getDrawableWidth(img);
        int imgh = getDrawableHeight(img);

        mBaseRect.set(0, 0, imgw, imgh);

        // 以图片中心点居中位移
        int tx = (w - imgw) / 2;
        int ty = (h - imgh) / 2;

        float sx = 1;
        float sy = 1;

        // 缩放，默认不超过屏幕大小
        if (imgw > w) {
            sx = (float) w / imgw;
        }

        if (imgh > h) {
            sy = (float) h / imgh;
        }

        float scale = sx < sy ? sx : sy;

        mBaseMatrix.reset();
        mBaseMatrix.postTranslate(tx, ty);
        mBaseMatrix.postScale(scale, scale, mScreenCenter.x, mScreenCenter.y);
        mBaseMatrix.mapRect(mBaseRect);

        mDoubleTab.set(mScreenCenter);

        executeTranslate();

        switch (mScaleType) {
            case CENTER:
                initCenter();
                break;
            case CENTER_CROP:
                initCenterCrop();
                break;
            case CENTER_INSIDE:
                initCenterInside();
                break;
            case FIT_CENTER:
                initFitCenter();
                break;
            case FIT_START:
                initFitStart();
                break;
            case FIT_END:
                initFitEnd();
                break;
            case FIT_XY:
                initFitXY();
                break;
        }

        isInit = true;

        if (mImageInfo != null && System.currentTimeMillis() - mInfoTime < MAX_ANIM_FROM_WAITE) {
            animateFrom(mImageInfo);
        }
        
        mImageInfo = null;
    }

    private void initCenter() {
        if (!hasDrawable) return;
        if (!isKnowSize) return;

        Drawable img = getDrawable();

        int imgw = img.getIntrinsicWidth();
        int imgh = img.getIntrinsicHeight();

        if (imgw > mWidgetRect.width() || imgh > mWidgetRect.height()) {
            float scaleX = imgw / mImgRect.width();
            float scaleY = imgh / mImgRect.height();

            mScale = scaleX > scaleY ? scaleX : scaleY;

            mAnimMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y);

            executeTranslate();
        }
    }

    private void initCenterCrop() {
        if (mImgRect.width() < mWidgetRect.width() || mImgRect.height() < mWidgetRect.height()) {
            float scaleX = mWidgetRect.width() / mImgRect.width();
            float scaleY = mWidgetRect.height() / mImgRect.height();

            mScale = scaleX > scaleY ? scaleX : scaleY;

            mAnimMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y);

            executeTranslate();
        }
    }

    private void initCenterInside() {
        if (mImgRect.width() > mWidgetRect.width() || mImgRect.height() > mWidgetRect.height()) {
            float scaleX = mWidgetRect.width() / mImgRect.width();
            float scaleY = mWidgetRect.height() / mImgRect.height();

            mScale = scaleX < scaleY ? scaleX : scaleY;

            mAnimMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y);

            executeTranslate();
        }
    }

    private void initFitCenter() {
        if (mImgRect.width() < mWidgetRect.width()) {
            mScale = mWidgetRect.width() / mImgRect.width();

            mAnimMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y);

            executeTranslate();
        }
    }

    private void initFitStart() {
        initFitCenter();

        float ty = -mImgRect.top;
        mTranslateY += ty;
        mAnimMatrix.postTranslate(0, ty);
        executeTranslate();
    }

    private void initFitEnd() {
        initFitCenter();

        float ty = (mWidgetRect.bottom - mImgRect.bottom);
        mTranslateY += ty;
        mAnimMatrix.postTranslate(0, ty);
        executeTranslate();
    }

    private void initFitXY() {
        float scaleX = mWidgetRect.width() / mImgRect.width();
        float scaleY = mWidgetRect.height() / mImgRect.height();

        mAnimMatrix.postScale(scaleX, scaleY, mScreenCenter.x, mScreenCenter.y);

        executeTranslate();
    }

    private void executeTranslate() {
        mSynthesisMatrix.set(mBaseMatrix);
        mSynthesisMatrix.postConcat(mAnimMatrix);
        setImageMatrix(mSynthesisMatrix);

        mAnimMatrix.mapRect(mImgRect, mBaseRect);

        imgLargeWidth = mImgRect.width() > mWidgetRect.width();
        imgLargeHeight = mImgRect.height() > mWidgetRect.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!hasDrawable) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        Drawable d = getDrawable();
        int drawableW = getDrawableWidth(d);
        int drawableH = getDrawableHeight(d);

        int pWidth = MeasureSpec.getSize(widthMeasureSpec);
        int pHeight = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;

        ViewGroup.LayoutParams p = getLayoutParams();

        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (p.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                width = drawableW;
            } else {
                width = pWidth;
            }
        } else {
            if (widthMode == MeasureSpec.EXACTLY) {
                width = pWidth;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = drawableW > pWidth ? pWidth : drawableW;
            } else {
                width = drawableW;
            }
        }

        if (p.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                height = drawableH;
            } else {
                height = pHeight;
            }
        } else {
            if (heightMode == MeasureSpec.EXACTLY) {
                height = pHeight;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = drawableH > pHeight ? pHeight : drawableH;
            } else {
                height = drawableH;
            }
        }

        if (mAdjustViewBounds && (float) drawableW / drawableH != (float) width / height) {

            float hScale = (float) height / drawableH;
            float wScale = (float) width / drawableW;

            float scale = hScale < wScale ? hScale : wScale;
            width = p.width == ViewGroup.LayoutParams.MATCH_PARENT ? width : (int) (drawableW * scale);
            height = p.height == ViewGroup.LayoutParams.MATCH_PARENT ? height : (int) (drawableH * scale);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        super.setAdjustViewBounds(adjustViewBounds);
        mAdjustViewBounds = adjustViewBounds;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidgetRect.set(0, 0, w, h);
        mScreenCenter.set(w / 2, h / 2);

        if (!isKnowSize) {
            isKnowSize = true;
            initBase();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mClip != null) {
            canvas.clipRect(mClip);
            mClip = null;
        }
        super.draw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isEnable) {
            mDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
            final int Action = event.getAction();
            if (Action == MotionEvent.ACTION_UP || Action == MotionEvent.ACTION_CANCEL) onUp(event);
        } else {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    private void onUp(MotionEvent ev) {
        if (mTranslate.isRuning) return;

        float scale = mScale;

        if (mScale < 1) {
            scale = 1;
            mTranslate.withScale(mScale, 1);
        } else if (mScale > MAX_SCALE) {
            scale = MAX_SCALE;
            mTranslate.withScale(mScale, MAX_SCALE);
        }

        mAnimMatrix.getValues(mValues);
        float s = mValues[Matrix.MSCALE_X];
        float tx = mValues[Matrix.MTRANS_X];
        float ty = mValues[Matrix.MTRANS_Y];

        float cpx = tx - mTranslateX;
        float cpy = ty - mTranslateY;

        mDoubleTab.x = -cpx / (s - 1);
        mDoubleTab.y = -cpy / (s - 1);

        mTmpRect.set(mImgRect);

        if (scale != mScale) {
            mTmpMatrix.setScale(scale, scale, mDoubleTab.x, mDoubleTab.y);
            mTmpMatrix.postTranslate(mTranslateX, mTranslateY);
            mTmpMatrix.mapRect(mTmpRect, mBaseRect);
        }

        doTranslateReset(mTmpRect);
        mTranslate.start();
    }

    private void doTranslateReset(RectF imgRect) {
        int tx = 0;
        int ty = 0;

        if (imgRect.width() < mWidgetRect.width()) {
            if (!isImageCenterWidth())
                tx = -(int) ((mWidgetRect.width() - imgRect.width()) / 2 - imgRect.left);
        } else {
            if (imgRect.left > mWidgetRect.left) {
                tx = (int) (imgRect.left - mWidgetRect.left);
            } else if (imgRect.right < mWidgetRect.right) {
                tx = (int) (imgRect.right - mWidgetRect.right);
            }
        }

        if (imgRect.height() < mWidgetRect.height()) {
            if (!isImageCenterHeight())
                ty = -(int) ((mWidgetRect.height() - imgRect.height()) / 2 - imgRect.top);
        } else {
            if (imgRect.top > mWidgetRect.top) {
                ty = (int) (imgRect.top - mWidgetRect.top);
            } else if (imgRect.bottom < mWidgetRect.bottom) {
                ty = (int) (imgRect.bottom - mWidgetRect.bottom);
            }
        }

        if (tx != 0 || ty != 0) {
            if (!mTranslate.mFlingScroller.isFinished()) mTranslate.mFlingScroller.abortAnimation();
            mTranslate.withTranslate(mTranslateX, mTranslateY, -tx, -ty);
        }
    }

    private boolean isImageCenterHeight() {
        return Math.round(mImgRect.top) == (mWidgetRect.height() - mImgRect.height()) / 2;
    }

    private boolean isImageCenterWidth() {
        return Math.round(mImgRect.left) == (mWidgetRect.width() - mImgRect.width()) / 2;
    }

    private ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return false;

            mScale *= scaleFactor;
            mDoubleTab.set(detector.getFocusX(), detector.getFocusY());
            mAnimMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            executeTranslate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            hasMultiTouch = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };

    private float resistanceScrollByX(float overScroll, float detalX) {
        float s = detalX * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / (float) MAX_OVER_RESISTANCE);
        return s;
    }

    private float resistanceScrollByY(float overScroll, float detalY) {
        float s = detalY * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / (float) MAX_OVER_RESISTANCE);
        return s;
    }

    /**
     * 匹配两个Rect的共同部分输出到out，若无共同部分则输出0，0，0，0
     */
    private void mapRect(RectF r1, RectF r2, RectF out) {

        float l, r, t, b;

        l = r1.left > r2.left ? r1.left : r2.left;
        r = r1.right < r2.right ? r1.right : r2.right;

        if (l > r) {
            out.set(0, 0, 0, 0);
            return;
        }

        t = r1.top > r2.top ? r1.top : r2.top;
        b = r1.bottom < r2.bottom ? r1.bottom : r2.bottom;

        if (t > b) {
            out.set(0, 0, 0, 0);
            return;
        }

        out.set(l, t, r, b);
    }

    private void checkRect() {
        if (!hasOverTranslate) {
            mapRect(mWidgetRect, mImgRect, mCommonRect);
        }
    }

    private Runnable mClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mClickListener != null) {
                mClickListener.onClick(PhotoView.this);
            }
        }
    };

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            hasOverTranslate = false;
            hasMultiTouch = false;
            removeCallbacks(mClickRunnable);
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (hasMultiTouch) return false;
            if (!imgLargeWidth && !imgLargeHeight) return false;
            if (!mTranslate.mFlingScroller.isFinished()) return false;

            float vx = velocityX;
            float vy = velocityY;

            if (Math.round(mImgRect.left) >= mWidgetRect.left || Math.round(mImgRect.right) <= mWidgetRect.right) {
                vx = 0;
            }

            if (Math.round(mImgRect.top) >= mWidgetRect.top || Math.round(mImgRect.bottom) <= mWidgetRect.bottom) {
                vy = 0;
            }

            doTranslateReset(mImgRect);
            mTranslate.withFling(vx, vy);
            mTranslate.start();

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mTranslate.isRuning) {
                mTranslate.stop();
            }

            if (canScrollHorizontallySelf(distanceX)) {
                if (distanceX < 0 && mImgRect.left - distanceX > mWidgetRect.left)
                    distanceX = mImgRect.left;
                if (distanceX > 0 && mImgRect.right - distanceX < mWidgetRect.right)
                    distanceX = mImgRect.right - mWidgetRect.right;

                mAnimMatrix.postTranslate(-distanceX, 0);
                mTranslateX -= distanceX;
            } else if (imgLargeWidth || hasMultiTouch || hasOverTranslate) {
                checkRect();
                if (!hasMultiTouch) {
                    if (distanceX < 0 && mImgRect.left - distanceX > mCommonRect.left)
                        distanceX = resistanceScrollByX(mImgRect.left - mCommonRect.left, distanceX);
                    if (distanceX > 0 && mImgRect.right - distanceX < mCommonRect.right)
                        distanceX = resistanceScrollByX(mImgRect.right - mCommonRect.right, distanceX);
                }

                mTranslateX -= distanceX;
                mAnimMatrix.postTranslate(-distanceX, 0);
                hasOverTranslate = true;
            }

            if (canScrollVerticallySelf(distanceY)) {
                if (distanceY < 0 && mImgRect.top - distanceY > mWidgetRect.top)
                    distanceY = mImgRect.top;
                if (distanceY > 0 && mImgRect.bottom - distanceY < mWidgetRect.bottom)
                    distanceY = mImgRect.bottom - mWidgetRect.bottom;

                mAnimMatrix.postTranslate(0, -distanceY);
                mTranslateY -= distanceY;
            } else if (imgLargeHeight || hasOverTranslate || hasMultiTouch) {
                checkRect();
                if (!hasMultiTouch) {
                    if (distanceY < 0 && mImgRect.top - distanceY > mCommonRect.top)
                        distanceY = resistanceScrollByY(mImgRect.top - mCommonRect.top, distanceY);
                    if (distanceY > 0 && mImgRect.bottom - distanceY < mCommonRect.bottom)
                        distanceY = resistanceScrollByY(mImgRect.bottom - mCommonRect.bottom, distanceY);
                }

                mAnimMatrix.postTranslate(0, -distanceY);
                mTranslateY -= distanceY;
                hasOverTranslate = true;
            }

            executeTranslate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            postDelayed(mClickRunnable, 250);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            mTranslate.stop();

            float from = 1;
            float to = 1;

            if (mScale == 1) {
                from = 1;
                to = MAX_SCALE;

                if (mImgRect.width() < mWidgetRect.width())
                    mDoubleTab.set(mScreenCenter.x, mScreenCenter.y);
                else
                    mDoubleTab.set(e.getX(), mScreenCenter.y);
            } else {
                from = mScale;
                to = 1;

                mTranslate.withTranslate(mTranslateX, mTranslateY, -mTranslateX, -mTranslateY);
            }

            mTranslate.withScale(from, to);
            mTranslate.start();

            return false;
        }
    };

    public boolean canScrollHorizontallySelf(float direction) {
        if (mImgRect.width() <= mWidgetRect.width()) return false;
        if (direction < 0 && Math.round(mImgRect.left) - direction >= mWidgetRect.left)
            return false;
        if (direction > 0 && Math.round(mImgRect.right) - direction <= mWidgetRect.right)
            return false;
        return true;
    }

    public boolean canScrollVerticallySelf(float direction) {
        if (mImgRect.height() <= mWidgetRect.height()) return false;
        if (direction < 0 && Math.round(mImgRect.top) - direction >= mWidgetRect.top) return false;
        if (direction > 0 && Math.round(mImgRect.bottom) - direction <= mWidgetRect.bottom)
            return false;
        return true;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (hasMultiTouch) return true;
        return canScrollHorizontallySelf(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (hasMultiTouch) return true;
        return canScrollVerticallySelf(direction);
    }

    private class Transform implements Runnable {

        boolean isRuning;

        OverScroller mTranslateScroller;
        OverScroller mFlingScroller;
        Scroller mScaleScroller;
        Scroller mClipScroll;

        ClipCalculate C;

        int mLastFlingX;
        int mLastFlingY;

        int mLastTranslateX;
        int mLastTranslateY;

        RectF mClipRect = new RectF();

        Transform() {
            Context ctx = getContext();
            DecelerateInterpolator i = new DecelerateInterpolator();
            mTranslateScroller = new OverScroller(ctx, i);
            mScaleScroller = new Scroller(ctx, i);
            mFlingScroller = new OverScroller(ctx, i);
            mClipScroll = new Scroller(ctx, i);
        }

        void withTranslate(int startX, int startY, int deltaX, int deltaY) {
            mLastTranslateX = 0;
            mLastTranslateY = 0;
            mTranslateScroller.startScroll(0, 0, deltaX, deltaY, ANIM_DURING);
        }

        void withScale(float form, float to) {
            mScaleScroller.startScroll((int) (form * 10000), 0, (int) ((to - form) * 10000), 0, ANIM_DURING);
        }

        void withClip(float fromX, float fromY, float deltaX, float deltaY, int d, ClipCalculate c) {
            mClipScroll.startScroll((int) (fromX * 10000), (int) (fromY * 10000), (int) (deltaX * 10000), (int) (deltaY * 10000), d);
            C = c;
        }

        void withFling(float velocityX, float velocityY) {
            mLastFlingX = velocityX < 0 ? Integer.MAX_VALUE : 0;
            int distanceX = (int) (velocityX > 0 ? Math.abs(mImgRect.left) : mImgRect.right - mWidgetRect.right);
            distanceX = velocityX < 0 ? Integer.MAX_VALUE - distanceX : distanceX;
            int minX = velocityX < 0 ? distanceX : 0;
            int maxX = velocityX < 0 ? Integer.MAX_VALUE : distanceX;
            int overX = velocityX < 0 ? Integer.MAX_VALUE - minX : distanceX;

            mLastFlingY = velocityY < 0 ? Integer.MAX_VALUE : 0;
            int distanceY = (int) (velocityY > 0 ? Math.abs(mImgRect.top) : mImgRect.bottom - mWidgetRect.bottom);
            distanceY = velocityY < 0 ? Integer.MAX_VALUE - distanceY : distanceY;
            int minY = velocityY < 0 ? distanceY : 0;
            int maxY = velocityY < 0 ? Integer.MAX_VALUE : distanceY;
            int overY = velocityY < 0 ? Integer.MAX_VALUE - minY : distanceY;

            if (velocityX == 0) {
                maxX = 0;
                minX = 0;
            }

            if (velocityY == 0) {
                maxY = 0;
                minY = 0;
            }

            mFlingScroller.fling(mLastFlingX, mLastFlingY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY, Math.abs(overX) < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL, Math.abs(overY) < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL);
        }

        void start() {
            isRuning = true;
            post(this);
        }

        void stop() {
            removeCallbacks(this);
            mTranslateScroller.abortAnimation();
            mScaleScroller.abortAnimation();
            mFlingScroller.abortAnimation();

            isRuning = false;
        }

        @Override
        public void run() {

            if (!isRuning) return;

            boolean endAnima = true;

            if (mScaleScroller.computeScrollOffset()) {
                mScale = mScaleScroller.getCurrX() / 10000f;
                endAnima = false;
            }

            if (mTranslateScroller.computeScrollOffset()) {
                int tx = mTranslateScroller.getCurrX() - mLastTranslateX;
                int ty = mTranslateScroller.getCurrY() - mLastTranslateY;
                mTranslateX += tx;
                mTranslateY += ty;
                mLastTranslateX = mTranslateScroller.getCurrX();
                mLastTranslateY = mTranslateScroller.getCurrY();
                endAnima = false;
            }

            if (mFlingScroller.computeScrollOffset()) {
                int x = mFlingScroller.getCurrX() - mLastFlingX;
                int y = mFlingScroller.getCurrY() - mLastFlingY;

                mLastFlingX = mFlingScroller.getCurrX();
                mLastFlingY = mFlingScroller.getCurrY();

                mTranslateX += x;
                mTranslateY += y;
                endAnima = false;
            }

            if (mClipScroll.computeScrollOffset() || mClip != null) {
                float sx = mClipScroll.getCurrX() / 10000f;
                float sy = mClipScroll.getCurrY() / 10000f;
                mTmpMatrix.setScale(sx, sy, (mImgRect.left + mImgRect.right) / 2, C.calculateTop());
                mTmpMatrix.mapRect(mClipRect, mImgRect);

                if (sx == 1) {
                    mClipRect.left = mWidgetRect.left;
                    mClipRect.right = mWidgetRect.right;
                }

                if (sy == 1) {
                    mClipRect.top = mWidgetRect.top;
                    mClipRect.bottom = mWidgetRect.bottom;
                }

                mClip = mClipRect;
            }

            if (!endAnima) {
                mAnimMatrix.reset();
                mAnimMatrix.postScale(mScale, mScale, mDoubleTab.x, mDoubleTab.y);
                mAnimMatrix.postTranslate(mTranslateX, mTranslateY);
                executeTranslate();
                post(this);
            } else {
                isRuning = false;
                invalidate();

                if (mCompleteCallBack != null) {
                    mCompleteCallBack.run();
                    mCompleteCallBack = null;
                }
            }
        }
    }

    public ImageInfo getInfo() {
        RectF rect = new RectF();
        RectF local = new RectF();
        int[] p = new int[2];
        getLocationInWindow(p);
        rect.set(p[0] + mImgRect.left, p[1] + mImgRect.top, p[0] + mImgRect.right, p[1] + mImgRect.bottom);
        local.set(p[0], p[1], p[0] + mImgRect.width(), p[1] + mImgRect.height());
        return new ImageInfo(rect, local, mImgRect, mWidgetRect, mScale, mScaleType);
    }

    private void reset() {
        mAnimMatrix.reset();
        executeTranslate();
        mScale = 1;
        mTranslateX = 0;
        mTranslateY = 0;
    }

    public interface ClipCalculate {
        float calculateTop();
    }

    public class START implements ClipCalculate {
        public float calculateTop() {
            return mImgRect.top;
        }
    }

    public class END implements ClipCalculate {
        public float calculateTop() {
            return mImgRect.bottom;
        }
    }

    public class OTHER implements ClipCalculate {
        public float calculateTop() {
            return (mImgRect.top + mImgRect.bottom) / 2;
        }
    }

    /**
     * 若等待时间过长也没有给控件设置图片，则会忽略该动画(针对GIF)，若要再次播放动画则需要重新调用该方法
     * (等待的时间默认500毫秒，可以通过setMaxAnimFromWaiteTime(int)设置最大等待时间)
     */
    public void animateFrom(ImageInfo imageInfo) {
        if (isInit) {
            reset();

            ImageInfo mine = getInfo();

            float scaleX = imageInfo.mImgRect.width() / mine.mImgRect.width();
            float scaleY = imageInfo.mImgRect.height() / mine.mImgRect.height();

            float scale = scaleX < scaleY ? scaleX : scaleY;
            float tx = imageInfo.mRect.left - mine.mRect.left;
            float ty = imageInfo.mRect.top - mine.mRect.top;

            mAnimMatrix.reset();
            mAnimMatrix.postScale(scale, scale, mImgRect.left, mImgRect.top);
            mAnimMatrix.postTranslate(tx, ty);

            executeTranslate();

            mTranslateX += tx;
            mTranslateY += ty;

            mDoubleTab.x = mImgRect.left - tx;
            mDoubleTab.y = mImgRect.top - ty;

            mAnimMatrix.getValues(mValues);
            float s = mValues[Matrix.MSCALE_X];

            mTranslate.withScale(s, mScale);
            mTranslate.withTranslate(mTranslateX, mTranslateY, (int) -tx, (int) -ty);

            if (imageInfo.mWidgetRect.width() < imageInfo.mImgRect.width() || imageInfo.mWidgetRect.height() < imageInfo.mImgRect.height()) {
                float clipX = imageInfo.mWidgetRect.width() / imageInfo.mImgRect.width();
                float clipY = imageInfo.mWidgetRect.height() / imageInfo.mImgRect.height();
                clipX = clipX > 1 ? 1 : clipX;
                clipY = clipY > 1 ? 1 : clipY;

                ClipCalculate c = imageInfo.mScaleType == ScaleType.FIT_START ? new START() : imageInfo.mScaleType == ScaleType.FIT_END ? new END() : new OTHER();

                mTranslate.withClip(clipX, clipY, 1 - clipX, 1 - clipY, ANIM_DURING / 3, c);

                mTmpMatrix.setScale(clipX, clipY, (mImgRect.left + mImgRect.right) / 2, c.calculateTop());
                mTmpMatrix.mapRect(mTranslate.mClipRect, mImgRect);
                mClip = mTranslate.mClipRect;
            }

            mTranslate.start();
        } else {
            mImageInfo = imageInfo;
            mInfoTime = System.currentTimeMillis();
        }
    }

    /**
     * 向ImageInfo的持有者imageView执行动画
     * @param imageInfo imageView的位置信息
      * @param completeCallBack 执行动画完后回调方法 (默认300ms)
     */
    public void animateTo(ImageInfo imageInfo, Runnable completeCallBack) {
        if (isInit) {
            executeTranslate();
            ImageInfo mine = getInfo();

            mDoubleTab.x = 0;
            mDoubleTab.y = 0;

            mAnimMatrix.getValues(mValues);
            mScale = mValues[Matrix.MSCALE_X];
            mTranslateX = (int) mValues[Matrix.MTRANS_X];
            mTranslateY = (int) mValues[Matrix.MTRANS_Y];

            // 缩放
            float scaleX = imageInfo.mImgRect.width() / mine.mImgRect.width();
            float scaleY = imageInfo.mImgRect.height() / mine.mImgRect.height();
            float scale = scaleX > scaleY ? scaleX : scaleY;

            mTmpMatrix.set(mAnimMatrix);
            mTmpMatrix.postScale(scale, scale, mDoubleTab.x, mDoubleTab.y);
            mTmpMatrix.getValues(mValues);
            scale = mValues[Matrix.MSCALE_X];

            mBaseMatrix.getValues(mValues);

            int tx = (int) (imageInfo.mLocalRect.left - mine.mLocalRect.left + imageInfo.mImgRect.left - mValues[Matrix.MTRANS_X] * scale);
            int ty = (int) (imageInfo.mLocalRect.top - mine.mLocalRect.top + imageInfo.mImgRect.top - mValues[Matrix.MTRANS_Y] * scale);

            mTranslate.withScale(mScale, scale);
            mTranslate.withTranslate(mTranslateX, mTranslateY, -mTranslateX + tx, -mTranslateY + ty);

            if (imageInfo.mWidgetRect.width() < imageInfo.mRect.width() || imageInfo.mWidgetRect.height() < imageInfo.mRect.height()) {
                float clipX = imageInfo.mWidgetRect.width() / imageInfo.mRect.width();
                float clipY = imageInfo.mWidgetRect.height() / imageInfo.mRect.height();
                clipX = clipX > 1 ? 1 : clipX;
                clipY = clipY > 1 ? 1 : clipY;

                final float cx = clipX;
                final float cy = clipY;
                final ClipCalculate c = imageInfo.mScaleType == ScaleType.FIT_START ? new START() : imageInfo.mScaleType == ScaleType.FIT_END ? new END() : new OTHER();

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTranslate.withClip(1, 1, -1 + cx, -1 + cy, ANIM_DURING / 2, c);
                    }
                }, ANIM_DURING / 2);
            }

            mCompleteCallBack = completeCallBack;
            mTranslate.start();
        }
    }
}