package com.david.assembleimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import me.xiaopan.sketch.CancelCause;
import me.xiaopan.sketch.FailCause;
import me.xiaopan.sketch.ImageFrom;
import me.xiaopan.sketch.LoadListener;
import me.xiaopan.sketch.RecycleBitmapDrawable;
import me.xiaopan.sketch.Sketch;


public class BitmapMaker {
    private ArrayList<Drawable> bitmapList = new ArrayList<>();
    private int compeletedNum = 0;
    private String urlKey;
    private Context context;
    int needWidth;
    int needHeight;

    public BitmapMaker(Context context) {
        this.context = context;
    }

    public void setAssembleBitmap(final ImageView imageView, final List<String> urlList, final int width, final int height) {
        needHeight = height;
        needWidth = width;
        /**先检查内存中是否有该图片**/
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : urlList) {
            stringBuilder.append(s);
        }
        urlKey = stringBuilder.toString();
        Drawable drawable = Sketch.with(context).getConfiguration().getMemoryCache().get(urlKey);
        if (drawable != null) {
            imageView.setImageBitmap(getFinalBitmap(drawable, width));
        } else {
            /**没有图片，从网络加载**/
            for (int i = 0; i < urlList.size(); i++) {
                Sketch.with(context).load(urlList.get(i), new LoadListener() {
                    @Override
                    public void onStarted() {

                    }
                    @Override
                    public void onCompleted(Drawable drawable, ImageFrom imageFrom, String mimeType) {
                        bitmapList.add(drawable);
                        compeletedNum++;
                        if (compeletedNum == urlList.size()) {
                            Bitmap finalBitmap = makeWithDrawableList(bitmapList, width, height);
                            imageView.setImageBitmap(finalBitmap);
                        }
                    }

                    @Override
                    public void onFailed(FailCause failCause) {
                    }

                    @Override
                    public void onCanceled(CancelCause cancelCause) {

                    }
                }).commit();
            }
        }
    }

    /**
     * 将缓存的drawable 转成对应尺寸的 Bitmap
     *
     * @param drawable 缓存的drawable
     * @param width    最终显示的区域的宽度
     */
    public Bitmap getFinalBitmap(Drawable drawable, int width) {
        Bitmap bitmapRaw = Bitmap.createBitmap(
                width,
                width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Bitmap bitmapTemp = rotateBitmap(bitmapRaw, 45);
        bitmapRaw.recycle();
        Bitmap bitmapFinal = Bitmap.createBitmap(
                bitmapTemp.getWidth(),
                bitmapTemp.getHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmapFinal);
        drawable.setBounds(0, 0, bitmapTemp.getWidth(), bitmapTemp.getHeight());
        bitmapTemp.recycle();
        drawable.draw(canvas);
        return bitmapFinal;
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     *
     * @param bitmap  原始图片
     * @param degrees 原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    public Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(needWidth, needHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.rotate(45, needWidth, needHeight * 0.3f);
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (null != bitmap) {
            bitmap.recycle();
        }
        return bmp;
    }

    /**
     * 组合图片，循环的从列表中取图片，组装成4*3的大图片
     * 小图片的大小是要显示区域的一半，高度等比拉伸
     * 组装完成后旋转45°
     * 最后将处理好的图片放到内存缓存起来。
     */
    public Bitmap makeWithDrawableList(ArrayList<Drawable> bitmapList, int needWidth, int needHeight) {
        Drawable drawable = bitmapList.get(0);
        int smallWidth = (int) (needWidth * 0.5);
        int smallHeight = (int) (needHeight * 0.6);
        int width = smallWidth * 3;
        int height = smallHeight * 4;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                drawable = bitmapList.get((i * 3 + j) % bitmapList.size());
                drawable.setBounds(smallWidth * j, smallHeight * i, smallWidth * (j + 1), smallHeight * (i + 1));
                drawable.draw(canvas);
            }
        }
        result = rotateBitmap(result, 45);

        if (Sketch.with(context).getConfiguration().getMemoryCache().get(urlKey) != null) {
            Sketch.with(context).getConfiguration().getMemoryCache().put(urlKey, new RecycleBitmapDrawable(result));
        }
        return result;
    }


}

