package teamenglify.englify.ExerciseModule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import teamenglify.englify.AudioBar;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

public class ExerciseModule extends Fragment {
    private ExerciseChapter exerciseChapter;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_module, container, false);
        FragmentManager fm = getChildFragmentManager();
        Log.d("Englify", "Class ExerciseModule: Method onCreateView(): Loading Exercise Module.");
        fm.beginTransaction().add(R.id.exerciseImage, ExerciseImage.newInstance(exerciseChapter)).commit();
        fm.beginTransaction().add(R.id.exerciseAudioBar, AudioBar.newInstance(exerciseChapter)).commit();
        fm.beginTransaction().add(R.id.exerciseSpeechBar, SpeechRecognition.newInstance(exerciseChapter)).commit();
        return view;
    }
}
