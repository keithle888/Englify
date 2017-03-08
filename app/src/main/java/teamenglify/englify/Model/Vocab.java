package teamenglify.englify.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Vocab extends Module{
    public ArrayList<VocabPart> vocabParts;

    public Vocab (String name, String imgURL, ArrayList<VocabPart> vocabParts) {
        super(name, imgURL);
        this.vocabParts = vocabParts;
    }

    public Vocab (String name) {
        super(name);
        vocabParts = new ArrayList<VocabPart>();
    }

    public void addVocabPartAudio(String vocabPartName, String audioAbsolutePath) {
        if (doesVocabPartExist(vocabPartName)) {
            VocabPart vocabPart = findVocabPart(vocabPartName);
            vocabPart.audioURL = audioAbsolutePath;
        } else {
            vocabParts.add(new VocabPart(vocabPartName, null, audioAbsolutePath));
        }
    }

    public void addVocabPartImg(String vocabPartName, String imgAbsolutePath) {
        if (doesVocabPartExist(vocabPartName)) {
            VocabPart vocabPart = findVocabPart(vocabPartName);
            vocabPart.imgURL = imgAbsolutePath;
        } else {
            vocabParts.add(new VocabPart(vocabPartName, imgAbsolutePath, null));
        }
    }

    public boolean doesVocabPartExist(String vocabPartName) {
        for (VocabPart vocabPart : vocabParts) {
            if (vocabPart.text.equalsIgnoreCase(vocabPartName)) {
                return true;
            }
        }
        return false;
    }

    public VocabPart findVocabPart(String vocabPartName) {
        VocabPart toReturn = null;
        for (VocabPart vocabPart : vocabParts) {
            if (vocabPart.text.equalsIgnoreCase(vocabPartName)) {
                toReturn = vocabPart;
            }
        }
        return toReturn;
    }

    public void overwriteTexts(LinkedList<String> texts) {
        Log.d("Englify", "Class Vocab: Method overwriteTexts(): Texts received for overwriting VocabParts, contents are -> " + texts.toString());
        Log.d("Englify", "Class Vocab: Method overwriteTexts(): Number of VocabParts -> " + vocabParts.size() + " and number of Overwrite texts -> " + texts.size());
        if (texts != null) {
            for (VocabPart vocabPart : vocabParts) {
                String text = texts.pop();
                vocabPart.text = text;
            }
        }
    }
}
