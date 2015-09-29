package com.brucetoo.activityanimation.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
/**
 * Created by Bruce Too
 * On 9/29/15.
 * At 21:52
 * change for this,Detail see
 * https://github.com/chrisbanes/Android-PullToRefresh/tree/master/library/src/com/handmark/pulltorefresh/library
 */
 public abstract class ReboundContainer<T extends View> extends RelativeLayout {

    private T mOverscrollView = null;

    private boolean mIsBeingDragged = false;

    private float mMotionBeginX = 0;

    private float mMotionBeginY = 0;

    private int mTouchSlop;

    public enum OverscrollDirection {
        Horizontal, Vertical,
    }

    abstract protected boolean canOverscrollAtStart();

    abstract protected boolean canOverscrollAtEnd();

    abstract protected OverscrollDirection getOverscrollDirection();

    abstract protected T createOverscrollView();

    public ReboundContainer(Context context) {
        this(context, null);
    }

    public ReboundContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReboundContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mOverscrollView = createOverscrollView();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(mOverscrollView, layoutParams);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public T getOverscrollView() {
        return mOverscrollView;
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {

        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {

            mMotionBeginX = ev.getX();
            mMotionBeginY = ev.getY();
            mIsBeingDragged = false;

        } else if (action == MotionEvent.ACTION_MOVE) {

            if (mIsBeingDragged == false) {

                float scrollDirectionDiff = 0f;
                float anotherDirectionDiff = 0f;
                if (getOverscrollDirection() == OverscrollDirection.Horizontal) {

                    scrollDirectionDiff = ev.getX() - mMotionBeginX;
                    anotherDirectionDiff = ev.getY() - mMotionBeginY;

                } else if (getOverscrollDirection() == OverscrollDirection.Vertical) {

                    scrollDirectionDiff = ev.getY() - mMotionBeginY;
                    anotherDirectionDiff = ev.getX() - mMotionBeginX;

                }
                float absScrollDirectionDiff = Math.abs(scrollDirectionDiff);
                float absAnotherDirectionDiff = Math.abs(anotherDirectionDiff);
                if (absScrollDirectionDiff > mTouchSlop && absScrollDirectionDiff > absAnotherDirectionDiff) {
                    if (canOverscrollAtStart() && scrollDirectionDiff > 0f) {

                        mIsBeingDragged = true;

                    } else if (canOverscrollAtEnd() && scrollDirectionDiff < 0f) {

                        mIsBeingDragged = true;

                    }
                }

            }

        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {

        int action = event.getAction();

        float moveOffset = 0;
        if (getOverscrollDirection() == OverscrollDirection.Horizontal) {

            moveOffset = event.getX() - mMotionBeginX;

        } else if (getOverscrollDirection() == OverscrollDirection.Vertical) {

            moveOffset = event.getY() - mMotionBeginY;

        }

        moveOffset *= 0.5f;

        if (action == MotionEvent.ACTION_MOVE) {

            if (getOverscrollDirection() == OverscrollDirection.Horizontal) {
                moveOverscrollView(moveOffset, 0);
            } else if (getOverscrollDirection() == OverscrollDirection.Vertical) {
                moveOverscrollView(0, moveOffset);
            }



        } else if (action == MotionEvent.ACTION_UP) {

            resetOverscrollViewWithAnimation(moveOffset, event.getY());
            mIsBeingDragged = false;

        }

        return true;
    }

    private void moveOverscrollView(float currentX, float currentY) {
        if (getOverscrollDirection() == OverscrollDirection.Horizontal) {

            scrollTo(-(int) currentX, 0);

        } else if (getOverscrollDirection() == OverscrollDirection.Vertical) {

            scrollTo(0, -(int) currentY);

        }
    }

    private void resetOverscrollViewWithAnimation(float currentX, float currentY) {
        Interpolator scrollAnimationInterpolator = new DecelerateInterpolator();
        SmoothScrollRunnable smoothScrollRunnable = new SmoothScrollRunnable((int) currentX, 0, 300, scrollAnimationInterpolator);
        post(smoothScrollRunnable);
    }

    final class SmoothScrollRunnable implements Runnable {
        private final Interpolator mInterpolator;
        private final int mScrollToPosition;
        private final int mScrollFromPosition;
        private final long mDuration;

        private boolean mContinueRunning = true;
        private long mStartTime = -1;
        private int mCurrentPosition = -1;

        public SmoothScrollRunnable(int fromPosition, int toPosition, long duration, Interpolator scrollAnimationInterpolator) {
            mScrollFromPosition = fromPosition;
            mScrollToPosition = toPosition;
            mInterpolator = scrollAnimationInterpolator;
            mDuration = duration;
        }

        @Override
        public void run() {

            /**
             * Only set mStartTime if this is the first time we're starting,
             * else actually calculate the Y delta
             */
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime)) / mDuration;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((mScrollFromPosition - mScrollToPosition)
                        * mInterpolator.getInterpolation(normalizedTime / 1000f));
                mCurrentPosition = mScrollFromPosition - deltaY;

                if (getOverscrollDirection() == OverscrollDirection.Horizontal) {
                    moveOverscrollView(mCurrentPosition, 0);
                } else if (getOverscrollDirection() == OverscrollDirection.Vertical) {
                    moveOverscrollView(0, mCurrentPosition);
                }

            }

            if (mContinueRunning && mScrollToPosition != mCurrentPosition) {

                ViewCompat.postOnAnimation(ReboundContainer.this, this);

            } else {

            }
        }

        public void stop() {
            mContinueRunning = false;
            removeCallbacks(this);
        }
    }

}
