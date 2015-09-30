/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brucetoo.activityanimation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.brucetoo.activityanimation.widget.ImageInfo;
import com.brucetoo.activityanimation.widget.PhotoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
/**
 * Created by Bruce Too
 * On 9/28/15.
 * At 10:16
 */
public class MainActivity extends FragmentActivity {

    private GridView gridView;
    private ArrayList<String> imgList = new ArrayList<>();
    private ArrayList<ImageInfo> imgImageInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        http://img6.cache.netease.com/3g/2015/9/30/20150930091938133ad.jpg
//        http://img2.cache.netease.com/3g/2015/9/30/2015093000515435aff.jpg
//        http://img5.cache.netease.com/3g/2015/9/30/20150930075225737e5.jpg
//        http://img5.cache.netease.com/3g/2015/9/29/20150929213007cd8cd.jpg
//        http://img3.cache.netease.com/3g/2015/9/29/20150929162747a8bfa.jpg
//        http://img2.cache.netease.com/3g/2015/9/30/20150930091208cf03c.jpg
        imgList.add(0, "http://img6.cache.netease.com/3g/2015/9/30/20150930091938133ad.jpg");
        imgList.add(1, "http://img2.cache.netease.com/3g/2015/9/30/2015093000515435aff.jpg");
        imgList.add(2, "http://img5.cache.netease.com/3g/2015/9/30/20150930075225737e5.jpg");
        imgList.add(3, "http://img5.cache.netease.com/3g/2015/9/29/20150929213007cd8cd.jpg");
        imgList.add(4, "http://img3.cache.netease.com/3g/2015/9/29/20150929162747a8bfa.jpg");
        imgList.add(5, "http://img2.cache.netease.com/3g/2015/9/30/20150930091208cf03c.jpg");
        imgList.add(6, "http://img2.cache.netease.com/3g/2015/9/30/2015093000515435aff.jpg");
        imgList.add(7, "http://img5.cache.netease.com/3g/2015/9/29/20150929213007cd8cd.jpg");
        imgList.add(8, "http://img3.cache.netease.com/3g/2015/9/29/20150929162747a8bfa.jpg");
        gridView = (GridView) findViewById(R.id.gridview);
        final ImageAdapter adapter = new ImageAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(view.isEnabled()) {
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("imgs", imgList);
                    bundle.putParcelable("info", ((PhotoView) view).getInfo());
                    bundle.putInt("position", position);
                    imgImageInfos.clear();
                    //NOTE:if imgList.size >= the visible count in single screen,i will cause NullPointException
                    //because item out of screen have been replaced/reused
                    for (int i = 0; i < imgList.size(); i++) {
                        imgImageInfos.add(((PhotoView) parent.getChildAt(i)).getInfo());
                    }
                    parent.getChildAt(position);
                    bundle.putParcelableArrayList("infos", imgImageInfos);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_viewpager, ViewPagerFragment.getInstance(bundle), "ViewPagerFragment")
                            .addToBackStack(null).commit();
                }

            }
        });
    }


    class ImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return imgList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            PhotoView p = new PhotoView(MainActivity.this);
            p.setLayoutParams(new AbsListView.LayoutParams((int) (getResources().getDisplayMetrics().density * 100), (int) (getResources().getDisplayMetrics().density * 100)));
            p.setScaleType(ImageView.ScaleType.CENTER_CROP);

            p.setEnabled(false);
            ImageLoader.getInstance().displayImage(imgList.get(i),p,
                    new DisplayImageOptions.Builder()
                            .showImageOnLoading(android.R.color.darker_gray)
                            .cacheInMemory(true).cacheOnDisk(true).build(),loadingListener);
            p.touchEnable(false);//disable touch
            return p;
        }
    }

    private ImageLoadingListener loadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            view.setEnabled(true);//only loadedImage is available we can click item
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //just for test to clean cache
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
    }
}
