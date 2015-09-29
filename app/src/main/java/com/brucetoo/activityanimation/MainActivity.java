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

import java.util.ArrayList;
/**
 * Created by Bruce Too
 * On 9/28/15.
 * At 10:16
 */
public class MainActivity extends FragmentActivity {

    private GridView gridView;
    private ArrayList<Integer> imgList = new ArrayList<>();
    private ArrayList<ImageInfo> imgImageInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgList.add(0, R.drawable.p1);
        imgList.add(1, R.drawable.p2);
        imgList.add(2, R.drawable.p3);
        imgList.add(3, R.drawable.p4);
        imgList.add(4, R.drawable.p1);
        imgList.add(5, R.drawable.p2);
        imgList.add(6, R.drawable.p3);
        imgList.add(7, R.drawable.p4);
        imgList.add(8, R.drawable.p1);
        gridView = (GridView) findViewById(R.id.gridview);
        final ImageAdapter adapter = new ImageAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("imgs", imgList);
                bundle.putParcelable("info", ((PhotoView) view).getInfo());
                bundle.putInt("position", position);
                imgImageInfos.clear();
                //NOTE:if imgList.size >= the visible count in single screen,i will cause NullPointException
                //because item out of screen have been replaced/reused
                for(int i = 0; i < imgList.size(); i++){
                    imgImageInfos.add(((PhotoView)parent.getChildAt(i)).getInfo());
                }
                parent.getChildAt(position);
                bundle.putParcelableArrayList("infos", imgImageInfos);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_viewpager, ViewPagerFragment.getInstance(bundle), "ViewPagerFragment")
                        .addToBackStack(null).commit();

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
            p.setImageResource(imgList.get(i));
            p.touchEnable(false);//disable touch
            return p;
        }
    }
}
