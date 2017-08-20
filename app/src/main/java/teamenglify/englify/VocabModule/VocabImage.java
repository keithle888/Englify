package teamenglify.englify.VocabModule;


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
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VocabImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VocabImage extends Fragment {
    private ViewPager viewPager;
    private VocabFragmentStateAdapter vocabFragmentStateAdapter;
    private Vocab vocab;
    private static final String TAG = VocabImage.class.getSimpleName();

    public VocabImage() {
        // Required empty public constructor
    }

    public static VocabImage newInstance(Vocab vocab) {
        VocabImage fragment = new VocabImage();
        Bundle args = new Bundle();
        fragment.vocab = vocab;
        fragment.setArguments(args);
        return fragment;
    }

    public static void recordDataVocab (int position){
        HashMap<String,ArrayList<String>> analyticListVocab = MainActivity.analyticListVocab;
        ArrayList<String> dataRecorded = analyticListVocab.get(MainActivity.strGrade+MainActivity.lesson);
        if(dataRecorded==null){
            analyticListVocab.put(MainActivity.strGrade+MainActivity.lesson, new ArrayList<String>());
            Log.d("analytic vocab", "null");
            dataRecorded = new ArrayList<>();
            dataRecorded.add(Integer.toString(position));
            analyticListVocab.put(MainActivity.strGrade+MainActivity.lesson, dataRecorded);
        } else {
            boolean isExist = false;
            for(String temp : dataRecorded){
                if(temp.equalsIgnoreCase(Integer.toString(position))){
                    isExist = true;
                }
            }
            if(!isExist){
                dataRecorded.add(Integer.toString(position));
                analyticListVocab.put(MainActivity.strGrade+MainActivity.lesson, dataRecorded);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_vocab_image, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.vocabViewPager);
        vocabFragmentStateAdapter = new VocabFragmentStateAdapter(MainActivity.getMainActivity().getSupportFragmentManager(), vocab);
        viewPager.setAdapter(vocabFragmentStateAdapter);
        viewPager.setCurrentItem(MainActivity.position);
        final Fragment fragment_audio_bar = mainActivity.getSupportFragmentManager().findFragmentByTag("AUDIO_BAR");
        final Fragment fragmentSpeechRecognition = mainActivity.getSupportFragmentManager().findFragmentByTag(SpeechRecognition.FM_TAG_NAME);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(bucketName, "Class VocabImage: Method viewPager:onPageSelected: Page changed to =>" + Integer.toString(position));
                if (mainActivity.position != position) {
                    mainActivity.position = position;
                }
                recordDataVocab(position);
                //Trigger for Audio Bar and speech recognition to change track/answer
                if (fragment_audio_bar != null) {
                    Log.d(bucketName, "Class VocabImage: Method viewPager:onPageSelected: Changing Audio track to position => " + Integer.toString(position));
                    ((AudioBar)fragment_audio_bar).setAudioTrack(position);
                } else {
                    Log.d(TAG, "Unable to find Audio Bar to trigger UI update.");
                }

                //Trigger for speech recognition
                if (fragmentSpeechRecognition != null) {
                    ((SpeechRecognition)fragmentSpeechRecognition).updateUI(position);
                } else {
                    Log.d(TAG, "Unable to find Speech Recognition to trigger UI update.");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }
}