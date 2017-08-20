package teamenglify.englify.ReadingModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import teamenglify.englify.AudioBar;
import teamenglify.englify.Listing.ListingFragment;
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
    private String previous_actionbar_title;

    public ReadingModule() {
        // Required empty public constructor
    }

    public static ReadingModule newInstance(Read read, String previous_actionbar_title) {
        ReadingModule fragment = new ReadingModule();
        Bundle args = new Bundle();
        fragment.read = read;
        fragment.previous_actionbar_title = previous_actionbar_title;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reading_module, container, false);
        FragmentManager fm = getFragmentManager();
        //Pull in speech recognition
        ProgressBar pb = (ProgressBar) view.findViewById(R.id.speechProgressBar_Read);
        SpeechRecognition speechRecognition = SpeechRecognition.newInstance(read,
                (TextView) view.findViewById(R.id.speechRecognitionTextViewRead_TextToMatch),
                (TextView) view.findViewById(R.id.speechRecognitionTextViewRead_Return),
                pb);
        AudioBar audioBar = AudioBar.newInstance(read);
        ReadImage readImage = ReadImage.newInstance(read, speechRecognition, audioBar);
        fm.beginTransaction().add(R.id.audioBarFrameLayoutRead, audioBar, "AUDIO_BAR").commit();
        fm.beginTransaction().add(R.id.speechRecognitionButtonFrameLayoutRead, speechRecognition, "SPEED_RECOGNITION").commit();
        fm.beginTransaction().add(R.id.readImage,readImage).commit();

        mainActivity.getSupportActionBar().setTitle(previous_actionbar_title + ListingFragment.ACTION_BAR_DELIMITER + read.name);
        return view;
    }
}