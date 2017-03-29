package teamenglify.englify;

import android.content.Intent;
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
    private TextView speechDisplayTextView;
    private TextView speechToMatchTextView;
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

    public static SpeechRecognition newInstance(Object object) {
        SpeechRecognition fragment = new SpeechRecognition();
        Bundle args = new Bundle();
        fragment.object = object;
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
        speechDisplayTextView = (TextView) view.findViewById(R.id.speechDisplayTextView);
        speechToMatchTextView = (TextView) view.findViewById(R.id.speechToMatchTextView);
        speechReturnTextView = (TextView) view.findViewById(R.id.speechReturnTextView);
        speechProgressBar = (ProgressBar) view.findViewById(R.id.speechProgressBar);
        speechButton = (ImageButton) view.findViewById(R.id.speechImageButton);
        speechProgressBar.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mainActivity.hasInternetConnection == true) {
            initializeSpeechRecognition();
        } else {
            speechDisplayTextView.setText(R.string.Speech_Recognition_Requires_Internet);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (speech != null) {
            releaseResources();
        }
        mHandler.removeCallbacks(mBackgroundThread);
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
            speechDisplayTextView.setText(R.string.press_hold_b);
            setButtonListener();
            mHandler.post(mBackgroundThread);
            updateUI();
        } else {
            speechDisplayTextView.setText(R.string.Speech_Recognition_Unavailable);
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
                        startTimeoutTimer();
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
        int score = calculateScore(results);
        if (score == 999) {
            speechReturnTextView.setText(matches.get(0));
        } else {
            speechReturnTextView.setText(matches.get(0) + ". Score: " + score + "%");
        }
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

    private Runnable mBackgroundThread = new Runnable() {
        public void run() {
            //check for page changes
            if (mainActivity.position != position) {
                //page has changed
                position = mainActivity.position;
                updateUI();
                //wipe the returnTextView
                speechReturnTextView.setText("");
                //stop the timeout stopwatch
                if (stopWatch != null && stopWatch.isRunning()) {
                    resetTimeoutTimer();
                }
            }
            if (speech == null && mainActivity.hasInternetConnection == true) {
                initializeSpeechRecognition();
            }
            if (stopWatch != null && stopWatch.isRunning()) {
                checkTimeoutTimer();
            }
            mHandler.postDelayed(mBackgroundThread, 500);
        }
    };

    public void updateUI() {
        Log.d("Englify", "Class SpeechRecognition: Method updateUI(): Updating UI");
        if (object instanceof Vocab) {
            VocabPart vocabPart = ((Vocab)object).vocabParts.get(position);
            textToMatch = vocabPart.text;
        } else if (object instanceof Read) {
            ReadPart readPart = ((Read)object).readParts.get(position);
            textToMatch = readPart.reading;
        } else if (object instanceof ExerciseChapter) {
            ExerciseChapterPart exerciseChapterPart = ((ExerciseChapter) object).chapterParts.get(position);
            textToMatch = exerciseChapterPart.text;
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

}
