package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.R;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_vocab_image, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.vocabViewPager);
        vocabFragmentStateAdapter = new VocabFragmentStateAdapter(MainActivity.getMainActivity().getSupportFragmentManager(), vocab);
        viewPager.setAdapter(vocabFragmentStateAdapter);
        viewPager.setCurrentItem(MainActivity.position);
        Log.d("VocabImage", MainActivity.position+"");
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("VocabImage", "viewPager:onPageSelected: " + Integer.toString(position));
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