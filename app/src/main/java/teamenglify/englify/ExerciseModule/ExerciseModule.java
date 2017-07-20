package teamenglify.englify.ExerciseModule;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import teamenglify.englify.AudioBar;
import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.mainActivity;

public class ExerciseModule extends Fragment {
    private static final String TAG = ExerciseModule.class.getSimpleName();
    private ExerciseChapter exerciseChapter;
    public int partNumber;
    private GridView choices_grid_view;
    private ImageView exercise_image_view;
    private Button exercise_forward_button;
    private Button exercise_back_button;



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

        //Bind Views
        choices_grid_view = (GridView) view.findViewById(R.id.exercise_choices_grid_view);
        exercise_image_view = (ImageView) view.findViewById(R.id.exerciseImageView);

        Log.d("Englify", "Class ExerciseModule: Method onCreateView(): Loading Exercise Module.");

        //Pull in audio player and speech recognition
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.audioBarFrameLayoutExercise, AudioBar.newInstance(exerciseChapter), "AUDIO_BAR")
                .commit();
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.speechRecognitionButtonFrameLayoutExercise,
                        SpeechRecognition.newInstance(exerciseChapter,
                                (TextView) view.findViewById(R.id.speechRecognitionTextViewExercise_TextToMatch),
                                (TextView) view.findViewById(R.id.speechRecognitionTextViewExercise_Return)),
                        "SPEECH_RECOGNITION")
                .commit();

        choices_grid_view.setAdapter(new ExerciseChoicesAdapter(
                exerciseChapter.chapterParts.get(partNumber).choices,
                exerciseChapter.chapterParts.get(partNumber).answer));

        updateExerciseImageView(partNumber);
        return view;
    }

    public void updateExerciseImageView(int i) {
        Log.d(TAG, "Updating exercise image view.");
        String imgURL = exerciseChapter.chapterParts.get(i).imageURL;
        Log.d(TAG, "Attempting to load imageURL: " + imgURL);
        if (exercise_image_view != null) {
            if (imgURL != null) {
                exercise_image_view.setImageBitmap(LocalSave.getImageByBitmap(imgURL));
            } else {
                exercise_image_view.setImageResource(R.drawable.englify_logo);
            }
        } else {
            Log.d(TAG, "Exercise Image View not yet binded == is null.");
        }
    }
}
