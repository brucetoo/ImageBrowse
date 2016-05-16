package com.brucetoo.imagebrowse.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by Bruce Too
 * On 9/29/15.
 * At 20:57
 * ViewPager with rebound effect
 * like Chris banes's pulltorefreshview
 */
public class ReboundViewPager extends ReboundContainer<ViewPager> {

    public ReboundViewPager(Context context) {
        this(context, null);
    }

    public ReboundViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReboundViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean canOverscrollAtStart() {
        ViewPager viewPager = getOverscrollView();
        PagerAdapter adapter = viewPager.getAdapter();
        if (null != adapter) {
            if (viewPager.getCurrentItem() == 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    protected boolean canOverscrollAtEnd() {
        ViewPager viewPager = getOverscrollView();
        PagerAdapter adapter = viewPager.getAdapter();
        if (null != adapter && adapter.getCount() > 0) {
            if (viewPager.getCurrentItem() == adapter.getCount() - 1) {
                return true;
            }
            return false;
        }

        return false;
    }

    @Override
    protected ReboundContainer.OverscrollDirection getOverscrollDirection() {
        return ReboundContainer.OverscrollDirection.Horizontal;
    }

    @Override
    protected ViewPager createOverscrollView() {
        ViewPager viewPager = new ViewPager(getContext());
        return viewPager;
    }

}