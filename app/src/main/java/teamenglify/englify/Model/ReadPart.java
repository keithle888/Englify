package teamenglify.englify.Model;

import java.io.Serializable;

/**
 * Created by Keith on 07-Mar-17.
 */

public class ReadPart implements Serializable {
    public String reading;
    public String audioURL;
    public String imgURL;

    public ReadPart(String reading, String imgURL, String audioURL) {
        this.reading = reading;
        this.audioURL = audioURL;
        this.imgURL = imgURL;
    }
}
