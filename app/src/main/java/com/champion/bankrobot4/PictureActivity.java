package com.champion.bankrobot4;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PictureActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private String effect = "DepthPage";
    private int REQUEST_CODE_PICK_IMAGE = 2;
    @BindView(R.id.slider)
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ButterKnife.bind(this);
        initAction();
    }

    private void initAction() {
//        HashMap<String, File> file_maps = new HashMap<String, File>();
//        File file = Environment.getExternalStorageDirectory();
//        String path = file.toString() + "/Pictures/Screenshots/";
//        File pathFile = new File(path);
//        MyFilter filter = new MyFilter(".jpg");
//        File[] files = pathFile.listFiles(filter);
//        if (files != null) {
//            for (File fileChild : files) {
//                String filepath = fileChild.toString();
//                int location = filepath.lastIndexOf("/");
//                String filename = filepath.substring(location + 1);
//                System.out.println("filename -> " + filename);
//                file_maps.put(filename, fileChild);
//            }
//        }

        Map<String, Integer> file_maps = new LinkedHashMap<String, Integer>();//LinkedHashMap有序,HashMap无序
        file_maps.put("荷福创品-全自主服务机器人系统方案提供商", R.drawable.c1);
        file_maps.put("荷福创品-核心技术", R.drawable.c2);
        file_maps.put("荷福创品-健身伙伴机器人", R.drawable.c3);
        file_maps.put("荷福创品-团队介绍", R.drawable.c4);
        file_maps.put("荷福创品-领导关怀", R.drawable.c5);
        file_maps.put("荷福创品", R.drawable.c6);

        Iterator iter = file_maps.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = entry.getKey().toString();
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);

            mSlider.addSlider(textSliderView);
        }
        mSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
//        mSlider.setDuration(4000);
        mSlider.addOnPageChangeListener(this);
        mSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        mSlider.setPresetTransformer(effect);
    }

    @OnClick(R.id.picture_back)
    void back() {
        finish();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("Slider Demo", "Page Changed: " + position);
    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mSlider.stopAutoCycle();
        super.onStop();
    }

    static class MyFilter implements FilenameFilter {
        private String type;

        public MyFilter(String type) {
            this.type = type;
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(type);
        }
    }
}
