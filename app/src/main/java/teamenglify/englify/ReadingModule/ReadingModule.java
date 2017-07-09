package teamenglify.englify.ReadingModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Read;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReadingModule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadingModule extends Fragment {
    private Read read;

    public ReadingModule() {
        // Required empty public constructor
    }

    public static ReadingModule newInstance(Read read) {
        ReadingModule fragment = new ReadingModule();
        Bundle args = new Bundle();
        fragment.read = read;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_reading_module, container, false);
        FragmentManager fm = mainActivity.getSupportFragmentManager();
        ReadImage readImage = ReadImage.newInstance(read);
        SpeechRecognition speechRecognition = SpeechRecognition.newInstance(read);
        AudioBar audioBar = AudioBar.newInstance(read);
        fm.beginTransaction().add(R.id.readAudioBar, audioBar, "AUDIO_BAR").commit();
        fm.beginTransaction().add(R.id.readSpeechBar, speechRecognition, "SPEED_RECOGNITION").commit();
        fm.beginTransaction().add(R.id.readImage,readImage).commit();

        return v;
    }
}