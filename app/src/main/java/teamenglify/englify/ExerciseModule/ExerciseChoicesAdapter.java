package teamenglify.englify.ExerciseModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;

import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by keith on 18-Jul-17.
 */

public class ExerciseChoicesAdapter extends BaseAdapter {
    private String[] choices;
    private String answer;

    public ExerciseChoicesAdapter(String[] choices, String answer) {
        this.choices = choices;
        this.answer = answer;
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
                    //Additional methods to set weight for view with choices to be 0, and the answer to appear on the question.
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