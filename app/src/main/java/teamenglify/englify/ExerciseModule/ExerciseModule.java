package teamenglify.englify.ExerciseModule;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import teamenglify.englify.AudioBar;
import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.mainActivity;

public class ExerciseModule extends Fragment {
    private static final String TAG = ExerciseModule.class.getSimpleName();
    private ExerciseChapter exerciseChapter;
    public int partNumber;
    private String previous_actionbar_title;
    private GridView choices_grid_view;
    private TextView exercise_translation;
    private RelativeLayout exerciseUtils;

    public static final String exerciseTextToMatchBlankCharacter = "_";
    public static final String exerciseTextToMatchAnswerBlank = "____";

    //Layout params for reactive views.
    public static final LinearLayout.LayoutParams exerciseChoices_LayoutParam_Invisible = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            0f
    );
    public static final LinearLayout.LayoutParams exerciseChoices_LayoutParam_Visible = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            40f
    );
    public static final LinearLayout.LayoutParams exercise_textview_Invisible = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            0f
    );
    public static final LinearLayout.LayoutParams exercise_textview_Visible = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            10f
    );
    public static final LinearLayout.LayoutParams utils_Invisible = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            0f
    );
    public static final LinearLayout.LayoutParams utils_Visible = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            20f
    );

    public ExerciseModule() {
        // Required empty public constructor
    }

    public static ExerciseModule newInstance(ExerciseChapter exerciseChapter, String previous_actionbar_title) {
        ExerciseModule fragment = new ExerciseModule();
        fragment.exerciseChapter = exerciseChapter;
        fragment.previous_actionbar_title = previous_actionbar_title;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialize variables
        partNumber = 0;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_module, container, false);

        //Update action bar
        mainActivity.getSupportActionBar().setTitle(previous_actionbar_title + ListingFragment.ACTION_BAR_DELIMITER + exerciseChapter.name);

        //Add in image carousel
        ExerciseImage exerciseImage = ExerciseImage.newInstance(this, exerciseChapter);
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.exerciseImages, exerciseImage)
                .commit();

        //Bind Views
        choices_grid_view = (GridView) view.findViewById(R.id.exercise_choices_grid_view);
        exercise_translation = (TextView) view.findViewById(R.id.exercise_translation_textview);
        exerciseUtils = (RelativeLayout) view.findViewById(R.id.exercise_utils);

        Log.d("Englify", "Class ExerciseModule: Method onCreateView(): Loading Exercise Module.");

        //Pull in audio player
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.audioBarFrameLayoutExercise, AudioBar.newInstance(exerciseChapter), AudioBar.FM_TAG_NAME)
                .commit();

        //Pull in speech recognition
        ProgressBar pb = (ProgressBar) view.findViewById(R.id.speechProgressBar_Exercise);
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.speechRecognitionButtonFrameLayoutExercise,
                        SpeechRecognition.newInstance(exerciseChapter,
                                (TextView) view.findViewById(R.id.speechRecognitionTextViewExercise_TextToMatch),
                                (TextView) view.findViewById(R.id.speechRecognitionTextViewExercise_Return),
                                pb),
                        "SPEECH_RECOGNITION")
                .commit();

        //bindButtonBehaviour();

        updateExercisePage(partNumber);
        return view;
    }

    public void updateExercisePage(int page) {
        Log.d(TAG,"Updating exercise module for page number:" + page);
        partNumber = page;
        updateSpeechRecognition(page);
        updateAudioBar(page);
        updateChoicesView(page);
        updateTranslationView(page);
        updateUtilView();
    }

    private void updateUtilView() {
        exerciseUtils.setLayoutParams(utils_Invisible);
    }

    private void updateTranslationView(int page) {
        Log.d(TAG,"Updating translation text view.");
        if (exercise_translation != null) {
            exercise_translation.setText(exerciseChapter.chapterParts.get(page).translation);
            exercise_translation.setLayoutParams(exercise_textview_Invisible);
        } else {
            Log.e(TAG,"Updating translation text view failed.");
        }
    }

    private void updateSpeechRecognition(int page) {
        SpeechRecognition sr = (SpeechRecognition) mainActivity.getSupportFragmentManager().findFragmentByTag(SpeechRecognition.FM_TAG_NAME);
        if (sr != null) {
            sr.updateUI(page);
        } else {
            Log.d(TAG, "Unable to find speech recognition to update.");
        }
    }

    private void updateAudioBar(int page) {
        AudioBar ab = (AudioBar) mainActivity.getSupportFragmentManager().findFragmentByTag(AudioBar.FM_TAG_NAME);
        if (ab != null) {
            ab.setAudioTrack(page);
        } else {
            Log.d(TAG, "Unable to find Audio Bar to update.");
        }
    }

    private void updateChoicesView(int page) {
        if (choices_grid_view != null) {
            //Make sure choices are visible
            choices_grid_view.setLayoutParams(exerciseChoices_LayoutParam_Visible);
            //Set adapter
            choices_grid_view.setAdapter(
                    new ExerciseChoicesAdapter(
                            exerciseChapter.chapterParts.get(page),
                            choices_grid_view,
                            this,
                            exercise_translation,
                            exerciseUtils
                    )
            );
        }
    }

    public void replaceQuestionWithAnswerIncluded() {
        Log.d(TAG,"Updating Speech Recognition Text To Match with answer.");
        String question = exerciseChapter.chapterParts.get(partNumber).question;
        for (String answerPart : exerciseChapter.chapterParts.get(partNumber).answer) {
            if (question != null) {
                question = question.replaceFirst(exerciseTextToMatchAnswerBlank, answerPart);
            }
        }
        SpeechRecognition sr = (SpeechRecognition) mainActivity.getSupportFragmentManager().findFragmentByTag(SpeechRecognition.FM_TAG_NAME);
        if (sr != null) {
            sr.speechToMatchTextView.setText(question);
        } else {
            Log.d(TAG,"Unable to locate speech recognition fragment for text view answer replacement.");
        }

        Log.d(TAG,"Making translation text view visible.");
        if (exercise_translation != null) {
            exercise_translation.setLayoutParams(exercise_textview_Visible);
        }
    }
}
