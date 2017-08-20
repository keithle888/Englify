package teamenglify.englify.ExerciseModule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.Read;
import teamenglify.englify.R;
import teamenglify.englify.ReadingModule.ImageFragmentStateAdapter;

import static teamenglify.englify.MainActivity.mainActivity;

public class ExerciseImage extends Fragment {
    private ViewPager viewPager;
    private ExerciseFragmentStateAdapter imageFragmentStateAdapter;
    private ExerciseChapter exerciseChapter;
    private ExerciseModule exerciseModule;

    public ExerciseImage() {
        // Required empty public constructor
    }

    public static ExerciseImage newInstance(ExerciseModule exerciseModule, ExerciseChapter exerciseChapter) {
        ExerciseImage fragment = new ExerciseImage();
        fragment.exerciseChapter = exerciseChapter;
        fragment.exerciseModule = exerciseModule;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_exercise_image, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.execiseViewPager);
        imageFragmentStateAdapter = new ExerciseFragmentStateAdapter(MainActivity.getMainActivity().getSupportFragmentManager(), exerciseChapter);
        viewPager.setAdapter(imageFragmentStateAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("Englify", "Class ExerciseImage: Method viewPager:onPageSelected: " + Integer.toString(position));
                exerciseModule.updateExercisePage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }
}
