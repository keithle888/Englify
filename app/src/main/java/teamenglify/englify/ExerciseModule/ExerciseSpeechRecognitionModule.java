package teamenglify.englify.ExerciseModule;


import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import teamenglify.englify.CustomSpeechRecognition;
import teamenglify.englify.R;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseSpeechRecognitionModule extends Fragment {
    private CustomSpeechRecognition speech;
    private ImageButton button;
    private String question;
    private TextView questionTextView;
    private TextView returnTextView;


    public ExerciseSpeechRecognitionModule() {
        // Required empty public constructor
    }

    public static Fragment newInstance(String question) {
        ExerciseSpeechRecognitionModule frag = new ExerciseSpeechRecognitionModule();
        frag.question = question;
        return frag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_speech_recognition_module, container, false);

        button = view.findViewById(R.id.exercise_speech_button);
        questionTextView = view.findViewById(R.id.exercise_speech_question_text);
        returnTextView = view.findViewById(R.id.exercise_speech_return_text);

        questionTextView.setText(question);

        speech = new CustomSpeechRecognition(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Timber.d("onReadyForSpeech()");
                button.setClickable(true);
            }

            @Override
            public void onBeginningOfSpeech() {
                Timber.d("onBeginningOfSpeech()");
                returnTextView.setText("Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                Timber.d("onEndOfSpeech()");
                returnTextView.setText("Loading...");
            }

            @Override
            public void onError(int error) {
                Timber.e("speech onError()");
                button.setClickable(false);
                returnTextView.setText("Error with speech recognition.");
            }

            @Override
            public void onResults(Bundle results) {
                returnTextView.setText(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                onResults(partialResults);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        }, getActivity());

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Timber.d("Speech button pressed.");
                    speech.startListening();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Timber.d("Speech button released.");
                    speech.stopListening();
                }
                return true;
            }
        });

        return view;
    }



}
