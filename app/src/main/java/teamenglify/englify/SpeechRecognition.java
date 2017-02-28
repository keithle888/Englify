package teamenglify.englify;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.*;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpeechRecognition#newInstance} factory method to
 * create an instance of this fragment.
 * */
public class SpeechRecognition extends Fragment implements RecognitionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView speechDisplayTextView;
    private TextView speechToMatchTextView;
    private TextView speechReturnTextView;
    private ProgressBar speechProgressBar;
    private SpeechRecognizer speech;
    private ImageButton speechButton;
    private Intent recognizerIntent;
    private String textToMatch;
    private int currentPage = 99999; //arbitrary value to ensure speechRecognition works when loaded.
    private Handler mHandler = new Handler();


    public SpeechRecognition() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpeechRecognition.
     */

    public static SpeechRecognition newInstance(String param1, String param2) {
        SpeechRecognition fragment = new SpeechRecognition();
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
        View view = inflater.inflate(R.layout.fragment_speech_recognition, container, false);

        speechDisplayTextView = (TextView) view.findViewById(R.id.speechDisplayTextView);
        speechToMatchTextView = (TextView) view.findViewById(R.id.speechToMatchTextView);
        speechReturnTextView = (TextView) view.findViewById(R.id.speechReturnTextView);
        speechProgressBar = (ProgressBar) view.findViewById(R.id.speechProgressBar);
        speechButton = (ImageButton) view.findViewById(R.id.speechImageButton);
        speechProgressBar.setVisibility(View.INVISIBLE);
        //initialize speech recognizer
        speech = SpeechRecognizer.createSpeechRecognizer(getContext());
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getActivity().getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (SpeechRecognizer.isRecognitionAvailable(getContext())) {
            speechDisplayTextView.setText(R.string.press_hold_b);
            setButtonListener();
            executeAsyncTask();
        } else {
            speechDisplayTextView.setText("Speech Recognition not available.");
            speechToMatchTextView.setText("");
            speechReturnTextView.setText("");
            speechButton.setClickable(false);
        }

        return view;
    }

    public void setButtonListener() {
        Log.d("SpeechRecognition", "Setting speechButton onTouchListeners");
        speechButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        speechProgressBar.setVisibility(View.VISIBLE);
                        speechProgressBar.setIndeterminate(true);
                        speech.startListening(recognizerIntent);
                        speechReturnTextView.setText("");
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        speechProgressBar.setVisibility(View.INVISIBLE);
                        speechProgressBar.setIndeterminate(false);
                        speechReturnTextView.setText("Translating audio...");
                        speech.stopListening();
                        break;
                    }
                }
                return true;
            }
        });

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("SpeechRecognition", "onBeginningOfSpeech");
        speechProgressBar.setIndeterminate(false);
        speechProgressBar.setMax(20);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("SpeechRecognition", "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("SpeechRecognition", "End of speech");
        speechProgressBar.setIndeterminate(true);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d("SpeechRecognition", "FAILED " + errorMessage);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.d("SpeechRecognition", "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        speechReturnTextView.setText(arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d("SpeechRecognition", "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.d("SpeechRecognition", "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        speechReturnTextView.setText(matches.get(0));
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        speechProgressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (speech != null) {
            speech = null;
        }
    }

    private class asyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...Void) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Log.d("SpeechRecognition", "asyncTask error: " + e.toString());
            }
            if (currentPage != MainActivity.getMainActivity().currentPage) {
                //if the currentPage on mainActivity has changed, it will be detected and this instance's value will be updated.
                currentPage = MainActivity.getMainActivity().currentPage;
                //wait until readyForSpeechRecognitionToLoad is true then load the URL and TextsToMatch
                if (MainActivity.getMainActivity().readyForSpeechRecognitionToLoad) {
                    mHandler.post(mUpdateUI);
                }
            }
            //ensure that the correct texts are loaded when instantiated for the first time
            if (currentPage != 99999 && textToMatch == null) {
                mHandler.post(mUpdateUI);
            }
            executeAsyncTask();
            return null;
        }
    }

    private void executeAsyncTask() {
        new asyncTask().execute();
    }

    private Runnable mUpdateUI = new Runnable() {
        public void run() {
            if (mParam1.equalsIgnoreCase("Conversation")) {
                Log.d("SpeechRecognition", "mUpdateUI: " + mParam1 + "," + MainActivity.getMainActivity().audioConversationTextsToMatch.toString());
                textToMatch = MainActivity.getMainActivity().audioConversationTextsToMatch.get(currentPage);
            } else if (mParam1.equalsIgnoreCase("Vocab")) {
                Log.d("SpeechRecognition", "mUpdateUI: " + mParam1 + "," + MainActivity.getMainActivity().vocabListing.toString());
                textToMatch = MainActivity.getMainActivity().vocabListing.get(currentPage);
            }
            speechToMatchTextView.setText(textToMatch);
        }
    };
}
