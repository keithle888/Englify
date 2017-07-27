package teamenglify.englify;

import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.ServiceCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.ExerciseChapterPart;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.ReadPart;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.Model.VocabPart;

import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.read;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpeechRecognition#newInstance} factory method to
 * create an instance of this fragment.
 * */
public class SpeechRecognition extends Fragment implements RecognitionListener {
    public static final String FM_TAG_NAME = "SPEECH_RECOGNITION";
    private static final String TAG = SpeechRecognition.class.getSimpleName();
    public TextView speechToMatchTextView;
    private TextView speechReturnTextView;
    private ProgressBar speechProgressBar;
    private SpeechRecognizer speech;
    private ImageButton speechButton;
    private Intent recognizerIntent;
    private String textToMatch;
    private int position;
    private Handler mHandler = new Handler();
    private Object object;
    private long replyTimeOut = 3000;
    private StopWatch stopWatch;

    public SpeechRecognition() {
        // Required empty public constructor
    }

    public static SpeechRecognition newInstance(Object object, TextView speechToMatchTextView, TextView speechReturnTextView) {
        SpeechRecognition fragment = new SpeechRecognition();
        Bundle args = new Bundle();
        fragment.object = object;
        fragment.speechToMatchTextView = speechToMatchTextView;
        fragment.speechReturnTextView = speechReturnTextView;
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
        View view = inflater.inflate(R.layout.fragment_speech_recognition, container, false);
        bindViews(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mainActivity.hasInternetConnection == true) {
            initializeSpeechRecognition();
        } else {
            speechReturnTextView.setText(R.string.Speech_Recognition_Requires_Internet);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (speech != null) {
            releaseResources();
        }
    }

    public void initializeSpeechRecognition() {
        //initialize speech recognizer
        speech = SpeechRecognizer.createSpeechRecognizer(getContext());
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getActivity().getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (SpeechRecognizer.isRecognitionAvailable(getContext())) {
            setButtonListener();
            updateUI(0);
        } else {
            speechReturnTextView.setText(R.string.Speech_Recognition_Unavailable);
            speechToMatchTextView.setText("");
            speechReturnTextView.setText("");
            speechButton.setClickable(false);
        }
    }

    public void setButtonListener() {
        Log.d("SpeechRecognition", "Setting speechButton onTouchListeners");
        speechButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (mainActivity.hasInternetConnection) {
                            speechProgressBar.setVisibility(View.VISIBLE);
                            speech.startListening(recognizerIntent);
                            speechReturnTextView.setText("");
                        } else {
                            speechReturnTextView.setText(R.string.speech_recognition_no_internet_connection);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (mainActivity.hasInternetConnection) {
                            speechProgressBar.setVisibility(View.INVISIBLE);
                            speechReturnTextView.setText("Translating audio...");
                            speech.stopListening();
                            startTimeoutTimer();
                        }
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
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        speechReturnTextView.setText(matches.get(0));
        //Call off the timeout timer.
        if (stopWatch != null && stopWatch.isRunning()) {
            resetTimeoutTimer();
        }
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

    public void updateUI(int pageNumber) {
        //Update page number for tracking.
        if (this.position != pageNumber) {
            position = pageNumber;
        }
        if (speechReturnTextView != null) {
            speechReturnTextView.setText("");
        }
        Log.d("Englify", "Class SpeechRecognition: Method updateUI(): Updating UI");
        if (object instanceof Vocab) {
            VocabPart vocabPart = ((Vocab)object).vocabParts.get(position);
            textToMatch = vocabPart.text;
        } else if (object instanceof Read) {
            if(((Read) object).readParts.size()!=0){
                ReadPart readPart = ((Read)object).readParts.get(position);
                textToMatch = readPart.reading;
            }
        } else if (object instanceof ExerciseChapter) {
            if(((ExerciseChapter) object).chapterParts.size()!=0){
                ExerciseChapterPart exerciseChapterPart = ((ExerciseChapter) object).chapterParts.get(position);
                textToMatch = exerciseChapterPart.question;
            }
        }
        if (textToMatch == null) {
            speechToMatchTextView.setText("Text is missing.");
        } else {
            speechToMatchTextView.setText(textToMatch);
        }
    }

    public void releaseResources() {
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
    }

    public void startTimeoutTimer() {
        if (stopWatch == null) {
            stopWatch = new StopWatch();
        }
        stopWatch.start();
        Log.d("Englify", "Class SpeechRecognition: Method startTimeoutTimer(): Timer started." );
    }

    public void checkTimeoutTimer() {
        if (stopWatch.isRunning() && stopWatch.lapTime() > replyTimeOut) {
            speechReturnTextView.setText(getString(R.string.Speech_Recognition_Timeout) + " - " + getString(R.string.Speech_Recognition_Timeout_b));
            resetTimeoutTimer();
            Log.d("Englify", "Class SpeechRecognition: Method checkTimeoutTimer(): Timed out." );
        }
        Log.d("Englify", "Class SpeechRecognition: Method checkTimeoutTimer(): Timer has not timed out. " + (replyTimeOut - stopWatch.lapTime()) + "ms left." );
    }

    public void resetTimeoutTimer() {
        stopWatch.stop();
        Log.d("Englify", "Class SpeechRecognition: Method resetTimeoutTimer: Timer reset." );
    }

    public int calculateScore(Bundle results) {
        String returnText = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        if (scores != null && scores.length != 0) {
            float score = scores[0];
            Log.d("Englify", "Class SpeechRecognition: Method calculateScore(): Base score received from results is => " + score);
            String[] dReturnText = returnText.split(" ");
            String[] dMatchText = (textToMatch.split("-"))[0].trim().split(" "); //Seperate the myanmese part out, then trim whitespaces, then break the english part down into words.
            Log.d("Englify", "Class SpeechRecognition: Method calculateScore(): MatchText => " + Arrays.toString(dMatchText) + " ReturnText => " + Arrays.toString(dReturnText));
            int correctWords = 0;
            if (dReturnText.length >= dMatchText.length) {
                for (int i = 0; i < dMatchText.length ; i++) {
                    if (dMatchText[i].toLowerCase().contains(dReturnText[i].toLowerCase())) {
                        correctWords++;
                    }
                }
            } else {
                for (int i = 0; i < dReturnText.length ; i++) {
                    if (dReturnText[i].toLowerCase().contains(dMatchText[i].toLowerCase())) {
                        correctWords++;
                    }
                }
            }
            Log.d("Englify", "Class SpeechRecognition: Method calculateScore(): Number of correct words => " + correctWords);
            return (int)((score * (correctWords / (double) dMatchText.length)) * 100);
        }
        return 999;
    }

    public void bindViews(View view) {
        //Bind common modules
        speechButton = (ImageButton) view.findViewById(R.id.speechImageButton);
        speechProgressBar = (ProgressBar) view.findViewById(R.id.speechProgressBar);
        Log.i(TAG, "Binding common view modules.");
    }
}
