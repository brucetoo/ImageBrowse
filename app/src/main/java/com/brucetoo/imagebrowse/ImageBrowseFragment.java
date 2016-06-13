package com.brucetoo.imagebrowse;

import android.graphics.Bitmap;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.brucetoo.imagebrowse.widget.ImageInfo;
import com.brucetoo.imagebrowse.widget.MaterialProgressBar;
import com.brucetoo.imagebrowse.widget.PhotoView;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by Bruce Too
 * On 9/28/15.
 * At 19:37
 * ImageBrowseFragment add into MainActivity
 */
public class ImageBrowseFragment extends Fragment {

    private ViewPager viewPager;
    private TextView tips; //viewpager indicator
    private ArrayList<String> imageUrls;
    private ImageInfo imageInfo;
    private View mask;//background view
    private ArrayList<ImageInfo> imageInfos;
    private int position;

    public static ImageBrowseFragment newInstance(Bundle imgs) {
        ImageBrowseFragment fragment = new ImageBrowseFragment();
        fragment.setArguments(imgs);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewpager, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tips = (TextView) view.findViewById(R.id.text);
        mask = view.findViewById(R.id.mask);

        runEnterAnimation();
        Bundle bundle = getArguments();
        imageUrls = bundle.getStringArrayList(ImageInfo.INTENT_IMAGE_URLS);
        imageInfo = bundle.getParcelable(ImageInfo.INTENT_CLICK_IMAGE_INFO);
        imageInfos = bundle.getParcelableArrayList(ImageInfo.INTENT_IMAGE_INFOS);
        position = bundle.getInt(ImageInfo.INTENT_CLICK_IMAGE_POSITION, 0);

        tips.setText((position + 1) + "/" + imageUrls.size());

        //NOTE: default viewPager has two page instance hold on.
        //if you like,just custom by viewPager.setOffscreenPageLimit();
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imageUrls.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int pos) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_view_detail, null, false);
                final PhotoView photoView = (PhotoView) view.findViewById(R.id.image_detail);
                final PhotoView thumbnailView = (PhotoView) view.findViewById(R.id.image_thumbnail);
                final MaterialProgressBar progressBar = (MaterialProgressBar) view.findViewById(R.id.progress);

                String imgUrl = imageUrls.get(pos);
                final String thumbnailImageUrl = Utils.getThumbnailImageUrl(getActivity(), imgUrl, 0, 0);

                if(Utils.isImageCacheAvailable(imgUrl)){//full image cache is available
                    if(pos == position){//only animation in where you click
                        photoView.animateFrom(imageInfo);
                    }
                    progressBar.setVisibility(View.GONE);
                    thumbnailView.setVisibility(View.GONE);
                    Utils.displayImageWithCache(imgUrl,photoView,null);
                }else {

                    if(Utils.isImageCacheAvailable(thumbnailImageUrl)){//if we had thumbnail image cache available
                        thumbnailView.setVisibility(View.VISIBLE);
                        thumbnailView.animateFrom(imageInfo);//animate from pre layout photoView
                    }

                    Utils.displayImageWithCache(thumbnailImageUrl,thumbnailView,null);

                    Utils.displayImageWithCache(imgUrl,photoView,new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    progressBar.setVisibility(View.GONE);
                                    thumbnailView.setVisibility(View.GONE);
                                    //once we load full image,we need animate from thumbnail view
                                    photoView.animateFrom(thumbnailView.getInfo());
                                }});
                }


                //force to get focal point,to listen key listener
                photoView.setFocusableInTouchMode(true);
                photoView.requestFocus();
                photoView.setOnKeyListener(pressKeyListener);//add key listener to listen back press
                photoView.setOnClickListener(onClickListener);
                photoView.setTag(pos);
                photoView.enable();

                container.addView(view);

//   Sample Use Example
//                PhotoView view = new PhotoView(getActivity());
//                view.touchEnable(true);
//                ImageLoader.newInstance().displayImage(imageUrls.get(pos), view,
//                        new DisplayImageOptions.Builder()
//                                .showImageOnLoading(android.R.color.darker_gray)
//                                .cacheInMemory(true).cacheOnDisk(true).build());
//                if(position == pos){//only animate when position equals u click in pre layout
//                    view.animateFrom(imageInfo);
//                }
//                //force to get focal point,to listen key listener
//                view.setFocusableInTouchMode(true);
//                view.requestFocus();
//                view.setOnKeyListener(pressKeyListener);//add key listener to listen back press
//                view.setOnClickListener(onClickListener);
//                view.setTag(pos);
//                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tips.setText((position + 1) + "/" + imageUrls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //set current position
        viewPager.setCurrentItem(position);
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
        if(((FrameLayout)v.getParent()).getChildAt(1).getVisibility() == View.VISIBLE){
            popFragment();
        }else {
            runExitAnimation(v);
            ((PhotoView) v).animateTo(imageInfos.get(position), new Runnable() {
                @Override
                public void run() {
                    popFragment();
                }
            });
        }
    }

    private void popFragment() {
        if (!ImageBrowseFragment.this.isResumed()) {
            return;
        }
        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
        }
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
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(PhotoView.ANIMATE_DURING);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        mask.startAnimation(alphaAnimation);
    }

    public void runExitAnimation(final View view) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(PhotoView.ANIMATE_DURING);
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
