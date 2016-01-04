package com.david.assembleimage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView imageView = (ImageView) findViewById(R.id.iamge_mainActivity_image);
        final ArrayList<String> urlList = new ArrayList<>();
        urlList.add("http://b.hiphotos.baidu.com/zhidao/pic/item/d0c8a786c9177f3ece25db6471cf3bc79f3d5641.jpg");
        urlList.add("http://g.hiphotos.baidu.com/zhidao/pic/item/b2de9c82d158ccbf62431ba419d8bc3eb03541b4.jpg");
        urlList.add("http://img2.cache.netease.com/photo/0005/2013-03-23/8QL3CAO04TM10005.jpg");
        urlList.add("http://img3.imgtn.bdimg.com/it/u=1636101420,4020019627&fm=21&gp=0.jpg");
        ViewTreeObserver vto = imageView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                BitmapMaker bitmapMaker=new BitmapMaker(MainActivity.this);
                bitmapMaker.setAssembleBitmap(imageView, urlList, imageView.getWidth()/2,imageView.getHeight()/2);
            }
        });
    }
}
