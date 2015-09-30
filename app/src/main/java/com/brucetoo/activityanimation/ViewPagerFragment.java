package com.brucetoo.activityanimation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.brucetoo.activityanimation.widget.ReboundViewPager;
import com.brucetoo.activityanimation.widget.ImageInfo;
import com.brucetoo.activityanimation.widget.PhotoView;

import java.util.ArrayList;

/**
 * Created by Bruce Too
 * On 9/28/15.
 * At 19:37
 * ViewPagerFragment add into MainActivity
 */
public class ViewPagerFragment extends Fragment{

    private ReboundViewPager viewPager;
    private TextView  tips; //viewpager indicator
    private ArrayList<Integer> imgs;
    private ImageInfo imageInfo;
    private View mask;//background view
    private ArrayList<ImageInfo> imageInfos;
    private int position;

    public static ViewPagerFragment getInstance(Bundle imgs){
        ViewPagerFragment fragment = new ViewPagerFragment();
        fragment.setArguments(imgs);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewpager,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (ReboundViewPager) view.findViewById(R.id.viewpager);
        tips = (TextView) view.findViewById(R.id.text);
        mask = view.findViewById(R.id.mask);

        runEnterAnimation();
        Bundle bundle = getArguments();
        imgs = bundle.getIntegerArrayList("imgs");
        imageInfo = bundle.getParcelable("info");
        imageInfos = bundle.getParcelableArrayList("infos");

        position = bundle.getInt("position", 0);
        tips.setText((position + 1) + "/" + imgs.size());

        viewPager.getOverscrollView().setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imgs.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int pos) {
                PhotoView view = new PhotoView(getActivity());
                view.touchEnable(true);
                view.setImageResource(imgs.get(pos));
                if(position == pos){//only animate when position equals u click in pre layout
                    view.animateFrom(imageInfo);
                }
                //force to get focal point,to listen key listener
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                view.setOnKeyListener(pressKeyListener);//add key listener to listen back press
                view.setOnClickListener(onClickListener);
                view.setTag(pos);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        viewPager.getOverscrollView().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tips.setText((position + 1) + "/" + imgs.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //set current position
        viewPager.getOverscrollView().setCurrentItem(position);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            exitFragment(v);
        }
    };

    private void exitFragment(View v) {
        //退出时点击的位置
        int position = (int) v.getTag();
        //回到上个界面该view的位置
        runExitAnimation(v);
        ((PhotoView)v).animateTo(imageInfos.get(position), new Runnable() {
            @Override
            public void run() {
                if (!ViewPagerFragment.this.isResumed()) {//fragment被回收
                    return;
                }
                final FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    fragmentManager.popBackStack();//回退栈
                }
            }
        });
    }


    private View.OnKeyListener pressKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {//只监听返回键
                if (event.getAction() != KeyEvent.ACTION_UP) {
                    return true;
                }
                exitFragment(v);
                return true;
            }
            return false;
        }
    };


    private void runEnterAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(300);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        mask.startAnimation(alphaAnimation);
    }

    public void runExitAnimation(final View view) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        alphaAnimation.setDuration(300);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mask.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mask.startAnimation(alphaAnimation);
    }

}
