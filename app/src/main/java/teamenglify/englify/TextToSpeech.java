package teamenglify.englify;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextToSpeech.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextToSpeech#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextToSpeech extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private android.speech.tts.TextToSpeech tts;
    private EditText inputTextView;
    private ImageButton button;

    public TextToSpeech() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TextToSpeech.
     */
    // TODO: Rename and change types and number of parameters
    public static TextToSpeech newInstance(String param1, String param2) {
        TextToSpeech fragment = new TextToSpeech();
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
    public void onResume() {
        super.onResume();
        MainActivity.mainActivity.getSupportActionBar().setTitle(R.string.pronounce_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_to_speech, container, false);
        button = (ImageButton) view.findViewById(R.id.ttsButton);
        inputTextView = (EditText) view.findViewById(R.id.ttsInputText);
        initialize();
        return view;
    }

    public void initialize() {
        //initialize the TextToSpeech
        tts = new android.speech.tts.TextToSpeech(getContext(), new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == android.speech.tts.TextToSpeech.SUCCESS) {
                    //set tts settings
                    tts.setLanguage(Locale.UK);
                } else {
                    inputTextView.setText("Text to Speech not available...");
                    inputTextView.setEnabled(false);
                    button.setClickable(false);
                }
            }
        });

        //Set speech rate of TTS to be slower.
        tts.setSpeechRate(0.7f);

        //set listener for TTS and when button is clicked to translate
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = inputTextView.getText().toString();
                tts.speak(inputText, tts.QUEUE_FLUSH, null);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.shutdown();
        }
    }
}
