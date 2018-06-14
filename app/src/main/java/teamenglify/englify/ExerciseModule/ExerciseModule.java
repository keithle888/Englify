package teamenglify.englify.ExerciseModule;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import teamenglify.englify.CustomSpeechRecognition;
import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;
import timber.log.Timber;

import static teamenglify.englify.MainActivity.mainActivity;

public class ExerciseModule extends Fragment {
    private ExerciseChapter exerciseChapter;
    public int partNumber;
    private CarouselView exerciseCarousel;

    //Media player
    private MediaPlayer mediaPlayer;

    private int mCurrentPart;

    public ExerciseModule() {
        // Required empty public constructor
    }

    public static ExerciseModule newInstance(ExerciseChapter exerciseChapter) {
        ExerciseModule fragment = new ExerciseModule();
        fragment.exerciseChapter = exerciseChapter;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialize variables
        partNumber = 0;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_module, container, false);
        Timber.d("Class ExerciseModule: Method onCreateView(): Loading Exercise Module.");
        exerciseCarousel = view.findViewById(R.id.exercise_image);

        //Update image carousel
        exerciseCarousel.setPageCount(exerciseChapter.chapterParts.size());
        exerciseCarousel.setImageListener(imageListener);
        exerciseCarousel.addOnPageChangeListener(pageChangeListener);

        updateUIBasedOnPage(0);

        return view;
    }

    public void updateUIBasedOnPage(final int position) {
        Timber.d("Updating UI to position: %s", position);
        mCurrentPart = position;
        stopAudioTrack();
        getFragmentManager().beginTransaction()
                .replace(R.id.exercise_fragment_bottom_segment, ExerciseQuestionModule.newInstance(exerciseChapter.chapterParts.get(position),
                        new ExerciseQuestionModule.Callback() {
                            @Override
                            public void onCorrectAnswerSelected(String completedQuestion) {
                                playAudioTrack(position);
                                loadSpeechRecognitionModule(position, completedQuestion);
                            }
                        }
                )).commit();
    }

    private void loadSpeechRecognitionModule(int position, String completedQuestion) {
        Timber.d("Loading speech recognition module: %s", position);
        getFragmentManager().beginTransaction()
                .replace(R.id.exercise_fragment_bottom_segment, ExerciseSpeechRecognitionModule.newInstance(completedQuestion))
                .commit();
    }

    public void playAudioTrack(int position) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            Timber.d("Getting audio file: %s", exerciseChapter.chapterParts.get(position).audioURL);
            FileInputStream audioFile = LocalSave.loadAudio(exerciseChapter.chapterParts.get(position).audioURL);
            mediaPlayer.setDataSource(audioFile.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Timber.e(e, "Failed to set audio track.");
        }
    }

    public void stopAudioTrack() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            if (exerciseChapter.chapterParts.get(position).imageURL != null) {
                Glide.with(getContext())
                        .load(LocalSave.loadImage(exerciseChapter.chapterParts.get(position).imageURL))
                        .fitCenter()
                        .error(R.drawable.logonocontent)
                        .into(imageView);
            }
        }
    };

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override
        public void onPageSelected(int position) {
            Timber.d("User switched exercise image to page: %s", position);
            updateUIBasedOnPage(position);
        }
        @Override
        public void onPageScrollStateChanged(int state) {}
    };

}
