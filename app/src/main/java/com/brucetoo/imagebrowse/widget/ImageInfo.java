package com.brucetoo.imagebrowse.widget;

import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

/**
 * Created by Bruce too
 * On 2016/4/18
 * At 20:26
 */
public class ImageInfo implements Parcelable {

    public static final String INTENT_IMAGE_URLS = "imageUrls";
    public static final String INTENT_CLICK_IMAGE_POSITION = "preImagePosition";
    public static final String INTENT_CLICK_IMAGE_INFO = "clickImageInfo";
    public static final String INTENT_IMAGE_INFOS = "imageInfos";

    // 内部图片在整个窗口的位置
    RectF mRect = new RectF();
    // 控件在窗口的位置
    RectF mLocalRect = new RectF();
    RectF mImgRect = new RectF();
    RectF mWidgetRect = new RectF();
    float mScale;
    float mDegrees;
    ImageView.ScaleType mScaleType;

    public ImageInfo(RectF rect, RectF local, RectF img, RectF widget, float scale, float degrees, ImageView.ScaleType scaleType) {
        mRect.set(rect);
        mLocalRect.set(local);
        mImgRect.set(img);
        mWidgetRect.set(widget);
        mScale = scale;
        mScaleType = scaleType;
        mDegrees = degrees;
    }

    protected ImageInfo(Parcel in) {
        mRect = in.readParcelable(RectF.class.getClassLoader());
        mLocalRect = in.readParcelable(RectF.class.getClassLoader());
        mImgRect = in.readParcelable(RectF.class.getClassLoader());
        mWidgetRect = in.readParcelable(RectF.class.getClassLoader());
        mScale = in.readFloat();
        mDegrees = in.readFloat();
    }

    /**
     * Correct ImageInfo Use in {@link com.brucetoo.imagebrowse.ImageBrowseDialogFragment}
     * @param rootLocation  root view's location in screen
     * @param statusBarHeight use status bar's height if {@link com.brucetoo.imagebrowse.ImageBrowseDialogFragment#onActivityCreated(Bundle)}
     *                        no set fragment fullscreen flag...
     */
    public void correct(int[] rootLocation, int statusBarHeight){
        mRect.left = mRect.left + rootLocation[0];
        mRect.right = mRect.right + rootLocation[0];
        mRect.top = mRect.top + rootLocation[1] - statusBarHeight;
        mRect.bottom = mRect.bottom + rootLocation[1] - statusBarHeight;

        mLocalRect.left = mLocalRect.left + rootLocation[0];
        mLocalRect.right = mLocalRect.right + rootLocation[0];
        mLocalRect.top = mLocalRect.top + rootLocation[1] - statusBarHeight;
        mLocalRect.bottom = mLocalRect.bottom + rootLocation[1] - statusBarHeight;

        mImgRect.left = mImgRect.left + rootLocation[0];
        mImgRect.right = mImgRect.right + rootLocation[0];
        mImgRect.top = mImgRect.top + rootLocation[1] - statusBarHeight;
        mImgRect.bottom = mImgRect.bottom + rootLocation[1] - statusBarHeight;

        mWidgetRect.left = mWidgetRect.left + rootLocation[0];
        mWidgetRect.right = mWidgetRect.right + rootLocation[0];
        mWidgetRect.top = mWidgetRect.top + rootLocation[1] - statusBarHeight;
        mWidgetRect.bottom = mWidgetRect.bottom + rootLocation[1] - statusBarHeight;
    }

    public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        @Override
        public ImageInfo createFromParcel(Parcel in) {
            return new ImageInfo(in);
        }

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mRect, i);
        parcel.writeParcelable(mLocalRect, i);
        parcel.writeParcelable(mImgRect, i);
        parcel.writeParcelable(mWidgetRect, i);
        parcel.writeFloat(mScale);
        parcel.writeFloat(mDegrees);
    }
}
