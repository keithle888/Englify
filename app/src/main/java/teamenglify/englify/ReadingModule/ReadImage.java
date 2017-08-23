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

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Read;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;

public class ReadImage extends Fragment {
    private ViewPager viewPager;
    private ImageFragmentStateAdapter imageFragmentStateAdapter;
    private Read read;
    private static final String TAG = ReadImage.class.getSimpleName();
    private SpeechRecognition speechRecognition;
    private AudioBar audioBar;
    private ReadingModule readingModule;

    public ReadImage() {
        // Required empty public constructor
    }

    public static ReadImage newInstance(Read read, SpeechRecognition speechRecognition, AudioBar audioBar, ReadingModule readingModule) {
        ReadImage fragment = new ReadImage();
        fragment.read = read;
        fragment.speechRecognition = speechRecognition;
        fragment.audioBar = audioBar;
        fragment.readingModule = readingModule;
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
        View view =  inflater.inflate(R.layout.fragment_read_image, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.readViewPager);
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
                //Trigger for Audio Bar to change track/answer
                if (audioBar != null) {
                    Log.d(bucketName, "Class ReadImageImage: Method viewPager:onPageSelected: Changing Audio track to position => " + Integer.toString(position));
                    audioBar.setAudioTrack(position);
                } else {
                    Log.d(TAG, "Unable to find Audio Bar to trigger UI update.");
                }
                //Trigger for speech recognition
                if (speechRecognition != null) {
                    speechRecognition.updateUI(position);
                } else {
                    Log.d(TAG, "Unable to find Speech Recognition to trigger UI update.");
                }
                //Trigger for translation view update.
                if (readingModule != null) {
                    readingModule.updateTranslationView(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }
}
