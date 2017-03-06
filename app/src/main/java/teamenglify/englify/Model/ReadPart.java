package teamenglify.englify.Model;

import java.io.Serializable;

/**
 * Created by Keith on 07-Mar-17.
 */

public class ReadPart implements Serializable {
    public String reading;
    public String audioFile;
    public String imgFile;

    public ReadPart(String reading, String audioFile, String imgFile) {
        this.reading = reading;
        this.audioFile = audioFile;
        this.imgFile = imgFile;
    }
}
