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

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by keith on 18-Jul-17.
 */

public class ExerciseChoicesAdapter extends BaseAdapter {
    private static final String TAG = ExerciseChoicesAdapter.class.getSimpleName();
    private String[] choices;
    private String answer;
    private View exerciseChoicesView;

    public ExerciseChoicesAdapter(String[] choices, String answer, GridView exerciseChoicesView) {
        this.choices = choices;
        this.answer = answer;
        this.exerciseChoicesView = exerciseChoicesView;
    }


    @Override
    public int getCount() {
        return choices.length;
    }

    @Override
    public Object getItem(int i) {
        return choices[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {

        final Button choice_button = (Button) new Button(viewGroup.getContext());
        choice_button.setPadding(5,5,5,5);

        //Set text for button
        final String textForButton = choices[i];
        choice_button.setText(textForButton);
        //setting so the text does not appear all caps.
        choice_button.setTransformationMethod(null);

        //Set behaviour of button
        choice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textForButton.contentEquals(answer)) {
                    choice_button.setClickable(false);
                    //Play audio after correct answer
                    Fragment fragment = MainActivity.mainActivity.getSupportFragmentManager().findFragmentByTag(AudioBar.FM_TAG_NAME);
                    if (fragment != null) {
                        ((AudioBar) fragment).play();
                    } else {
                        Log.d(TAG, "Unable to find audio bar fragment to play track after correct choice selected.");
                    }
                    //Set exercise choices weight = 0
                    exerciseChoicesView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                            0f
                        )
                    );
                    //Get text_to_match to put the correct text into place.
                } else {
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
}