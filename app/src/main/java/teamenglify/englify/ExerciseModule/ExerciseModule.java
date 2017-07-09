package teamenglify.englify.ExerciseModule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import teamenglify.englify.AudioBar;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.mainActivity;

public class ExerciseModule extends Fragment {
    private ExerciseChapter exerciseChapter;
    public int partNumber;
    public TextView questionView;
    public RecyclerView choices_recycler;



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
        Log.d("Englify", "Class ExerciseModule: Method onCreateView(): Loading Exercise Module.");
        questionView = (TextView) view.findViewById(R.id.exercise_question);
        choices_recycler = (RecyclerView) view.findViewById(R.id.exercise_choices);

        //Pull in audio player and speech recognition
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.exerciseAudioBar, AudioBar.newInstance(exerciseChapter), "AUDIO_BAR")
                .commit();
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.exerciseSpeechBar, SpeechRecognition.newInstance(exerciseChapter), "SPEECH_RECOGNITION")
                .commit();

        //Populate question and choices
        questionView.setText(exerciseChapter.chapterParts.get(partNumber).question);
        choices_recycler.setAdapter(new ExerciseChoicesAdapter());
        return view;
    }

    public class ExerciseChoicesAdapter extends RecyclerView.Adapter<ExerciseChoicesViewHolder> {
        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public ExerciseChoicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ExerciseChoicesViewHolder holder, int position) {

        }
    }

    public class ExerciseChoicesViewHolder extends RecyclerView.ViewHolder {
        public ExerciseChoicesViewHolder(View view) {
            super(view);
        }
    }
}
