package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VocabModule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VocabModule extends Fragment {
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
        View v = inflater.inflate(R.layout.fragment_vocab_module, container, false);
        //Log.d("vocab", MainActivity.getMainActivity().getCurrentListingURL());
        MainActivity.getMainActivity().getSupportActionBar().setTitle("Study Vocab");
        FragmentManager fm = getChildFragmentManager();
        VocabImage vocabImage = VocabImage.newInstance(vocab);
        SpeechRecognition speechRecognition = SpeechRecognition.newInstance(vocab);
        AudioBar audioBar = AudioBar.newInstance(vocab);
        fm.beginTransaction().add(R.id.vocabImage,vocabImage).commit();
        fm.beginTransaction().add(R.id.vocabAudioBar, audioBar).commit();
        //fm.beginTransaction().add(R.id.vocabSpeechBar, speechRecognition).commit();
        return v;
    }

}
