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
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.brucetoo.activityanimation.widget.ImageInfo;
import com.brucetoo.activityanimation.widget.PhotoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
                    getSupportFragmentManager().beginTransaction().replace(Window.ID_ANDROID_CONTENT, ImageBrowseFragment.newInstance(bundle), "ViewPagerFragment")
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

            //get thumbnailurl to save user data...like WeChat does
            String thumbnailUrl = getThumbnailImageUrl(imgList.get(i),0,0);
            ImageLoader.getInstance().displayImage(thumbnailUrl, p,
                    new DisplayImageOptions.Builder()
                            .showImageOnLoading(android.R.color.darker_gray)
                            .cacheInMemory(true).cacheOnDisk(true).build(), loadingListener);
            p.disenable();//enable touch
            return p;
        }
    }

    private ImageLoadingListener loadingListener = new SimpleImageLoadingListener() {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            view.setEnabled(true);//only loadedImage is available we can click item
        }
    };

    /**
     * get a thumbnail image url from original url
     * @param imgUrl original image url
     * @param width  width u need
     * @param height height u need
     * @return the number(85) in below url indicate the quality of original image
     */
    public String getThumbnailImageUrl(String imgUrl,int width,int height){
        String url="http://imgsize.ph.126.net/?imgurl=data1_data2xdata3x0x85.jpg&enlarge=true";
        width = (int) (getResources().getDisplayMetrics().density * 100);
        height = (int) (getResources().getDisplayMetrics().density * 100); //just for convenient
        url=url.replaceAll("data1", imgUrl).replaceAll("data2", width+"").replaceAll("data3", height+"");
        return url;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //just for test to clean cache
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
    }
}
