package teamenglify.englify.ExerciseModule;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.support.v4.app.Fragment;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.ExerciseChapterPart;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by keith on 18-Jul-17.
 */

public class ExerciseChoicesAdapter extends BaseAdapter {
    private static final String TAG = ExerciseChoicesAdapter.class.getSimpleName();
    private ExerciseChapterPart exerciseChapterPart;
    private View exerciseChoicesView;
    private ExerciseModule exerciseModule;
    private TextView exercise_translation;
    private RelativeLayout exerciseUtils;

    private static final String delimiterBetweenAnswerOptions = ", ";

    public ExerciseChoicesAdapter(ExerciseChapterPart exerciseChapterPart, GridView exerciseChoicesView, ExerciseModule exerciseModule, TextView exercise_translation, RelativeLayout exerciseUtils) {
        this.exerciseChapterPart = exerciseChapterPart;
        this.exerciseChoicesView = exerciseChoicesView;
        this.exerciseModule = exerciseModule;
        this.exercise_translation = exercise_translation;
        this.exerciseUtils = exerciseUtils;
    }


    @Override
    public int getCount() {
        return exerciseChapterPart.choices.get(0).size();
    }

    @Override
    public Object getItem(int i) {
        return extractChoicesFromList(i, exerciseChapterPart.choices);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {

        final Button choice_button = (Button) new Button(viewGroup.getContext());
        choice_button.setPadding(5,5,5,5);

        //Set text for button
        final String textForButton = extractChoicesFromList(i, exerciseChapterPart.choices);
        choice_button.setText(textForButton);
        //setting so the text does not appear all caps.
        choice_button.setTransformationMethod(null);

        //Set behaviour of button
        final String answer = createStringFromList(exerciseChapterPart.answer);
        choice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textForButton.contentEquals(answer)) {
                    Log.i(TAG,"Correct answer selected.");
                    choice_button.setClickable(false);
                    //Play audio after correct answer
                    Fragment fragment = MainActivity.mainActivity.getSupportFragmentManager().findFragmentByTag(AudioBar.FM_TAG_NAME);
                    if (fragment != null) {
                        ((AudioBar) fragment).play();
                    } else {
                        Log.d(TAG, "Unable to find audio bar fragment to play track after correct choice selected.");
                    }
                    //Set exercise choices weight = 0
                    exerciseChoicesView.setLayoutParams(ExerciseModule.exerciseChoices_LayoutParam_Invisible);
                    //Get text_to_match to put the correct text into place.
                    exerciseModule.replaceQuestionWithAnswerIncluded();
                    //Make translation visible
                    exercise_translation.setLayoutParams(ExerciseModule.exercise_textview_Visible);
                    //Make media/util (speech recognition/audio bar) appear
                    exerciseUtils.setLayoutParams(ExerciseModule.utils_Visible);
                } else {
                    Log.i(TAG,"Wrong answer selected.");
                    choice_button.setClickable(false);
                    choice_button.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.holo_red_light));
                    Animation fadeout = AnimationUtils.loadAnimation(viewGroup.getContext(), R.anim.exercise_choices_button_fadeout);
                    choice_button.startAnimation(fadeout);
                    choice_button.setVisibility(View.INVISIBLE);
                }
            }
        });
        return choice_button;
    }

    private String extractChoicesFromList(int i, List<List<String>> choices) {
        StringBuilder builder = new StringBuilder();
        for (List<String> list : exerciseChapterPart.choices) {
            builder.append(list.get(i) + delimiterBetweenAnswerOptions);
        }
        return builder.toString().substring(0,
                builder.toString().length() - 2);
    }

    private String createStringFromList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String string : list) {
            builder.append(string + delimiterBetweenAnswerOptions);
        }
        return builder.toString().substring(0,
                builder.toString().length() - 2);
    }
}