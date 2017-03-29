package teamenglify.englify.ReadingModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Read;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

public class ReadImage extends Fragment {
    private ViewPager viewPager;
    private ImageFragmentStateAdapter imageFragmentStateAdapter;
    private Read read;

    public ReadImage() {
        // Required empty public constructor
    }

    public static ReadImage newInstance(Read read) {
        ReadImage fragment = new ReadImage();
        fragment.read = read;
        return fragment;
    }

    public static void recordDataRead (int position){
        HashMap<String,ArrayList<String>> analyticListRead = MainActivity.analyticListRead;
        ArrayList<String> dataRecorded = analyticListRead.get(MainActivity.strGrade+MainActivity.lesson);
        if(dataRecorded==null){
            analyticListRead.put(MainActivity.strGrade+MainActivity.lesson, new ArrayList<String>());
            Log.d("analytic read", "null");
            dataRecorded = new ArrayList<>();
            dataRecorded.add(Integer.toString(position));
            analyticListRead.put(MainActivity.strGrade+MainActivity.lesson, dataRecorded);
        } else {
            boolean isExist = false;
            for(String temp : dataRecorded){
                if(temp.equalsIgnoreCase(Integer.toString(position))){
                    isExist = true;
                }
            }
            if(!isExist){
                dataRecorded.add(Integer.toString(position));
                analyticListRead.put(MainActivity.strGrade+MainActivity.lesson, dataRecorded);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_read_image, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.readViewPager);
        imageFragmentStateAdapter = new ImageFragmentStateAdapter(MainActivity.getMainActivity().getSupportFragmentManager(), read);
        viewPager.setAdapter(imageFragmentStateAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("ReadImage", "viewPager:onPageSelected: " + Integer.toString(position));
                if (mainActivity.position != position) {
                    mainActivity.position = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return v;
    }
}
