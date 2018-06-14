package teamenglify.englify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.Locale;

public class CustomSpeechRecognition {
    SpeechRecognizer speech;
    Intent intent;


    public CustomSpeechRecognition(RecognitionListener listener, Activity activity) {
        speech = SpeechRecognizer.createSpeechRecognizer(activity);
        speech.setRecognitionListener(listener);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    }

    private CustomSpeechRecognition(){}

    public static boolean isSpeechRecognitionAvailable(Context context) {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    public void close() {
        speech.destroy();
        speech = null;
        intent = null;
    }

    public void startListening() {
        speech.startListening(intent);
    }

    public void stopListening() {
        speech.stopListening();
    }
}
