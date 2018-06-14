package teamenglify.englify.ExerciseModule;


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
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.ExerciseChapterPart;
import teamenglify.englify.R;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseQuestionModule extends Fragment {
    private ExerciseChapterPart chapterPart;
    private Callback callback;

    private ExerciseChoicesAdapter choicesAdapter = new ExerciseChoicesAdapter(this);
    private TextView questionTextView;
    private GridView choicesGridView;

    private int mCurrentSubPartIndex = 0;

    public ExerciseQuestionModule() {
        // Required empty public constructor
    }

    public static Fragment newInstance(ExerciseChapterPart chapterPart, Callback callback) {
        ExerciseQuestionModule frag = new ExerciseQuestionModule();
        frag.chapterPart = chapterPart;
        frag.callback = callback;
        return frag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_question_module, container, false);

        choicesGridView = view.findViewById(R.id.exercise_choices);
        questionTextView = view.findViewById(R.id.exercise_question);

        questionTextView.setText(chapterPart.question);
        choicesGridView.setAdapter(choicesAdapter);

        updateUIBasedQuestionPart(0);
        return view;
    }

    private void updateUIBasedQuestionPart(int position) {
        choicesAdapter.setChoices(chapterPart.choices.get(position));
        mCurrentSubPartIndex = position;
    }

    private void updateUIBasedOnChoiceSelected(Button button) {
        //Check if answer selected is correct
        if (!button.getText().toString().equalsIgnoreCase(chapterPart.answer.get(mCurrentSubPartIndex))) {
            button.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.green));
            //Update question text view
            questionTextView.setText(questionTextView.getText().toString().replaceFirst("_", chapterPart.answer.get(mCurrentSubPartIndex)));
            //Check if question is completely solved
            if (questionTextView.getText().toString().contains("_") && mCurrentSubPartIndex < chapterPart.answer.size()-1) {
                updateUIBasedQuestionPart(mCurrentSubPartIndex+1);
            } else {
                //Actions if question is complete
                callback.onCorrectAnswerSelected(questionTextView.getText().toString());
            }
        }
    }

    public interface Callback {
        public void onCorrectAnswerSelected(String completedQuestion);
    }

    public class ExerciseChoicesAdapter extends BaseAdapter {
        public ExerciseChoicesAdapter(ExerciseQuestionModule module) {
            this.exerciseModule = module;
        }

        private ExerciseQuestionModule exerciseModule;
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
                    exerciseModule.updateUIBasedOnChoiceSelected((Button)view);
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
}
