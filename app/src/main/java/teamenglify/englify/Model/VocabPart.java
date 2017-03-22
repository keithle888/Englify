package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Keith on 07-Mar-17.
 */

public class VocabPart implements Serializable {
    public String text;
    public String imgURL;
    public String audioURL;
    public Date lastModified;

    public VocabPart(String text, String imgURL, String audioURL) {
        this.text = text;
        this.imgURL = imgURL;
        this.audioURL = audioURL;
    }

    public VocabPart(String text, String imgURL, String audioURL, Date lastModified) {
        this.text = text;
        this.imgURL = imgURL;
        this.audioURL = audioURL;
        this.lastModified = lastModified;
    }
}
