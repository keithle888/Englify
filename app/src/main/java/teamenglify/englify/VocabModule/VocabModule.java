package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import teamenglify.englify.AudioBar;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;
import teamenglify.englify.SpeechRecognition;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VocabModule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VocabModule extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public VocabModule() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VocabModule.
     */
    // TODO: Rename and change types and number of parameters
    public static VocabModule newInstance(String param1, String param2) {
        VocabModule fragment = new VocabModule();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_vocab_module, container, false);
        //Log.d("vocab", MainActivity.getMainActivity().getCurrentListingURL());
        MainActivity.getMainActivity().getSupportActionBar().setTitle("Study Vocab");
        FragmentManager fm = getChildFragmentManager();
        VocabImage vocabImage = new VocabImage();
        SpeechRecognition speechRecognition = SpeechRecognition.newInstance("Vocab", null);
        AudioBar audioBar = AudioBar.newInstance("Vocab", null);
        fm.beginTransaction().add(R.id.vocabImage,vocabImage).commit();
        fm.beginTransaction().add(R.id.vocabAudioBar, audioBar).commit();
        fm.beginTransaction().add(R.id.vocabSpeechBar, speechRecognition).commit();
        return v;
    }

}
