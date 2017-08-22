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
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class TextToSpeech extends Fragment {

    private android.speech.tts.TextToSpeech tts;
    private EditText inputTextView;
    private ImageButton button;
    private SeekBar slider;
    private TextView sliderValueTextView;

    public TextToSpeech() {
        // Required empty public constructor
    }

    public static TextToSpeech newInstance() {
        TextToSpeech fragment = new TextToSpeech();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_to_speech, container, false);
        button = (ImageButton) view.findViewById(R.id.ttsButton);
        inputTextView = (EditText) view.findViewById(R.id.ttsInputText);
        slider = (SeekBar) view.findViewById(R.id.tts_slider);
        sliderValueTextView = (TextView) view.findViewById(R.id.tts_slider_textview);

        MainActivity.mainActivity.getSupportActionBar().setTitle(R.string.pronounce_title);
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

        //Setup slider behaviour
        slider.setProgress(70);
        sliderValueTextView.setText("" + ((double)slider.getProgress()) / slider.getMax() + "x");
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tts.setSpeechRate( (float) ((double)slider.getProgress()) / slider.getMax() );
                sliderValueTextView.setText("" + ((double)slider.getProgress()) / slider.getMax() + "x");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        //Set speech rate of TTS to be slower.

        //set listener for TTS and when button is clicked to translate
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = inputTextView.getText().toString();
                tts.speak(inputText, tts.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.shutdown();
        }
        tts = null;
    }
}
