package teamenglify.englify.Model;

import java.util.ArrayList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Vocab extends Module{
    public ArrayList<VocabPart> vocabParts;

    public Vocab (String name, String imgURL, ArrayList<VocabPart> vocabParts) {
        super(name, imgURL);
        this.vocabParts = vocabParts;
    }
}
