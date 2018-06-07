package teamenglify.englify.ExerciseModule;

import android.media.MediaPlayer;
import android.os.Bundle;
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

import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;
import timber.log.Timber;

import static teamenglify.englify.MainActivity.mainActivity;

public class ExerciseModule extends Fragment {
    private ExerciseChapter exerciseChapter;
    public int partNumber;
    public TextView questionView;
    public GridView choicesView;
    private CarouselView exerciseCarousel;
    private ExerciseChoicesAdapter choicesAdapter = new ExerciseChoicesAdapter(this);

    private int mCurrentSubPartIndex;
    private int mCurrentPartIndex;

    //Media player
    private MediaPlayer mediaPlayer;

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
        questionView = view.findViewById(R.id.exercise_question);
        choicesView = view.findViewById(R.id.exercise_choices);
        exerciseCarousel = view.findViewById(R.id.exercise_image);

        //Update image carousel
        exerciseCarousel.setPageCount(exerciseChapter.chapterParts.size());
        exerciseCarousel.setImageListener(imageListener);
        exerciseCarousel.addOnPageChangeListener(pageChangeListener);

        //Populate question and choices
        questionView.setText(exerciseChapter.chapterParts.get(partNumber).question);
        choicesView.setAdapter(choicesAdapter);

        updateUIBasedOnPage(0);

        return view;
    }

    public void updateUIBasedOnPage(int position) {
        Timber.d("Updating UI to position: %s", position);
        choicesAdapter.setChoices(exerciseChapter.chapterParts.get(position).choices.get(0));
        questionView.setText(exerciseChapter.chapterParts.get(position).question);
        mCurrentPartIndex = position;
        mCurrentSubPartIndex = 0;
        stopAudioTrack();
    }

    public void updateBasedOnAnswerSelected(Button button) {
        //Check if answer selected is correct
        if (button.getText().toString().equalsIgnoreCase(exerciseChapter.chapterParts.get(mCurrentPartIndex).answer.get(mCurrentSubPartIndex))) {
            button.setBackgroundColor(getResources().getColor(R.color.green));
            playAudioTrack(mCurrentPartIndex);
            if (questionView.getText().toString().contains("_")) {
                questionView.setText(questionView.getText().toString().replaceFirst("_", exerciseChapter.chapterParts.get(mCurrentPartIndex).answer.get(mCurrentSubPartIndex)));
            }
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.red));
        }
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

    public class ExerciseChoicesAdapter extends BaseAdapter {
        public ExerciseChoicesAdapter(ExerciseModule exerciseModule) {
            this.exerciseModule = exerciseModule;
        }

        private ExerciseModule exerciseModule;
        private LinkedList<String> choices = new LinkedList<>();

        @Override
        public Object getItem(int position) {
            return choices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Button button = new Button(parent.getContext());
            button.setText(choices.get(position));
            button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    exerciseModule.updateBasedOnAnswerSelected((Button) view);
                }
            });
            return button;
        }

        @Override
        public int getCount() {
            return choices.size();
        }

        public void setChoices(@NonNull ArrayList<String> choices) {
            Timber.d("Choices set for exercise: %s", Arrays.toString(choices.toArray()));
            this.choices.clear();
            this.choices.addAll(choices);
            notifyDataSetChanged();
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
