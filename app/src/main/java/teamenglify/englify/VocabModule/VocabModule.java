package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VocabModule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VocabModule extends Fragment {
    private final static String TAG = VocabModule.class.getSimpleName();
    private Vocab vocab;

    public VocabModule() {
        // Required empty public constructor
    }

    public static VocabModule newInstance(Vocab vocab) {
        VocabModule fragment = new VocabModule();
        Bundle args = new Bundle();
        fragment.vocab = vocab;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vocab_module, container, false);
        //Log.d("vocab", MainActivity.getMainActivity().getCurrentListingURL());
        MainActivity.getMainActivity().getSupportActionBar().setTitle("Study Vocab");
        FragmentManager fm = getFragmentManager();
        //Pull in speech recognition
        ProgressBar pb = (ProgressBar) view.findViewById(R.id.speechProgressBar_Exercise);
        SpeechRecognition speechRecognition = SpeechRecognition.newInstance(vocab,
                (TextView) view.findViewById(R.id.speechRecognitionTextViewVocab_TextToMatch),
                (TextView) view.findViewById(R.id.speechRecognitionTextViewVocab_Return),
                pb);
        AudioBar audioBar = AudioBar.newInstance(vocab);
        VocabImage vocabImage = VocabImage.newInstance(vocab, speechRecognition, audioBar);

        Log.d(TAG, "Loading fragment for vocab image.");
        fm.beginTransaction().add(R.id.vocabImage, vocabImage).commit();
        Log.d(TAG, "Loading fragment for vocab audio bar.");
        fm.beginTransaction().add(R.id.audioBarFrameLayoutVocab, audioBar, AudioBar.FM_TAG_NAME).commit();
        Log.d(TAG, "Loading fragment for vocab speech recognition.");
        fm.beginTransaction().add(R.id.speechRecognitionButtonFrameLayoutVocab, speechRecognition, SpeechRecognition.FM_TAG_NAME).commit();

        return view;
    }

}
