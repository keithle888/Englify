package teamenglify.englify.Model;

import java.io.Serializable;

/**
 * Created by Keith on 07-Mar-17.
 */

public class VocabPart implements Serializable {
    public String text;
    public String imgURL;
    public String audioURL;

    public VocabPart(String text, String imgURL, String audioURL) {
        this.text = text;
        this.imgURL = imgURL;
        this.audioURL = audioURL;
    }
}
