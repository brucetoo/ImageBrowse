package com.brucetoo.activityanimation.widget;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

/**
 * Created by Bruce Too
 * On 9/28/15.
 * At 19:37
 * Image图片在界面显示时的坐标位置等
 */
public class ImageInfo implements Parcelable{
    // 内部图片在整个窗口的位置
    RectF mRect = new RectF();
    RectF mLocalRect = new RectF();
    RectF mImgRect = new RectF();
    RectF mWidgetRect = new RectF();
    float mScale;
    ImageView.ScaleType mScaleType;//image的scaleType属性

    public ImageInfo(RectF rect, RectF local, RectF img, RectF widget, float scale, ImageView.ScaleType scaleType) {
        this.mRect.set(rect);
        this.mLocalRect.set(local);
        this.mImgRect.set(img);
        this.mWidgetRect.set(widget);
        this.mScale = scale;
        this.mScaleType = scaleType;
    }

    protected ImageInfo(Parcel in) {
        mRect = in.readParcelable(RectF.class.getClassLoader());
        mLocalRect = in.readParcelable(RectF.class.getClassLoader());
        mImgRect = in.readParcelable(RectF.class.getClassLoader());
        mWidgetRect = in.readParcelable(RectF.class.getClassLoader());
        mScale = in.readFloat();
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRect, flags);
        dest.writeParcelable(mLocalRect, flags);
        dest.writeParcelable(mImgRect, flags);
        dest.writeParcelable(mWidgetRect, flags);
        dest.writeFloat(mScale);
    }
}
